package com.lupicus.rsx.block;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneBenderBlock extends HorizontalBlock
{
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

	protected RedstoneBenderBlock(Properties builder) {
		super(builder);
		this.setDefaultState(
				this.stateContainer.getBaseState()
				.with(HORIZONTAL_FACING, Direction.NORTH)
				.with(POWER, Integer.valueOf(0)));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return hasSolidSideOnTop(worldIn, pos.down());
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {
		if (!player.abilities.allowEdit) {
			return ActionResultType.PASS;
		} else {
			HashSet<Direction> set = new HashSet<>();
			if (getActiveSignal(world, pos, state) > 0)
				getSides(state, set);
			Rotation rot = (player.isSneaking()) ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
			state = rotate(state, rot);
			world.setBlockState(pos, state, 0);
			int j = calculateInputStrength(world, pos, state);
			state = state.with(POWER, Integer.valueOf(j));
			world.setBlockState(pos, state, 2);
			getSides(state, set);
			notifyNeighbors(world, pos, state, set);
			return ActionResultType.SUCCESS;
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getBlock() != oldState.getBlock())
			updateState(worldIn, pos, state);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			super.onReplaced(state, worldIn, pos, newState, isMoving);
			if (getActiveSignal(worldIn, pos, state) > 0)
				notifyNeighbors(worldIn, pos, state);
		}
	}

	protected void getSides(BlockState state, Set<Direction> set)
	{
		Direction facing = state.get(HORIZONTAL_FACING);
		set.add(facing);
		set.add(facing.rotateYCCW());
	}

	protected void notifyNeighbors(World worldIn, BlockPos pos, BlockState state, Set<Direction> set)
	{
		set.forEach((facing) -> notifyNeighbors(worldIn, pos, state, facing));
	}

	protected void notifyNeighbors(World worldIn, BlockPos pos, BlockState state)
	{
	    Direction facing = state.get(HORIZONTAL_FACING);
	    notifyNeighbors(worldIn, pos, state, facing);
	    notifyNeighbors(worldIn, pos, state, facing.rotateYCCW());
	}

	protected void notifyNeighbors(World worldIn, BlockPos pos, BlockState state, Direction direction)
	{
		BlockPos blockpos = pos.offset(direction);
		if (net.minecraftforge.event.ForgeEventFactory
				.onNeighborNotify(worldIn, pos, worldIn.getBlockState(pos), java.util.EnumSet.of(direction), false)
				.isCanceled())
			return;
		worldIn.neighborChanged(blockpos, this, pos);
		worldIn.notifyNeighborsOfStateExcept(blockpos, this, direction.getOpposite());
	}

	protected void updateState(World world, BlockPos pos, BlockState state)
	{
		if (!world.isRemote)
		{
			int i = state.get(POWER);
			int j = calculateInputStrength(world, pos, state);
			if (i != j)
			{
				state = state.with(POWER, Integer.valueOf(j));
				world.setBlockState(pos, state, 2);
				notifyNeighbors(world, pos, state);
			}
		}
	}

	protected int calculateInputStrength(World world, BlockPos pos, BlockState state)
	{
		Direction direction = state.get(HORIZONTAL_FACING);
		BlockPos blockpos = pos.offset(direction);
		int i = world.getRedstonePower(blockpos, direction);
		if (i >= 15)
			return 15;
		Direction direction2 = direction.rotateYCCW();
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
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return getWeakPower(blockState, blockAccess, pos, side);
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return canConnectRedstone(blockState, blockAccess, pos, side) ? getActiveSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (state.isValidPosition(worldIn, pos)) {
			Direction from = Direction.byLong(pos.getX() - fromPos.getX(), pos.getY() - fromPos.getY(),
					pos.getZ() - fromPos.getZ());
			if (canConnectRedstone(state, worldIn, pos, from))
			{
				updateState(worldIn, pos, state);
			}
		} else {
			TileEntity tileentity = state.hasTileEntity() ? worldIn.getTileEntity(pos) : null;
			spawnDrops(state, worldIn, pos, tileentity);
			worldIn.removeBlock(pos, false);
		}
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}

	protected int getActiveSignal(IBlockReader worldIn, BlockPos pos, BlockState state) {
		int i = state.get(POWER);
		return (i > 0) ? i - 1 : 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		Direction facing = state.get(HORIZONTAL_FACING).getOpposite();
		return facing == side || facing.rotateYCCW() == side;
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

	/**
	 * Called periodically clientside on blocks near the player to show effects
	 * (like furnace fire particles). Note that this method is unrelated to
	 * {@link randomTick} and {@link #needsRandomTick}, and will always be called
	 * regardless of whether the block can receive random update ticks
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		int i = stateIn.get(POWER);
		if (i != 0) {
			double d0 = (double) pos.getX() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.2D;
			double d1 = (double) ((float) pos.getY() + 0.1875F);
			double d2 = (double) pos.getZ() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.2D;
			float f = (float) i / 15.0F;
			float f1 = f * 0.6F + 0.4F;
			float f2 = Math.max(0.0F, f * f * 0.7F - 0.5F);
			float f3 = Math.max(0.0F, f * f * 0.6F - 0.7F);
			worldIn.addParticle(new RedstoneParticleData(f1, f2, f3, 1.0F), d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, POWER);
	}
}
