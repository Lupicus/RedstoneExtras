package com.lupicus.rsx.block;

import java.util.HashSet;
import java.util.Set;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneBenderBlock extends HorizontalDirectionalBlock
{
	public static final MapCodec<RedstoneBenderBlock> CODEC = simpleCodec(RedstoneBenderBlock::new);
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
	private static final int[] COLORS = RedstonePipeBlock.COLORS;

	@Override
	protected MapCodec<? extends RedstoneBenderBlock> codec() {
		return CODEC;
	}

	protected RedstoneBenderBlock(Properties builder) {
		super(builder);
		registerDefaultState(
				stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(POWER, Integer.valueOf(0)));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		BlockPos blockpos = pos.below();
		return this.canSurviveOn(worldIn, blockpos, worldIn.getBlockState(blockpos));
	}

	protected boolean canSurviveOn(LevelReader world, BlockPos pos, BlockState state) {
		return state.isFaceSturdy(world, pos, Direction.UP, SupportType.RIGID);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
			BlockHitResult result) {
		if (!player.mayBuild()) {
			return InteractionResult.PASS;
		} else {
			HashSet<Direction> set = new HashSet<>();
			if (getActiveSignal(world, pos, state) > 0)
				getSides(state, set);
			Rotation rot = (player.isSecondaryUseActive()) ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
			state = rotate(state, rot);
			world.setBlock(pos, state, 3);
			int j = calculateInputStrength(world, pos, state);
			state = state.setValue(POWER, Integer.valueOf(j));
			world.setBlock(pos, state, 2);
			if (j > 1)
				getSides(state, set);
			notifyNeighbors(world, pos, state, set);
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickAccess, BlockPos pos,
			Direction dir, BlockPos dirPos, BlockState dirState, RandomSource rand) {
		if (dir == Direction.DOWN)
			return !canSurviveOn(world, dirPos, dirState) ? Blocks.AIR.defaultBlockState() : state;
		return super.updateShape(state, world, tickAccess, pos, dir, dirPos, dirState, rand);
	}

	@Override
	protected void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getBlock() != oldState.getBlock())
			updateState(worldIn, pos, state);
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel worldIn, BlockPos pos, boolean isMoving) {
		if (getActiveSignal(worldIn, pos, state) > 0)
			notifyNeighbors(worldIn, pos, state);
	}

	protected void getSides(BlockState state, Set<Direction> set)
	{
		Direction facing = state.getValue(FACING);
		set.add(facing);
		set.add(facing.getCounterClockWise());
	}

	protected void notifyNeighbors(Level worldIn, BlockPos pos, BlockState state, Set<Direction> set)
	{
		set.forEach((facing) -> notifyNeighbors(worldIn, pos, state, facing));
	}

	protected void notifyNeighbors(Level worldIn, BlockPos pos, BlockState state)
	{
	    Direction facing = state.getValue(FACING);
	    notifyNeighbors(worldIn, pos, state, facing);
	    notifyNeighbors(worldIn, pos, state, facing.getCounterClockWise());
	}

	protected void notifyNeighbors(Level worldIn, BlockPos pos, BlockState state, Direction direction)
	{
		BlockPos blockpos = pos.relative(direction);
		if (net.minecraftforge.event.ForgeEventFactory
				.onNeighborNotify(worldIn, pos, worldIn.getBlockState(pos), java.util.EnumSet.of(direction), false))
			return;
		worldIn.neighborChanged(blockpos, this, null);
		worldIn.updateNeighborsAtExceptFromFacing(blockpos, this, direction.getOpposite(), null);
	}

	protected void updateState(Level world, BlockPos pos, BlockState state)
	{
		if (!world.isClientSide)
		{
			int i = state.getValue(POWER);
			int j = calculateInputStrength(world, pos, state);
			if (i != j)
			{
				state = state.setValue(POWER, Integer.valueOf(j));
				world.setBlock(pos, state, 2);
				notifyNeighbors(world, pos, state);
			}
		}
	}

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
		if (i < i2)
			i = i2;
		return i;
	}

	@Override
	protected int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return getSignal(blockState, blockAccess, pos, side);
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return canConnectRedstone(blockState, blockAccess, pos, side) ? getActiveSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, Orientation orient,
			boolean isMoving) {
		if (state.canSurvive(worldIn, pos)) {
			updateState(worldIn, pos, state);
		} else {
			BlockEntity tileentity = state.hasBlockEntity() ? worldIn.getBlockEntity(pos) : null;
			dropResources(state, worldIn, pos, tileentity);
			worldIn.removeBlock(pos, false);
		}
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	protected int getActiveSignal(BlockGetter worldIn, BlockPos pos, BlockState state) {
		int i = state.getValue(POWER);
		return (i > 0) ? i - 1 : 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		Direction facing = state.getValue(FACING).getOpposite();
		return facing == side || facing.getCounterClockWise() == side;
	}

	@OnlyIn(Dist.CLIENT)
	public static int getColorForPower(int power) {
		return COLORS[power];
	}

	/**
	 * Called periodically clientside on blocks near the player to show effects
	 * (like furnace fire particles). Note that this method is unrelated to
	 * {@link #randomTick} and {@link #isRandomlyTicking}, and will always be called
	 * regardless of whether the block can receive random update ticks
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
		int i = stateIn.getValue(POWER);
		if (i != 0) {
			double d0 = (double) pos.getX() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.2D;
			double d1 = (double) ((float) pos.getY() + 0.1875F);
			double d2 = (double) pos.getZ() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.2D;
			worldIn.addParticle(new DustParticleOptions(COLORS[i], 1.0F), d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWER);
	}
}
