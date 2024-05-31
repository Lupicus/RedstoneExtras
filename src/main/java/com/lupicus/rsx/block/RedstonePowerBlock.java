package com.lupicus.rsx.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstonePowerBlock extends Block
{
	public static final MapCodec<RedstonePowerBlock> CODEC = simpleCodec(RedstonePowerBlock::new);
	public static final IntegerProperty POWER = BlockStateProperties.POWER;

	@Override
	protected MapCodec<RedstonePowerBlock> codec() {
		return CODEC;
	}

	public RedstonePowerBlock(Properties properties) {
		super(properties);
		registerDefaultState(
				stateDefinition.any()
				.setValue(POWER, Integer.valueOf(15)));
	}

	public static boolean isNormalCube(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return false;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		if (!player.mayBuild()) {
			return InteractionResult.PASS;
		} else {
			int i = state.getValue(POWER);
			if (player.isSecondaryUseActive()) {
				i = (i > 0) ? i - 1 : 15; 
			}
			else {
				i = (i < 15) ? i + 1 : 0;
			}
			worldIn.setBlock(pos, state.setValue(POWER, Integer.valueOf(i)), 3);
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return getSignal(blockState, blockAccess, pos, side);
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return blockState.getValue(POWER);
	}

	@OnlyIn(Dist.CLIENT)
	public static int getColorForPower(int power) {
		float f = (float) power / 15.0F;
		float f1 = f * 0.6F + 0.4F;
		if (power == 0) {
			f1 = 0.3F;
		}

		float f2 = f * f * 0.7F - 0.5F;
		float f3 = f * f * 0.6F - 0.7F;

		int i = Mth.clamp((int) (f1 * 255.0F), 0, 255);
		int j = Mth.clamp((int) (f2 * 255.0F), 0, 255);
		int k = Mth.clamp((int) (f3 * 255.0F), 0, 255);
		return -16777216 | i << 16 | j << 8 | k;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWER);
	}
}
