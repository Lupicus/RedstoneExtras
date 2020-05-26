package com.lupicus.rsx.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstonePowerBlock extends Block
{
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;

	public RedstonePowerBlock(Properties properties) {
		super(properties);
		this.setDefaultState(
				this.stateContainer.getBaseState()
				.with(POWER, Integer.valueOf(15)));
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult p_225533_6_) {
		if (!player.abilities.allowEdit) {
			return ActionResultType.PASS;
		} else {
			int i = state.get(POWER);
			if (player.isShiftKeyDown()) {
				i = (i > 0) ? i - 1 : 15; 
			}
			else {
				i = (i < 15) ? i + 1 : 0;
			}
			worldIn.setBlockState(pos, state.with(POWER, Integer.valueOf(i)), 3);
			return ActionResultType.SUCCESS;
		}
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return getWeakPower(blockState, blockAccess, pos, side);
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.get(POWER);
	}

	@OnlyIn(Dist.CLIENT)
	public static int colorMultiplier(int power) {
		float f = (float) power / 15.0F;
		float f1 = f * 0.6F + 0.4F;
		if (power == 0) {
			f1 = 0.3F;
		}

		float f2 = f * f * 0.7F - 0.5F;
		float f3 = f * f * 0.6F - 0.7F;

		int i = MathHelper.clamp((int) (f1 * 255.0F), 0, 255);
		int j = MathHelper.clamp((int) (f2 * 255.0F), 0, 255);
		int k = MathHelper.clamp((int) (f3 * 255.0F), 0, 255);
		return -16777216 | i << 16 | j << 8 | k;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWER);
	}
}
