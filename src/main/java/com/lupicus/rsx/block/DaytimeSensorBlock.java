package com.lupicus.rsx.block;

import com.lupicus.rsx.tileentity.DaytimeSensorTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;

public class DaytimeSensorBlock extends DaylightDetectorBlock
{
	private static long time;
	private static Dimension dim;
	private static int skylightSubtracted;

	public DaytimeSensorBlock(Properties properties) {
		super(properties);
	}

	public static void updatePower(BlockState state, World world, BlockPos pos) {
		long time = world.getDayTime();
		if (time != DaytimeSensorBlock.time || world.dimension != dim)
		{
			DaytimeSensorBlock.time = time;
			dim = world.dimension;
			// calculateInitialSkylight without rain and thunder
			double d2 = 0.5D + 2.0D * MathHelper
					.clamp((double) MathHelper.cos(world.getCelestialAngle(1.0F) * ((float) Math.PI * 2F)), -0.25D, 0.25D);
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
				BlockState blockstate = state.cycle(INVERTED);
				worldIn.setBlockState(pos, blockstate, 4);
				updatePower(blockstate, worldIn, pos);
				return ActionResultType.SUCCESS;
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
