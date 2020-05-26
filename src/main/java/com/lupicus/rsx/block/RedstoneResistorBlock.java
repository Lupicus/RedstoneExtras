package com.lupicus.rsx.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneResistorBlock extends RedstoneDiodeBlock
{
	public static final IntegerProperty RESISTANCE = IntegerProperty.create("resistance", 0, 15);
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;

	protected RedstoneResistorBlock(Properties builder) {
		super(builder);
		this.setDefaultState(
				this.stateContainer.getBaseState()
				.with(HORIZONTAL_FACING, Direction.NORTH)
				.with(RESISTANCE, 0)
				.with(POWER, 0));
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult p_225533_6_) {
		if (!player.abilities.allowEdit) {
			return ActionResultType.PASS;
		} else {
			int i = state.get(RESISTANCE);
			if (player.isShiftKeyDown()) {
				i = (i > 0) ? i - 1 : 15;
			}
			else {
				i = (i < 15) ? i + 1 : 0;
			}
			world.setBlockState(pos, state.with(RESISTANCE, Integer.valueOf(i)), 2);
			notifyNeighbors(world, pos, state);
			return ActionResultType.SUCCESS;
		}
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		updateState(worldIn, pos, state);
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
	}

	@Override
	protected void updateState(World world, BlockPos pos, BlockState state)
	{
		if (!world.isRemote)
		{
			int i = state.get(POWER);
			int j = calculateInputStrength(world, pos, state);
			if (i != j)
			{
				state = state.with(POWER, Integer.valueOf(j));
				world.setBlockState(pos, state, 3);
			}
		}
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return getWeakPower(blockState, blockAccess, pos, side);
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.get(HORIZONTAL_FACING) == side ? this.getActiveSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected int getActiveSignal(IBlockReader worldIn, BlockPos pos, BlockState state) {
		int output = state.get(POWER) - state.get(RESISTANCE);
		return output <= 0 ? 0 : output;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		Direction facing = state.get(HORIZONTAL_FACING);
		return facing == side || facing.getOpposite() == side;
	}

	@Override
	protected int getDelay(BlockState state) {
		return 0;
	}

	@OnlyIn(Dist.CLIENT)
	public static int colorMultiplier(int resistance) {
		return DyeColor.byId(resistance).getColorValue();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, RESISTANCE, POWER);
	}
}
