package com.lupicus.rsx.block;

import com.lupicus.rsx.tileentity.RedstoneEnergyTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class RedstoneEnergyBlock extends ContainerBlock
{
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;

	public RedstoneEnergyBlock(Properties properties) {
		super(properties);
		this.setDefaultState(
				this.stateContainer.getBaseState().with(POWER, Integer.valueOf(0)));
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		updateState(worldIn, pos, state);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		updateState(worldIn, pos, state);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof RedstoneEnergyTileEntity)
			((RedstoneEnergyTileEntity) te).neighborChanged();
	}

	protected void updateState(World worldIn, BlockPos pos, BlockState state)
	{
		int i = worldIn.getStrongPower(pos);
		int j = state.get(POWER);
		if (i != j)
		{
			state = state.with(POWER, Integer.valueOf(i));
			worldIn.setBlockState(pos, state, 2);
		}
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(POWER);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new RedstoneEnergyTileEntity();
	}
}
