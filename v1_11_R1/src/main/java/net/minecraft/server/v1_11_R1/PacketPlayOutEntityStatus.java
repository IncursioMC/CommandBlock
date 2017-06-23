package net.minecraft.server.v1_10_R1;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.commons.lang3.exception.ExceptionUtils;

public abstract class CommandAbstract implements ICommand {
	private static ICommandDispatcher a;

	protected static ExceptionInvalidSyntax a(JsonParseException paramJsonParseException) {
		Throwable localThrowable = ExceptionUtils.getRootCause(paramJsonParseException);
		String str = "";
		if (localThrowable != null) {
			str = localThrowable.getMessage();
			if (str.contains("setLenient")) {
				str = str.substring(str.indexOf("to accept ") + 10);
			}
		}
		return new ExceptionInvalidSyntax("commands.tellraw.jsonException", new Object[] { str });
	}

	protected static NBTTagCompound a(Entity paramEntity) {
		NBTTagCompound localNBTTagCompound = paramEntity.e(new NBTTagCompound());
		if ((paramEntity instanceof EntityHuman)) {
			ItemStack localItemStack = ((EntityHuman) paramEntity).inventory.getItemInHand();
			if ((localItemStack != null) && (localItemStack.getItem() != null)) {
				localNBTTagCompound.set("SelectedItem", localItemStack.save(new NBTTagCompound()));
			}
		}
		return localNBTTagCompound;
	}

	public int a() {
		return 4;
	}

	public List<String> b() {
		return Collections.emptyList();
	}

	public boolean canUse(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener) {
		return paramICommandListener.a(a(), getCommand());
	}

	public List<String> tabComplete(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener,
			String[] paramArrayOfString, @Nullable BlockPosition paramBlockPosition) {
		return Collections.emptyList();
	}

	public static int a(String paramString) throws ExceptionInvalidNumber {
		try {
			return Integer.parseInt(paramString);
		} catch (NumberFormatException localNumberFormatException) {
			throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
		}
	}

	public static int a(String paramString, int paramInt) throws ExceptionInvalidNumber {
		return a(paramString, paramInt, Integer.MAX_VALUE);
	}

	public static int a(String paramString, int paramInt1, int paramInt2) throws ExceptionInvalidNumber {
		int i = a(paramString);
		if (i < paramInt1) {
			throw new ExceptionInvalidNumber("commands.generic.num.tooSmall",
					new Object[] { Integer.valueOf(i), Integer.valueOf(paramInt1) });
		}
		if (i > paramInt2) {
			throw new ExceptionInvalidNumber("commands.generic.num.tooBig",
					new Object[] { Integer.valueOf(i), Integer.valueOf(paramInt2) });
		}
		return i;
	}

	public static long b(String paramString) throws ExceptionInvalidNumber {
		try {
			return Long.parseLong(paramString);
		} catch (NumberFormatException localNumberFormatException) {
			throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
		}
	}

	public static long a(String paramString, long paramLong1, long paramLong2) throws ExceptionInvalidNumber {
		long l = b(paramString);
		if (l < paramLong1) {
			throw new ExceptionInvalidNumber("commands.generic.num.tooSmall",
					new Object[] { Long.valueOf(l), Long.valueOf(paramLong1) });
		}
		if (l > paramLong2) {
			throw new ExceptionInvalidNumber("commands.generic.num.tooBig",
					new Object[] { Long.valueOf(l), Long.valueOf(paramLong2) });
		}
		return l;
	}

	public static BlockPosition a(ICommandListener paramICommandListener, String[] paramArrayOfString, int paramInt,
			boolean paramBoolean) throws ExceptionInvalidNumber {
		BlockPosition localBlockPosition = paramICommandListener.getChunkCoordinates();
		return new BlockPosition(
				b(localBlockPosition.getX(), paramArrayOfString[paramInt], -30000000, 30000000, paramBoolean),
				b(localBlockPosition.getY(), paramArrayOfString[(paramInt + 1)], 0, 256, false),
				b(localBlockPosition.getZ(), paramArrayOfString[(paramInt + 2)], -30000000, 30000000, paramBoolean));
	}

	public static double c(String paramString) throws ExceptionInvalidNumber {
		try {
			double d = Double.parseDouble(paramString);
			if (!Doubles.isFinite(d)) {
				throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
			}
			return d;
		} catch (NumberFormatException localNumberFormatException) {
			throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
		}
	}

	public static double a(String paramString, double paramDouble) throws ExceptionInvalidNumber {
		return a(paramString, paramDouble, Double.MAX_VALUE);
	}

	public static double a(String paramString, double paramDouble1, double paramDouble2) throws ExceptionInvalidNumber {
		double d = c(paramString);
		if (d < paramDouble1) {
			throw new ExceptionInvalidNumber("commands.generic.double.tooSmall",
					new Object[] { Double.valueOf(d), Double.valueOf(paramDouble1) });
		}
		if (d > paramDouble2) {
			throw new ExceptionInvalidNumber("commands.generic.double.tooBig",
					new Object[] { Double.valueOf(d), Double.valueOf(paramDouble2) });
		}
		return d;
	}

	public static boolean d(String paramString) throws CommandException {
		if (("true".equals(paramString)) || ("1".equals(paramString))) {
			return true;
		}
		if (("false".equals(paramString)) || ("0".equals(paramString))) {
			return false;
		}
		throw new CommandException("commands.generic.boolean.invalid", new Object[] { paramString });
	}

	public static EntityPlayer a(ICommandListener paramICommandListener) throws ExceptionPlayerNotFound {
		if ((paramICommandListener instanceof EntityPlayer)) {
			return (EntityPlayer) paramICommandListener;
		}
		throw new ExceptionPlayerNotFound("You must specify which player you wish to perform this action on.",
				new Object[0]);
	}

	public static EntityPlayer a(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener,
			String paramString) throws ExceptionPlayerNotFound {
		EntityPlayer localEntityPlayer = PlayerSelector.getPlayer(paramICommandListener, paramString);
		if (localEntityPlayer == null) {
			try {
				localEntityPlayer = paramMinecraftServer.getPlayerList().a(UUID.fromString(paramString));
			} catch (IllegalArgumentException localIllegalArgumentException) {
			}
		}
		if (localEntityPlayer == null) {
			localEntityPlayer = paramMinecraftServer.getPlayerList().getPlayer(paramString);
		}
		if (localEntityPlayer == null) {
			throw new ExceptionPlayerNotFound();
		}
		return localEntityPlayer;
	}

	public static Entity b(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener,
			String paramString) throws ExceptionEntityNotFound {
		return a(paramMinecraftServer, paramICommandListener, paramString, Entity.class);
	}

	public static <T extends Entity> T a(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener,
			String paramString, Class<? extends T> paramClass) throws ExceptionEntityNotFound {
		Object localObject = PlayerSelector.getEntity(paramICommandListener, paramString, paramClass);
		if (localObject == null) {
			localObject = paramMinecraftServer.getPlayerList().getPlayer(paramString);
		}
		if (localObject == null) {
			try {
				UUID localUUID = UUID.fromString(paramString);
				localObject = paramMinecraftServer.a(localUUID);
				if (localObject == null) {
					localObject = paramMinecraftServer.getPlayerList().a(localUUID);
				}
			} catch (IllegalArgumentException localIllegalArgumentException) {
				throw new ExceptionEntityNotFound("commands.generic.entity.invalidUuid", new Object[0]);
			}
		}
		if ((localObject == null) || (!paramClass.isAssignableFrom(localObject.getClass()))) {
			throw new ExceptionEntityNotFound();
		}
		return (T) localObject;
	}

	public static List<Entity> c(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener,
			String paramString) throws ExceptionEntityNotFound {
		if (PlayerSelector.isPattern(paramString)) {
			return PlayerSelector.getPlayers(paramICommandListener, paramString, Entity.class);
		}
		return Lists.newArrayList(new Entity[] { b(paramMinecraftServer, paramICommandListener, paramString) });
	}

	public static String d(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener,
			String paramString) throws ExceptionPlayerNotFound {
		try {
			return a(paramMinecraftServer, paramICommandListener, paramString).getName();
		} catch (ExceptionPlayerNotFound localExceptionPlayerNotFound) {
			if ((paramString == null) || (paramString.startsWith("@"))) {
				throw localExceptionPlayerNotFound;
			}
		}
		return paramString;
	}

	public static String e(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener,
			String paramString) throws ExceptionEntityNotFound {
		try {
			return a(paramMinecraftServer, paramICommandListener, paramString).getName();
		} catch (ExceptionPlayerNotFound localExceptionPlayerNotFound) {
			try {
				return b(paramMinecraftServer, paramICommandListener, paramString).bf();
			} catch (ExceptionEntityNotFound localExceptionEntityNotFound) {
				if ((paramString == null) || (paramString.startsWith("@"))) {
					throw localExceptionEntityNotFound;
				}
			}
		}
		return paramString;
	}

	public static IChatBaseComponent a(ICommandListener paramICommandListener, String[] paramArrayOfString,
			int paramInt) throws ExceptionPlayerNotFound {
		return b(paramICommandListener, paramArrayOfString, paramInt, false);
	}

	public static IChatBaseComponent b(ICommandListener paramICommandListener, String[] paramArrayOfString,
			int paramInt, boolean paramBoolean) throws ExceptionPlayerNotFound {
		ChatComponentText localChatComponentText = new ChatComponentText("");
		for (int i = paramInt; i < paramArrayOfString.length; i++) {
			if (i > paramInt) {
				localChatComponentText.a(" ");
			}
			Object localObject = new ChatComponentText(paramArrayOfString[i]);
			if (paramBoolean) {
				IChatBaseComponent localIChatBaseComponent = PlayerSelector.getPlayerNames(paramICommandListener,
						paramArrayOfString[i]);
				if (localIChatBaseComponent == null) {
					if (PlayerSelector.isPattern(paramArrayOfString[i])) {
						throw new ExceptionPlayerNotFound();
					}
				} else {
					localObject = localIChatBaseComponent;
				}
			}
			localChatComponentText.addSibling((IChatBaseComponent) localObject);
		}
		return localChatComponentText;
	}

	public static String a(String[] paramArrayOfString, int paramInt) {
		StringBuilder localStringBuilder = new StringBuilder();
		for (int i = paramInt; i < paramArrayOfString.length; i++) {
			if (i > paramInt) {
				localStringBuilder.append(" ");
			}
			String str = paramArrayOfString[i];

			localStringBuilder.append(str);
		}
		return localStringBuilder.toString();
	}

	public static CommandNumber a(double paramDouble, String paramString, boolean paramBoolean)
			throws ExceptionInvalidNumber {
		return a(paramDouble, paramString, -30000000, 30000000, paramBoolean);
	}

	public static CommandNumber a(double paramDouble, String paramString, int paramInt1, int paramInt2,
			boolean paramBoolean) throws ExceptionInvalidNumber {
		boolean bool1 = paramString.startsWith("~");
		if ((bool1) && (Double.isNaN(paramDouble))) {
			throw new ExceptionInvalidNumber("commands.generic.num.invalid",
					new Object[] { Double.valueOf(paramDouble) });
		}
		double d1 = 0.0D;
		if ((!bool1) || (paramString.length() > 1)) {
			boolean bool2 = paramString.contains(".");
			if (bool1) {
				paramString = paramString.substring(1);
			}
			d1 += c(paramString);
			if ((!bool2) && (!bool1) && (paramBoolean)) {
				d1 += 0.5D;
			}
		}
		double d2 = d1 + (bool1 ? paramDouble : 0.0D);
		if ((paramInt1 != 0) || (paramInt2 != 0)) {
			if (d2 < paramInt1) {
				throw new ExceptionInvalidNumber("commands.generic.double.tooSmall",
						new Object[] { Double.valueOf(d2), Integer.valueOf(paramInt1) });
			}
			if (d2 > paramInt2) {
				throw new ExceptionInvalidNumber("commands.generic.double.tooBig",
						new Object[] { Double.valueOf(d2), Integer.valueOf(paramInt2) });
			}
		}
		return new CommandNumber(d2, d1, bool1);
	}

	public static double b(double paramDouble, String paramString, boolean paramBoolean) throws ExceptionInvalidNumber {
		return b(paramDouble, paramString, -30000000, 30000000, paramBoolean);
	}

	public static double b(double paramDouble, String paramString, int paramInt1, int paramInt2, boolean paramBoolean)
			throws ExceptionInvalidNumber {
		boolean bool1 = paramString.startsWith("~");
		if ((bool1) && (Double.isNaN(paramDouble))) {
			throw new ExceptionInvalidNumber("commands.generic.num.invalid",
					new Object[] { Double.valueOf(paramDouble) });
		}
		double d = bool1 ? paramDouble : 0.0D;
		if ((!bool1) || (paramString.length() > 1)) {
			boolean bool2 = paramString.contains(".");
			if (bool1) {
				paramString = paramString.substring(1);
			}
			d += c(paramString);
			if ((!bool2) && (!bool1) && (paramBoolean)) {
				d += 0.5D;
			}
		}
		if ((paramInt1 != 0) || (paramInt2 != 0)) {
			if (d < paramInt1) {
				throw new ExceptionInvalidNumber("commands.generic.double.tooSmall",
						new Object[] { Double.valueOf(d), Integer.valueOf(paramInt1) });
			}
			if (d > paramInt2) {
				throw new ExceptionInvalidNumber("commands.generic.double.tooBig",
						new Object[] { Double.valueOf(d), Integer.valueOf(paramInt2) });
			}
		}
		return d;
	}

	public static class CommandNumber {
		private final double a;
		private final double b;
		private final boolean c;

		protected CommandNumber(double paramDouble1, double paramDouble2, boolean paramBoolean) {
			this.a = paramDouble1;
			this.b = paramDouble2;
			this.c = paramBoolean;
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

	public static Item a(ICommandListener paramICommandListener, String paramString) throws ExceptionInvalidNumber {
		MinecraftKey localMinecraftKey = new MinecraftKey(paramString);
		Item localItem = (Item) Item.REGISTRY.get(localMinecraftKey);
		if (localItem == null) {
			throw new ExceptionInvalidNumber("commands.give.item.notFound", new Object[] { localMinecraftKey });
		}
		return localItem;
	}

	public static Block b(ICommandListener paramICommandListener, String paramString) throws ExceptionInvalidNumber {
		MinecraftKey localMinecraftKey = new MinecraftKey(paramString);
		if (!Block.REGISTRY.d(localMinecraftKey)) {
			throw new ExceptionInvalidNumber("commands.give.block.notFound", new Object[] { localMinecraftKey });
		}
		Block localBlock = (Block) Block.REGISTRY.get(localMinecraftKey);
		if (localBlock == null) {
			throw new ExceptionInvalidNumber("commands.give.block.notFound", new Object[] { localMinecraftKey });
		}
		return localBlock;
	}

	public static String a(Object[] paramArrayOfObject) {
		StringBuilder localStringBuilder = new StringBuilder();
		for (int i = 0; i < paramArrayOfObject.length; i++) {
			String str = paramArrayOfObject[i].toString();
			if (i > 0) {
				if (i == paramArrayOfObject.length - 1) {
					localStringBuilder.append(" and ");
				} else {
					localStringBuilder.append(", ");
				}
			}
			localStringBuilder.append(str);
		}
		return localStringBuilder.toString();
	}

	public static IChatBaseComponent a(List<IChatBaseComponent> paramList) {
		ChatComponentText localChatComponentText = new ChatComponentText("");
		for (int i = 0; i < paramList.size(); i++) {
			if (i > 0) {
				if (i == paramList.size() - 1) {
					localChatComponentText.a(" and ");
				} else if (i > 0) {
					localChatComponentText.a(", ");
				}
			}
			localChatComponentText.addSibling((IChatBaseComponent) paramList.get(i));
		}
		return localChatComponentText;
	}

	public static String a(Collection<String> paramCollection) {
		return a(paramCollection.toArray(new String[paramCollection.size()]));
	}

	public static List<String> a(String[] paramArrayOfString, int paramInt,
			@Nullable BlockPosition paramBlockPosition) {
		if (paramBlockPosition == null) {
			return Lists.newArrayList(new String[] { "~" });
		}
		int i = paramArrayOfString.length - 1;
		String str;
		if (i == paramInt) {
			str = Integer.toString(paramBlockPosition.getX());
		} else if (i == paramInt + 1) {
			str = Integer.toString(paramBlockPosition.getY());
		} else if (i == paramInt + 2) {
			str = Integer.toString(paramBlockPosition.getZ());
		} else {
			return Collections.emptyList();
		}
		return Lists.newArrayList(new String[] { str });
	}

	@Nullable
	public static List<String> b(String[] paramArrayOfString, int paramInt,
			@Nullable BlockPosition paramBlockPosition) {
		if (paramBlockPosition == null) {
			return Lists.newArrayList(new String[] { "~" });
		}
		int i = paramArrayOfString.length - 1;
		String str;
		if (i == paramInt) {
			str = Integer.toString(paramBlockPosition.getX());
		} else if (i == paramInt + 1) {
			str = Integer.toString(paramBlockPosition.getZ());
		} else {
			return null;
		}
		return Lists.newArrayList(new String[] { str });
	}

	public static boolean a(String paramString1, String paramString2) {
		return paramString2.regionMatches(true, 0, paramString1, 0, paramString1.length());
	}

	public static List<String> a(String[] paramArrayOfString1, String... paramVarArgs) {
		return a(paramArrayOfString1, Arrays.asList(paramVarArgs));
	}

	public static List<String> a(String[] paramArrayOfString, Collection<?> paramCollection) {
		String str = paramArrayOfString[(paramArrayOfString.length - 1)];
		ArrayList localArrayList = Lists.newArrayList();
		Iterator localIterator;
		Object localObject;
		if (!paramCollection.isEmpty()) {
			for (localIterator = Iterables.transform(paramCollection, Functions.toStringFunction())
					.iterator(); localIterator.hasNext();) {
				localObject = (String) localIterator.next();
				if (a(str, (String) localObject)) {
					localArrayList.add(localObject);
				}
			}
			if (localArrayList.isEmpty()) {
				for (localIterator = paramCollection.iterator(); localIterator.hasNext();) {
					localObject = localIterator.next();
					if (((localObject instanceof MinecraftKey)) && (a(str, ((MinecraftKey) localObject).a()))) {
						localArrayList.add(String.valueOf(localObject));
					}
				}
			}
		}
		return localArrayList;
	}

	public boolean isListStart(String[] paramArrayOfString, int paramInt) {
		return false;
	}

	public static void a(ICommandListener paramICommandListener, ICommand paramICommand, String paramString,
			Object... paramVarArgs) {
		a(paramICommandListener, paramICommand, 0, paramString, paramVarArgs);
	}

	public static void a(ICommandListener paramICommandListener, ICommand paramICommand, int paramInt,
			String paramString, Object... paramVarArgs) {
		if (a != null) {
			a.a(paramICommandListener, paramICommand, paramInt, paramString, paramVarArgs);
		}
	}

	public static void a(ICommandDispatcher paramICommandDispatcher) {
		a = paramICommandDispatcher;
	}

	public int a(ICommand paramICommand) {
		return getCommand().compareTo(paramICommand.getCommand());
	}
}
