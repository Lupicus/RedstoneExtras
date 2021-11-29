package com.lupicus.rsx.block;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneTeeBlock extends RedstoneBenderBlock
{
	protected RedstoneTeeBlock(Properties builder) {
		super(builder);
	}

	@Override
	protected void getSides(BlockState state, Set<Direction> set)
	{
		Direction facing = state.getValue(FACING);
		set.add(facing);
		set.add(facing.getCounterClockWise());
		set.add(facing.getClockWise());
	}

	@Override
	protected void notifyNeighbors(Level worldIn, BlockPos pos, BlockState state)
	{
	    Direction facing = state.getValue(FACING);
	    notifyNeighbors(worldIn, pos, state, facing);
	    notifyNeighbors(worldIn, pos, state, facing.getCounterClockWise());
	    notifyNeighbors(worldIn, pos, state, facing.getClockWise());
	}

	@Override
	protected int calculateInputStrength(Level world, BlockPos pos, BlockState state)
	{
		Direction direction = state.getValue(FACING);
		BlockPos blockpos = pos.relative(direction);
		int i = world.getSignal(blockpos, direction);
		if (i >= 15)
			return 15;
		Direction direction2 = direction.getCounterClockWise();
		BlockPos blockpos2 = pos.relative(direction2);
		int i2 = world.getSignal(blockpos2, direction2);
		if (i2 >= 15)
			return 15;
		direction2 = direction.getClockWise();
		BlockPos blockpos3 = pos.relative(direction2);
		int i3 = world.getSignal(blockpos3, direction2);
		if (i3 >= 15)
			return 15;

		// get wire power in case it is turning from side
		if (i == 0)
		{
			BlockState blockstate = world.getBlockState(blockpos);
			if (blockstate.getBlock() == Blocks.REDSTONE_WIRE)
				i = blockstate.getValue(RedStoneWireBlock.POWER);
		}
		if (i2 == 0)
		{
			BlockState blockstate = world.getBlockState(blockpos2);
			if (blockstate.getBlock() == Blocks.REDSTONE_WIRE)
				i2 = blockstate.getValue(RedStoneWireBlock.POWER);
		}
		if (i3 == 0)
		{
			BlockState blockstate = world.getBlockState(blockpos3);
			if (blockstate.getBlock() == Blocks.REDSTONE_WIRE)
				i3 = blockstate.getValue(RedStoneWireBlock.POWER);
		}
		if (i < i2)
			i = i2;
		if (i < i3)
			i = i3;
		return i;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		Direction facing = state.getValue(FACING).getOpposite();
		return facing == side || facing.getCounterClockWise() == side || facing.getClockWise() == side;
	}
}
