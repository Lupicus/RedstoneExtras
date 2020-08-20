package com.lupicus.rsx.block;

import com.lupicus.rsx.tileentity.DaytimeSensorTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class DaytimeSensorBlock extends DaylightDetectorBlock
{
	private static long time;
	private static RegistryKey<World> dim;
	private static int skylightSubtracted;

	public DaytimeSensorBlock(Properties properties) {
		super(properties);
	}

	public static void updatePower(BlockState state, World world, BlockPos pos) {
		long time = world.getDayTime();
		RegistryKey<World> curdim = world.func_234923_W_();
		if (time != DaytimeSensorBlock.time || curdim != dim)
		{
			DaytimeSensorBlock.time = time;
			dim = curdim;
			// calculateInitialSkylight without rain and thunder
			double d2 = 0.5D + 2.0D * MathHelper
					.clamp((double) MathHelper.cos(world.func_242415_f(1.0F) * ((float) Math.PI * 2F)), -0.25D, 0.25D); // getCelestialAngle
			skylightSubtracted = (int) ((1.0D - d2) * 11.0D);
		}
		int i = (skylightSubtracted < 4) ? 15 : 0;
		if (state.get(INVERTED)) {
			i = 15 - i;
		}
		if (state.get(POWER) != i) {
			world.setBlockState(pos, state.with(POWER, Integer.valueOf(i)), 3);
		}
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {
		if (player.isAllowEdit()) {
			if (worldIn.isRemote) {
				return ActionResultType.SUCCESS;
			} else {
				BlockState blockstate = state.func_235896_a_(INVERTED); // cycle
				worldIn.setBlockState(pos, blockstate, 4);
				updatePower(blockstate, worldIn, pos);
				return ActionResultType.CONSUME;
			}
		} else {
			return super.onBlockActivated(state, worldIn, pos, player, handIn, result);
		}
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new DaytimeSensorTileEntity();
	}
}
