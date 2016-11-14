package org.squiddev.plethora.gameplay.modules.methods;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.api.module.TargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.api.method.ArgumentHelper.getInt;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Scanner.radius;

public final class MethodsScanner {
	@IMethod.Inject(IModuleContainer.class)
	public static final class MethodScanBlocks extends TargetedModuleObjectMethod<IWorldLocation> {
		public MethodScanBlocks() {
			super("scan", PlethoraModules.SCANNER, IWorldLocation.class, true, "function() -- Scan all blocks in the vicinity");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IWorldLocation location, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
			final World world = location.getWorld();
			final BlockPos pos = location.getPos();
			final int x = pos.getX(), y = pos.getY(), z = pos.getZ();

			int i = 0;
			HashMap<Integer, Object> map = Maps.newHashMap();
			for (int oX = x - radius; oX <= x + radius; oX++) {
				for (int oY = y - radius; oY <= y + radius; oY++) {
					for (int oZ = z - radius; oZ <= z + radius; oZ++) {
						BlockPos newPos = new BlockPos(oX, oY, oZ);
						IBlockState block = world.getBlockState(newPos);

						HashMap<String, Object> data = Maps.newHashMap();
						data.put("x", oX - x);
						data.put("y", oY - y);
						data.put("z", oZ - z);
						String name = block.getBlock().getRegistryName();
						data.put("name", name == null ? "unknown" : name);

						map.put(++i, data);
					}
				}
			}

			return new Object[]{map};
		}
	}

	@TargetedModuleMethod.Inject(
		module = PlethoraModules.SCANNER_S,
		target = IWorldLocation.class,
		doc = "function(x:integer, y:integer, z:integer):table -- Get metadata about a nearby block"
	)
	@Nonnull
	public static MethodResult getBlockMeta(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final int x = getInt(args, 0);
		final int y = getInt(args, 1);
		final int z = getInt(args, 2);

		assertBetween(x, -radius, radius, "X coordinate out of bounds (%s)");
		assertBetween(y, -radius, radius, "Y coordinate out of bounds (%s)");
		assertBetween(z, -radius, radius, "Z coordinate out of bounds (%s)");

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IModuleContainer> baked = context.bake();
				IWorldLocation location = baked.getContext(IWorldLocation.class);
				BlockPos pos = location.getPos().add(x, y, z);
				World world = location.getWorld();

				IBlockState block = world.getBlockState(pos);
				Map<Object, Object> meta = baked.makePartialChild(block).getMeta();

				TileEntity te = world.getTileEntity(pos);
				if (te != null) {
					meta.putAll(baked.makePartialChild(te).getMeta());
				}

				return MethodResult.result(meta);
			}
		});
	}
}