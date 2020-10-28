package com.lupicus.rsx.block;

import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class RedstoneStraightBlock extends RedstoneBenderBlock
{
	protected RedstoneStraightBlock(Properties builder) {
		super(builder);
	}

	@Override
	protected void getSides(BlockState state, Set<Direction> set)
	{
		Direction facing = state.get(HORIZONTAL_FACING);
		set.add(facing);
		set.add(facing.getOpposite());
	}

	@Override
	protected void notifyNeighbors(World worldIn, BlockPos pos, BlockState state)
	{
	    Direction facing = state.get(HORIZONTAL_FACING);
	    notifyNeighbors(worldIn, pos, state, facing);
	    notifyNeighbors(worldIn, pos, state, facing.getOpposite());
	}

	@Override
	protected int calculateInputStrength(World world, BlockPos pos, BlockState state)
	{
		Direction direction = state.get(HORIZONTAL_FACING);
		BlockPos blockpos = pos.offset(direction);
		int i = world.getRedstonePower(blockpos, direction);
		if (i >= 15)
			return 15;
		Direction direction2 = direction.getOpposite();
		BlockPos blockpos2 = pos.offset(direction2);
		int i2 = world.getRedstonePower(blockpos2, direction2);
		if (i2 >= 15)
			return 15;

		// get wire power in case it is turning from side
		if (i == 0)
		{
			BlockState blockstate = world.getBlockState(blockpos);
			if (blockstate.getBlock() == Blocks.REDSTONE_WIRE)
				i = blockstate.get(RedstoneWireBlock.POWER);
		}
		if (i2 == 0)
		{
			BlockState blockstate = world.getBlockState(blockpos2);
			if (blockstate.getBlock() == Blocks.REDSTONE_WIRE)
				i2 = blockstate.get(RedstoneWireBlock.POWER);
		}
		if (i < i2)
			i = i2;
		return i;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		Direction facing = state.get(HORIZONTAL_FACING);
		return facing == side || facing.getOpposite() == side;
	}
}
