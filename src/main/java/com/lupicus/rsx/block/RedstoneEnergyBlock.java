package com.lupicus.rsx.block;

import javax.annotation.Nullable;

import com.lupicus.rsx.tileentity.ModTileEntities;
import com.lupicus.rsx.tileentity.RedstoneEnergyTileEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;

public class RedstoneEnergyBlock extends BaseEntityBlock
{
	public static final MapCodec<RedstoneEnergyBlock> CODEC = simpleCodec(RedstoneEnergyBlock::new);
	public static final IntegerProperty POWER = BlockStateProperties.POWER;

	@Override
	protected MapCodec<RedstoneEnergyBlock> codec() {
		return CODEC;
	}

	public RedstoneEnergyBlock(Properties properties) {
		super(properties);
		registerDefaultState(
				stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
	}

	public static boolean isNormalCube(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return false;
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		updateState(worldIn, pos, state);
	}

	@Override
	protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, Orientation orient,
			boolean isMoving) {
		updateState(worldIn, pos, state);
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te instanceof RedstoneEnergyTileEntity)
			((RedstoneEnergyTileEntity) te).neighborChanged();
	}

	protected void updateState(Level worldIn, BlockPos pos, BlockState state)
	{
		int i = worldIn.getDirectSignalTo(pos);
		int j = state.getValue(POWER);
		if (i != j)
		{
			state = state.setValue(POWER, Integer.valueOf(i));
			worldIn.setBlock(pos, state, 2);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWER);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RedstoneEnergyTileEntity(pos, state);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		BlockEntityTicker<RedstoneEnergyTileEntity> func = RedstoneEnergyBlock::tickEntity;
		return !world.isClientSide && type == ModTileEntities.REDSTONE_ENERGY_BLOCK ? (BlockEntityTicker<T>)func : null;
	}

	private static void tickEntity(Level world, BlockPos pos, BlockState state, RedstoneEnergyTileEntity blockEntity) {
		blockEntity.serverTick();
	}
}
