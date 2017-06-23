package net.minecraft.server.v1_12_R1;

import com.boydti.cbs.CommandProcessor;
import com.boydti.cbs.Main;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.google.gson.JsonParseException;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.CommandBlockListenerAbstract;
import net.minecraft.server.v1_12_R1.DedicatedServer;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.ICommandListener;
import net.minecraft.server.v1_12_R1.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.craftbukkit.v1_12_R1.command.CraftBlockCommandSender;

public abstract class CommandAbstract implements ICommand {

	private static ICommandDispatcher a;
	private static final Splitter b = Splitter.on(',');
	private static final Splitter c = Splitter.on('=').limit(2);

	/*
	 * Custom code
	 */

	private static boolean enabled = false;

	private static World lastWorld;
	private static BlockPosition lastPos;

	public CommandAbstract() {
		lastWorld = null;
		lastPos = null;
	}

	/**
	 * Forwards to setOther, setCommandBlock or setPlayer
	 * 
	 * @param listener
	 * @param command
	 * @param clazz
	 * @return
	 */
	public static boolean setListener(final ICommandListener listener, final Class<?> clazz, String command) {
		lastWorld = listener.getWorld();
		lastPos = listener.getChunkCoordinates();
		if (!(listener instanceof CommandBlockListenerAbstract)) {
			if (listener instanceof EntityPlayer) {
				return enabled = CommandProcessor.manager.setPlayer(((EntityPlayer) listener).getBukkitEntity(), clazz,
						command);
			}
			if (listener instanceof DedicatedServer) {
				return enabled = CommandProcessor.manager.setConsole(clazz, command);
			}
			final Entity entity = listener.f();
			if (entity != null) {
				return enabled = CommandProcessor.manager.setLocation(entity.getBukkitEntity().getLocation(), clazz,
						command);
			}
			Main.debug("Unknown command sender: " + listener.getName() + " | " + listener.getClass());
			return enabled = CommandProcessor.manager.setOther(listener);
		}
		final CommandBlockListenerAbstract cmd = (CommandBlockListenerAbstract) listener;
		command = cmd.getCommand();
		final CraftBlockCommandSender sender = new CraftBlockCommandSender(listener);
		final org.bukkit.block.Block block = sender.getBlock();
		return enabled = CommandProcessor.manager.setCommandBlock(block, clazz, command);
	}

	/**
	 * Forwards to isLocationAllowed
	 * 
	 * @param entity
	 * @return
	 */
	public static Entity isEntityAllowed(final Entity entity) {
		if (!enabled) {
			return null;
		}
		final BlockPosition loc = entity.getChunkCoordinates();
		if (CommandProcessor.manager.isLocationAllowed(entity.getWorld().worldData.getName(), loc.getX(), loc.getY(),
				loc.getZ())) {
			return entity;
		}
		return null;
	}

	/**
	 * Forwards to isLocationAllowed
	 * 
	 * @param entity
	 * @return
	 */
	public static EntityPlayer isPlayerAllowed(final EntityPlayer entity) {
		return (EntityPlayer) isEntityAllowed(entity);
	}

	/**
	 * Forwards to isLocationAllowed
	 * 
	 * @param world
	 * @param loc
	 * @return
	 */
	public static BlockPosition isPositionAllowed(final World world, final BlockPosition loc) {
		if (!enabled) {
			return null;
		}
		if (CommandProcessor.manager.isLocationAllowed(world.worldData.getName(), loc.getX(), loc.getY(), loc.getZ())) {
			return loc;
		}
		return null;
	}

	/**
	 * Forwards to isLocationAllowed
	 * 
	 * @param world
	 * @param loc
	 * @return
	 */
	public static BlockPosition isPositionAllowed(final BlockPosition loc) {
		if (!enabled) {
			return null;
		}
		if (CommandProcessor.manager.isLocationAllowed(lastWorld.worldData.getName(), loc.getX(), loc.getY(),
				loc.getZ())) {
			return loc;
		}
		return null;
	}

	/*
	 * Modified internal code
	 */

	protected static ExceptionInvalidSyntax a(JsonParseException jsonparseexception) {
		Throwable throwable = ExceptionUtils.getRootCause(jsonparseexception);
		String s = "";

		if (throwable != null) {
			s = throwable.getMessage();
			if (s.contains("setLenient")) {
				s = s.substring(s.indexOf("to accept ") + 10);
			}
		}

		return new ExceptionInvalidSyntax("commands.tellraw.jsonException", new Object[] { s });
	}

	public static NBTTagCompound a(Entity entity) {
		NBTTagCompound nbttagcompound = entity.save(new NBTTagCompound());

		if (entity instanceof EntityHuman) {
			ItemStack itemstack = ((EntityHuman) entity).inventory.getItemInHand();

			if (!itemstack.isEmpty()) {
				nbttagcompound.set("SelectedItem", itemstack.save(new NBTTagCompound()));
			}
		}

		return nbttagcompound;
	}

	public int a() {
		return 4;
	}

	public List<String> getAliases() {
		return Collections.emptyList();
	}

	public boolean canUse(MinecraftServer minecraftserver, ICommandListener icommandlistener) {
		enabled = setListener(icommandlistener, getClass(), getCommand()) && icommandlistener.a(a(), getCommand());
		return icommandlistener.a(this.a(), this.getCommand());
	}

	public List<String> tabComplete(MinecraftServer minecraftserver, ICommandListener icommandlistener,
			String[] astring, @Nullable BlockPosition blockposition) {
		return Collections.emptyList();
	}

	public static int a(String s) throws ExceptionInvalidNumber {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException numberformatexception) {
			throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { s });
		}
	}

	public static int a(String s, int i) throws ExceptionInvalidNumber {
		return a(s, i, Integer.MAX_VALUE);
	}

	public static int a(String s, int i, int j) throws ExceptionInvalidNumber {
		int k = a(s);

		if (k < i) {
			throw new ExceptionInvalidNumber("commands.generic.num.tooSmall",
					new Object[] { Integer.valueOf(k), Integer.valueOf(i) });
		} else if (k > j) {
			throw new ExceptionInvalidNumber("commands.generic.num.tooBig",
					new Object[] { Integer.valueOf(k), Integer.valueOf(j) });
		} else {
			return k;
		}
	}

	public static long b(String s) throws ExceptionInvalidNumber {
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException numberformatexception) {
			throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { s });
		}
	}

	public static long a(String s, long i, long j) throws ExceptionInvalidNumber {
		long k = b(s);

		if (k < i) {
			throw new ExceptionInvalidNumber("commands.generic.num.tooSmall",
					new Object[] { Long.valueOf(k), Long.valueOf(i) });
		} else if (k > j) {
			throw new ExceptionInvalidNumber("commands.generic.num.tooBig",
					new Object[] { Long.valueOf(k), Long.valueOf(j) });
		} else {
			return k;
		}
	}

	public static BlockPosition a(ICommandListener icommandlistener, String[] astring, int i, boolean flag)
			throws ExceptionInvalidNumber {
		BlockPosition blockposition = icommandlistener.getChunkCoordinates();

		return new BlockPosition(b((double) blockposition.getX(), astring[i], -30000000, 30000000, flag),
				b((double) blockposition.getY(), astring[i + 1], 0, 256, false),
				b((double) blockposition.getZ(), astring[i + 2], -30000000, 30000000, flag));
	}

	public static double c(String s) throws ExceptionInvalidNumber {
		try {
			double d0 = Double.parseDouble(s);

			if (!Doubles.isFinite(d0)) {
				throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { s });
			} else {
				return d0;
			}
		} catch (NumberFormatException numberformatexception) {
			throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { s });
		}
	}

	public static double a(String s, double d0) throws ExceptionInvalidNumber {
		return a(s, d0, Double.MAX_VALUE);
	}

	public static double a(String s, double d0, double d1) throws ExceptionInvalidNumber {
		double d2 = c(s);

		if (d2 < d0) {
			throw new ExceptionInvalidNumber("commands.generic.num.tooSmall",
					new Object[] { String.format("%.2f", new Object[] { Double.valueOf(d2) }),
							String.format("%.2f", new Object[] { Double.valueOf(d0) }) });
		} else if (d2 > d1) {
			throw new ExceptionInvalidNumber("commands.generic.num.tooBig",
					new Object[] { String.format("%.2f", new Object[] { Double.valueOf(d2) }),
							String.format("%.2f", new Object[] { Double.valueOf(d1) }) });
		} else {
			return d2;
		}
	}

	public static boolean d(String s) throws CommandException {
		if (!"true".equals(s) && !"1".equals(s)) {
			if (!"false".equals(s) && !"0".equals(s)) {
				throw new CommandException("commands.generic.boolean.invalid", new Object[] { s });
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public static EntityPlayer a(ICommandListener icommandlistener) throws ExceptionPlayerNotFound {
		if (icommandlistener instanceof EntityPlayer) {
			return isPlayerAllowed((EntityPlayer) icommandlistener);
		} else {
			throw new ExceptionPlayerNotFound("commands.generic.player.unspecified");
		}
	}

	public static List<EntityPlayer> a(MinecraftServer minecraftserver, ICommandListener icommandlistener, String s)
			throws CommandException {
		List<EntityPlayer> values = PlayerSelector.b(icommandlistener, s);
		if (values.isEmpty()) {
			Lists.newArrayList(new EntityPlayer[] { a(minecraftserver, (EntityPlayer) null, s) });
		}
		final Iterator<EntityPlayer> iter = values.iterator();
		while (iter.hasNext()) {
			if (isEntityAllowed(iter.next()) == null) {
				iter.remove();
			}
		}
		return values;
	}

	public static EntityPlayer b(MinecraftServer minecraftserver, ICommandListener icommandlistener, String s)
			throws CommandException {
		return isPlayerAllowed(a(minecraftserver, PlayerSelector.getPlayer(icommandlistener, s), s));
	}

	private static EntityPlayer a(MinecraftServer minecraftserver, @Nullable EntityPlayer entityplayer, String s)
			throws CommandException {
		if (entityplayer == null) {
			try {
				entityplayer = minecraftserver.getPlayerList().a(UUID.fromString(s));
			} catch (IllegalArgumentException illegalargumentexception) {
				;
			}
		}

		if (entityplayer == null) {
			entityplayer = minecraftserver.getPlayerList().getPlayer(s);
		}

		if (entityplayer == null) {
			throw new ExceptionPlayerNotFound("commands.generic.player.notFound", new Object[] { s });
		} else {
			return isPlayerAllowed(entityplayer);
		}
	}

	public static Entity c(MinecraftServer minecraftserver, ICommandListener icommandlistener, String s)
			throws CommandException {
		return isEntityAllowed(a(minecraftserver, icommandlistener, s, Entity.class));
	}

	public static <T extends Entity> T a(MinecraftServer minecraftserver, ICommandListener icommandlistener, String s,
			Class<? extends T> oclass) throws CommandException {
		Object object = PlayerSelector.getEntity(icommandlistener, s, oclass);

		if (object == null) {
			object = minecraftserver.getPlayerList().getPlayer(s);
		}

		if (object == null) {
			try {
				UUID uuid = UUID.fromString(s);

				object = minecraftserver.a(uuid);
				if (object == null) {
					object = minecraftserver.getPlayerList().a(uuid);
				}
			} catch (IllegalArgumentException illegalargumentexception) {
				if (s.split("-").length == 5) {
					throw new ExceptionEntityNotFound("commands.generic.entity.invalidUuid", new Object[] { s });
				}
			}
		}

		if (object != null && oclass.isAssignableFrom(object.getClass())) {
			return (T) isEntityAllowed((Entity) object);
		} else {
			throw new ExceptionEntityNotFound(s);
		}
	}

	public static List<Entity> d(MinecraftServer minecraftserver, ICommandListener icommandlistener, String s)
			throws CommandException {
		List<Entity> values;
		if (PlayerSelector.isPattern(s)) {
			values = PlayerSelector.getPlayers(icommandlistener, s, Entity.class);
		} else {
			values = Lists.newArrayList(new Entity[] { b(minecraftserver, icommandlistener, s) });
		}
		final Iterator<Entity> iter = values.iterator();
		while (iter.hasNext()) {
			if (isEntityAllowed(iter.next()) == null) {
				iter.remove();
			}
		}
		return values;
	}

	public static String e(MinecraftServer minecraftserver, ICommandListener icommandlistener, String s)
			throws CommandException {
		try {
			return b(minecraftserver, icommandlistener, s).getName();
		} catch (CommandException commandexception) {
			if (PlayerSelector.isPattern(s)) {
				throw commandexception;
			} else {
				return s;
			}
		}
	}

	public static String f(MinecraftServer minecraftserver, ICommandListener icommandlistener, String s)
			throws CommandException {
		try {
			return b(minecraftserver, icommandlistener, s).getName();
		} catch (ExceptionPlayerNotFound exceptionplayernotfound) {
			try {
				return c(minecraftserver, icommandlistener, s).bn();
			} catch (ExceptionEntityNotFound exceptionentitynotfound) {
				if (PlayerSelector.isPattern(s)) {
					throw exceptionentitynotfound;
				} else {
					return s;
				}
			}
		}
	}

	public static IChatBaseComponent a(ICommandListener icommandlistener, String[] astring, int i)
			throws CommandException {
		return b(icommandlistener, astring, i, false);
	}

	public static IChatBaseComponent b(ICommandListener icommandlistener, String[] astring, int i, boolean flag)
			throws CommandException {
		ChatComponentText chatcomponenttext = new ChatComponentText("");

		for (int j = i; j < astring.length; ++j) {
			if (j > i) {
				chatcomponenttext.a(" ");
			}

			Object object = new ChatComponentText(astring[j]);

			if (flag) {
				IChatBaseComponent ichatbasecomponent = PlayerSelector.getPlayerNames(icommandlistener, astring[j]);

				if (ichatbasecomponent == null) {
					if (PlayerSelector.isPattern(astring[j])) {
						throw new ExceptionPlayerNotFound("commands.generic.selector.notFound",
								new Object[] { astring[j] });
					}
				} else {
					object = ichatbasecomponent;
				}
			}

			chatcomponenttext.addSibling((IChatBaseComponent) object);
		}

		return chatcomponenttext;
	}

	public static String a(String[] astring, int i) {
		StringBuilder stringbuilder = new StringBuilder();

		for (int j = i; j < astring.length; ++j) {
			if (j > i) {
				stringbuilder.append(" ");
			}

			String s = astring[j];

			stringbuilder.append(s);
		}

		return stringbuilder.toString();
	}

	public static CommandAbstract.CommandNumber a(double d0, String s, boolean flag) throws ExceptionInvalidNumber {
		return a(d0, s, -30000000, 30000000, flag);
	}

	public static CommandAbstract.CommandNumber a(double d0, String s, int i, int j, boolean flag)
			throws ExceptionInvalidNumber {
		boolean flag1 = s.startsWith("~");

		if (flag1 && Double.isNaN(d0)) {
			throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { Double.valueOf(d0) });
		} else {
			double d1 = 0.0D;

			if (!flag1 || s.length() > 1) {
				boolean flag2 = s.contains(".");

				if (flag1) {
					s = s.substring(1);
				}

				d1 += c(s);
				if (!flag2 && !flag1 && flag) {
					d1 += 0.5D;
				}
			}

			double d2 = d1 + (flag1 ? d0 : 0.0D);

			if (i != 0 || j != 0) {
				if (d2 < (double) i) {
					throw new ExceptionInvalidNumber("commands.generic.num.tooSmall", new Object[] {
							String.format("%.2f", new Object[] { Double.valueOf(d2) }), Integer.valueOf(i) });
				}

				if (d2 > (double) j) {
					throw new ExceptionInvalidNumber("commands.generic.num.tooBig", new Object[] {
							String.format("%.2f", new Object[] { Double.valueOf(d2) }), Integer.valueOf(j) });
				}
			}

			return new CommandAbstract.CommandNumber(d2, d1, flag1);
		}
	}

	public static double b(double d0, String s, boolean flag) throws ExceptionInvalidNumber {
		return b(d0, s, -30000000, 30000000, flag);
	}

	public static double b(double d0, String s, int i, int j, boolean flag) throws ExceptionInvalidNumber {
		boolean flag1 = s.startsWith("~");

		if (flag1 && Double.isNaN(d0)) {
			throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { Double.valueOf(d0) });
		} else {
			double d1 = flag1 ? d0 : 0.0D;

			if (!flag1 || s.length() > 1) {
				boolean flag2 = s.contains(".");

				if (flag1) {
					s = s.substring(1);
				}

				d1 += c(s);
				if (!flag2 && !flag1 && flag) {
					d1 += 0.5D;
				}
			}

			if (i != 0 || j != 0) {
				if (d1 < (double) i) {
					throw new ExceptionInvalidNumber("commands.generic.num.tooSmall", new Object[] {
							String.format("%.2f", new Object[] { Double.valueOf(d1) }), Integer.valueOf(i) });
				}

				if (d1 > (double) j) {
					throw new ExceptionInvalidNumber("commands.generic.num.tooBig", new Object[] {
							String.format("%.2f", new Object[] { Double.valueOf(d1) }), Integer.valueOf(j) });
				}
			}

			return d1;
		}
	}

	public static Item a(ICommandListener icommandlistener, String s) throws ExceptionInvalidNumber {
		MinecraftKey minecraftkey = new MinecraftKey(s);
		Item item = (Item) Item.REGISTRY.get(minecraftkey);

		if (item == null) {
			throw new ExceptionInvalidNumber("commands.give.item.notFound", new Object[] { minecraftkey });
		} else {
			return item;
		}
	}

	public static Block b(ICommandListener icommandlistener, String s) throws ExceptionInvalidNumber {
		MinecraftKey minecraftkey = new MinecraftKey(s);

		if (!Block.REGISTRY.d(minecraftkey)) {
			throw new ExceptionInvalidNumber("commands.give.block.notFound", new Object[] { minecraftkey });
		} else {
			return (Block) Block.REGISTRY.get(minecraftkey);
		}
	}

	public static IBlockData a(Block block, String s) throws ExceptionInvalidNumber, ExceptionInvalidBlockState {
		try {
			int i = Integer.parseInt(s);

			if (i < 0) {
				throw new ExceptionInvalidNumber("commands.generic.num.tooSmall",
						new Object[] { Integer.valueOf(i), Integer.valueOf(0) });
			} else if (i > 15) {
				throw new ExceptionInvalidNumber("commands.generic.num.tooBig",
						new Object[] { Integer.valueOf(i), Integer.valueOf(15) });
			} else {
				return block.fromLegacyData(Integer.parseInt(s));
			}
		} catch (RuntimeException runtimeexception) {
			try {
				Map map = c(block, s);
				IBlockData iblockdata = block.getBlockData();

				Entry entry;

				for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); iblockdata = a(iblockdata,
						(IBlockState) entry.getKey(), (Comparable) entry.getValue())) {
					entry = (Entry) iterator.next();
				}

				return iblockdata;
			} catch (RuntimeException runtimeexception1) {
				throw new ExceptionInvalidBlockState("commands.generic.blockstate.invalid",
						new Object[] { s, Block.REGISTRY.b(block) });
			}
		}
	}

	private static <T extends Comparable<T>> IBlockData a(IBlockData iblockdata, IBlockState iblockstate,
			Comparable comparable) {
		return iblockdata.set(iblockstate, comparable);
	}

	public static Predicate<IBlockData> b(final Block block, String s) throws ExceptionInvalidBlockState {
		if (!"*".equals(s) && !"-1".equals(s)) {
			try {
				final int i = Integer.parseInt(s);

				return new Predicate() {
					public boolean a(@Nullable IBlockData iblockdata) {
						return i == iblockdata.getBlock().toLegacyData(iblockdata);
					}

					public boolean apply(@Nullable Object object) {
						return this.a((IBlockData) object);
					}
				};
			} catch (RuntimeException runtimeexception) {
				final Map map = c(block, s);

				return new Predicate() {
					public boolean a(@Nullable IBlockData iblockdata) {
						if (iblockdata != null && block == iblockdata.getBlock()) {
							Iterator iterator = map.entrySet().iterator();

							Entry entry;

							do {
								if (!iterator.hasNext()) {
									return true;
								}

								entry = (Entry) iterator.next();
							} while (iblockdata.get((IBlockState) entry.getKey()).equals(entry.getValue()));

							return false;
						} else {
							return false;
						}
					}

					public boolean apply(@Nullable Object object) {
						return this.a((IBlockData) object);
					}
				};
			}
		} else {
			return Predicates.alwaysTrue();
		}
	}

	private static Map<IBlockState<?>, Comparable<?>> c(Block block, String s) throws ExceptionInvalidBlockState {
		HashMap hashmap = Maps.newHashMap();

		if ("default".equals(s)) {
			return block.getBlockData().t();
		} else {
			BlockStateList blockstatelist = block.s();
			Iterator iterator = CommandAbstract.b.split(s).iterator();

			while (true) {
				if (!iterator.hasNext()) {
					return hashmap;
				}

				String s1 = (String) iterator.next();
				Iterator iterator1 = CommandAbstract.c.split(s1).iterator();

				if (!iterator1.hasNext()) {
					break;
				}

				IBlockState iblockstate = blockstatelist.a((String) iterator1.next());

				if (iblockstate == null || !iterator1.hasNext()) {
					break;
				}

				Comparable comparable = a(iblockstate, (String) iterator1.next());

				if (comparable == null) {
					break;
				}

				hashmap.put(iblockstate, comparable);
			}

			throw new ExceptionInvalidBlockState("commands.generic.blockstate.invalid",
					new Object[] { s, Block.REGISTRY.b(block) });
		}
	}

	@Nullable
	private static <T extends Comparable<T>> T a(IBlockState<T> iblockstate, String s) {
		return (T) iblockstate.b(s).orNull();
	}

	public static String a(Object[] aobject) {
		StringBuilder stringbuilder = new StringBuilder();

		for (int i = 0; i < aobject.length; ++i) {
			String s = aobject[i].toString();

			if (i > 0) {
				if (i == aobject.length - 1) {
					stringbuilder.append(" and ");
				} else {
					stringbuilder.append(", ");
				}
			}

			stringbuilder.append(s);
		}

		return stringbuilder.toString();
	}

	public static IChatBaseComponent a(List<IChatBaseComponent> list) {
		ChatComponentText chatcomponenttext = new ChatComponentText("");

		for (int i = 0; i < list.size(); ++i) {
			if (i > 0) {
				if (i == list.size() - 1) {
					chatcomponenttext.a(" and ");
				} else if (i > 0) {
					chatcomponenttext.a(", ");
				}
			}

			chatcomponenttext.addSibling((IChatBaseComponent) list.get(i));
		}

		return chatcomponenttext;
	}

	public static String a(Collection<String> collection) {
		return a(collection.toArray(new String[collection.size()]));
	}

	public static List<String> a(String[] astring, int i, @Nullable BlockPosition blockposition) {
		if (blockposition == null) {
			return Lists.newArrayList(new String[] { "~" });
		} else {
			int j = astring.length - 1;
			String s;

			if (j == i) {
				s = Integer.toString(blockposition.getX());
			} else if (j == i + 1) {
				s = Integer.toString(blockposition.getY());
			} else {
				if (j != i + 2) {
					return Collections.emptyList();
				}

				s = Integer.toString(blockposition.getZ());
			}

			return Lists.newArrayList(new String[] { s });
		}
	}

	public static List<String> b(String[] astring, int i, @Nullable BlockPosition blockposition) {
		if (blockposition == null) {
			return Lists.newArrayList(new String[] { "~" });
		} else {
			int j = astring.length - 1;
			String s;

			if (j == i) {
				s = Integer.toString(blockposition.getX());
			} else {
				if (j != i + 1) {
					return Collections.emptyList();
				}

				s = Integer.toString(blockposition.getZ());
			}

			return Lists.newArrayList(new String[] { s });
		}
	}

	public static boolean a(String s, String s1) {
		return s1.regionMatches(true, 0, s, 0, s.length());
	}

	public static List<String> a(String[] astring, String... astring1) {
		return a(astring, (Collection) Arrays.asList(astring1));
	}

	public static List<String> a(String[] astring, Collection<?> collection) {
		String s = astring[astring.length - 1];
		ArrayList arraylist = Lists.newArrayList();

		if (!collection.isEmpty()) {
			Iterator iterator = Iterables.transform(collection, Functions.toStringFunction()).iterator();

			while (iterator.hasNext()) {
				String s1 = (String) iterator.next();

				if (a(s, s1)) {
					arraylist.add(s1);
				}
			}

			if (arraylist.isEmpty()) {
				iterator = collection.iterator();

				while (iterator.hasNext()) {
					Object object = iterator.next();

					if (object instanceof MinecraftKey && a(s, ((MinecraftKey) object).getKey())) {
						arraylist.add(String.valueOf(object));
					}
				}
			}
		}

		return arraylist;
	}

	public boolean isListStart(String[] astring, int i) {
		return false;
	}

	public static void a(ICommandListener icommandlistener, ICommand icommand, String s, Object... aobject) {
		a(icommandlistener, icommand, 0, s, aobject);
	}

	public static void a(ICommandListener icommandlistener, ICommand icommand, int i, String s, Object... aobject) {
		if (CommandAbstract.a != null) {
			CommandProcessor.manager.handleError(s, new ChatMessage(s, aobject).getText());
		}

	}

	public static void a(ICommandDispatcher icommanddispatcher) {
		CommandAbstract.a = icommanddispatcher;
	}

	public int a(ICommand icommand) {
		return this.getCommand().compareTo(icommand.getCommand());
	}

	public int compareTo(ICommand object) {
		return this.a(object);
	}

	public static class CommandNumber {

		private final double a;
		private final double b;
		private final boolean c;

		protected CommandNumber(double d0, double d1, boolean flag) {
			this.a = d0;
			this.b = d1;
			this.c = flag;
		}

		public double a() {
			return this.a;
		}

		public double b() {
			return this.b;
		}

		public boolean c() {
			return this.c;
		}
	}

	public static Class<?> inject() {
		return CommandAbstract.class;
	}
}