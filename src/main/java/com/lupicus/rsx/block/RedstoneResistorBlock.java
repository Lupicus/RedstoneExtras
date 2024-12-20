package com.lupicus.rsx.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneResistorBlock extends DiodeBlock
{
	public static final MapCodec<RedstoneResistorBlock> CODEC = simpleCodec(RedstoneResistorBlock::new);
	public static final IntegerProperty RESISTANCE = IntegerProperty.create("resistance", 0, 15);
	public static final IntegerProperty POWER = BlockStateProperties.POWER;

	@Override
	protected MapCodec<RedstoneResistorBlock> codec() {
		return CODEC;
	}

	protected RedstoneResistorBlock(Properties builder) {
		super(builder);
		registerDefaultState(
				stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(RESISTANCE, 0)
				.setValue(POWER, 0));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
			BlockHitResult result) {
		if (!player.mayBuild()) {
			return InteractionResult.PASS;
		} else {
			int i = state.getValue(RESISTANCE);
			if (player.isSecondaryUseActive()) {
				i = (i > 0) ? i - 1 : 15;
			}
			else {
				i = (i < 15) ? i + 1 : 0;
			}
			world.setBlock(pos, state.setValue(RESISTANCE, Integer.valueOf(i)), 2);
			updateNeighborsInFront(world, pos, state);
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickAccess, BlockPos pos,
			Direction dir, BlockPos dirPos, BlockState dirState, RandomSource rand) {
		if (dir == Direction.DOWN)
			return !canSurviveOn(world, dirPos, dirState) ? Blocks.AIR.defaultBlockState() : state;
		return super.updateShape(state, world, tickAccess, pos, dir, dirPos, dirState, rand);
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		checkTickOnNeighbor(worldIn, pos, state);
	}

	@Override
	protected void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
	}

	@Override
	protected void checkTickOnNeighbor(Level world, BlockPos pos, BlockState state)
	{
		if (!world.isClientSide)
		{
			int i = state.getValue(POWER);
			int j = getInputSignal(world, pos, state);
			if (i != j)
			{
				state = state.setValue(POWER, Integer.valueOf(j));
				world.setBlock(pos, state, 3);
			}
		}
	}

	@Override
	protected int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return getSignal(blockState, blockAccess, pos, side);
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return blockState.getValue(FACING) == side ? this.getOutputSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected int getOutputSignal(BlockGetter worldIn, BlockPos pos, BlockState state) {
		int output = state.getValue(POWER) - state.getValue(RESISTANCE);
		return output <= 0 ? 0 : output;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		Direction facing = state.getValue(FACING);
		return facing == side || facing.getOpposite() == side;
	}

	@Override
	protected int getDelay(BlockState state) {
		return 0;
	}

	@OnlyIn(Dist.CLIENT)
	public static int getColorForResistance(int resistance) {
		return DyeColor.byId(resistance).getTextureDiffuseColor();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, RESISTANCE, POWER);
	}
}
