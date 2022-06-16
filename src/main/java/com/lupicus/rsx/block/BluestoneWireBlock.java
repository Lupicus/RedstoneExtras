package com.lupicus.rsx.block;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Modified version of RedStoneWireBlock
public class BluestoneWireBlock extends Block
{
	public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
	public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	public static final Map<Direction, EnumProperty<RedstoneSide>> FACING_PROPERTY_MAP = Maps.newEnumMap(ImmutableMap
			.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
	private static final VoxelShape DOT_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
	private static final Map<Direction, VoxelShape> SIDE_SHAPES = Maps
			.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D),
					Direction.SOUTH, Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Direction.EAST,
					Block.box(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Direction.WEST,
					Block.box(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)));
	private static final Map<Direction, VoxelShape> UP_SHAPES = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH,
			Shapes.or(
					SIDE_SHAPES.get(Direction.NORTH), Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)),
			Direction.SOUTH,
			Shapes.or(SIDE_SHAPES.get(Direction.SOUTH),
					Block.box(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)),
			Direction.EAST,
			Shapes.or(SIDE_SHAPES.get(Direction.EAST),
					Block.box(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)),
			Direction.WEST, Shapes.or(SIDE_SHAPES.get(Direction.WEST),
					Block.box(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))));
	private static final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
	private static final Vector3f[] COLORS = new Vector3f[16];
	private final BlockState powerDot;
	private RedStoneWireBlock wire = (RedStoneWireBlock) Blocks.REDSTONE_WIRE;

	public BluestoneWireBlock(Properties properties) {
		super(properties);
		registerDefaultState(
				stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE)
						.setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(POWER, Integer.valueOf(0)));
		powerDot = defaultBlockState().setValue(NORTH, RedstoneSide.SIDE).setValue(EAST, RedstoneSide.SIDE)
				.setValue(SOUTH, RedstoneSide.SIDE).setValue(WEST, RedstoneSide.SIDE);

		for (BlockState blockstate : getStateDefinition().getPossibleStates()) {
			if (blockstate.getValue(POWER) == 0) {
				SHAPES_CACHE.put(blockstate, calculateShape(blockstate));
			}
		}
	}

	private VoxelShape calculateShape(BlockState state) {
		VoxelShape voxelshape = DOT_SHAPE;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneside = state.getValue(FACING_PROPERTY_MAP.get(direction));
			if (redstoneside == RedstoneSide.SIDE) {
				voxelshape = Shapes.or(voxelshape, SIDE_SHAPES.get(direction));
			} else if (redstoneside == RedstoneSide.UP) {
				voxelshape = Shapes.or(voxelshape, UP_SHAPES.get(direction));
			}
		}

		return voxelshape;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPES_CACHE.get(state.setValue(POWER, Integer.valueOf(0)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return getConnectionState(context.getLevel(), powerDot, context.getClickedPos());
	}

	private BlockState getConnectionState(BlockGetter world, BlockState state, BlockPos pos) {
		boolean flag = isDot(state);
		state = getMissingConnections(world, defaultBlockState().setValue(POWER, state.getValue(POWER)), pos);
		if (flag && isDot(state)) {
			return state;
		} else {
			boolean flag1 = state.getValue(NORTH).isConnected();
			boolean flag2 = state.getValue(SOUTH).isConnected();
			boolean flag3 = state.getValue(EAST).isConnected();
			boolean flag4 = state.getValue(WEST).isConnected();

			if (!flag1 && !flag2) {
				if (!flag4) {
					state = state.setValue(WEST, RedstoneSide.SIDE);
				}
				if (!flag3) {
					state = state.setValue(EAST, RedstoneSide.SIDE);
				}
			}
			if (!flag3 && !flag4) {
				if (!flag1) {
					state = state.setValue(NORTH, RedstoneSide.SIDE);
				}
				if (!flag2) {
					state = state.setValue(SOUTH, RedstoneSide.SIDE);
				}
			}

			return state;
		}
	}

	private BlockState getMissingConnections(BlockGetter world, BlockState state, BlockPos pos) {
		boolean flag = !world.getBlockState(pos.above()).isRedstoneConductor(world, pos);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			EnumProperty<RedstoneSide> prop = FACING_PROPERTY_MAP.get(direction);
			if (!state.getValue(prop).isConnected()) {
				state = state.setValue(prop, getConnectingSide(world, pos, direction, flag));
			}
		}

		return state;
	}

	/**
	 * Update the provided state given the provided neighbor facing and neighbor
	 * state, returning a new state. For example, fences make their connections to
	 * the passed in state if possible, and wet concrete powder immediately returns
	 * its solidified counterpart. Note that this method should ideally consider
	 * only the specific face passed in.
	 */
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (facing == Direction.DOWN) {
			return stateIn;
		} else if (facing == Direction.UP) {
			return getConnectionState(worldIn, stateIn, currentPos);
		} else {
			RedstoneSide redstoneside = getConnectingSide(worldIn, currentPos, facing);
			return redstoneside.isConnected() == stateIn.getValue(FACING_PROPERTY_MAP.get(facing)).isConnected()
					&& !isCross(stateIn) ? stateIn.setValue(FACING_PROPERTY_MAP.get(facing), redstoneside)
							: getConnectionState(worldIn, powerDot.setValue(POWER, stateIn.getValue(POWER))
									.setValue(FACING_PROPERTY_MAP.get(facing), redstoneside), currentPos);
		}
	}

	private static boolean isCross(BlockState state) {
		return state.getValue(NORTH).isConnected() && state.getValue(SOUTH).isConnected() &&
			   state.getValue(EAST).isConnected() && state.getValue(WEST).isConnected();
	}

	private static boolean isDot(BlockState state) {
		return !state.getValue(NORTH).isConnected() && !state.getValue(SOUTH).isConnected() &&
			   !state.getValue(EAST).isConnected() && !state.getValue(WEST).isConnected();
	}

	/**
	 * performs updates on diagonal neighbors of the target position and passes in
	 * the flags. The flags can be referenced from the docs for
	 * {@link Level#setBlock(BlockPos, BlockState, int)} or
	 * {@link net.minecraftforge.common.util.Constants.BlockFlags}.
	 */
	@Override
	public void updateIndirectNeighbourShapes(BlockState state, LevelAccessor worldIn, BlockPos pos, int flags, int p_196248_5_) {
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneside = state.getValue(FACING_PROPERTY_MAP.get(direction));
			if (redstoneside != RedstoneSide.NONE &&
				!worldIn.getBlockState(blockpos$mutable.setWithOffset(pos, direction)).is(this)) {
				blockpos$mutable.move(Direction.DOWN);
				BlockState blockstate = worldIn.getBlockState(blockpos$mutable);
				if (!blockstate.is(Blocks.OBSERVER)) {
					BlockPos blockpos = blockpos$mutable.relative(direction.getOpposite());
					BlockState blockstate1 = blockstate.updateShape(direction.getOpposite(),
							worldIn.getBlockState(blockpos), worldIn, blockpos$mutable, blockpos);
					updateOrDestroy(blockstate, blockstate1, worldIn, blockpos$mutable, flags, p_196248_5_);
				}

				blockpos$mutable.setWithOffset(pos, direction).move(Direction.UP);
				BlockState blockstate3 = worldIn.getBlockState(blockpos$mutable);
				if (!blockstate3.is(Blocks.OBSERVER)) {
					BlockPos blockpos1 = blockpos$mutable.relative(direction.getOpposite());
					BlockState blockstate2 = blockstate3.updateShape(direction.getOpposite(),
							worldIn.getBlockState(blockpos1), worldIn, blockpos$mutable, blockpos1);
					updateOrDestroy(blockstate3, blockstate2, worldIn, blockpos$mutable, flags, p_196248_5_);
				}
			}
		}
	}

	private RedstoneSide getConnectingSide(BlockGetter worldIn, BlockPos pos, Direction face) {
		BlockPos blockpos1 = pos.above();
		return getConnectingSide(worldIn, pos, face, !worldIn.getBlockState(blockpos1).isRedstoneConductor(worldIn, blockpos1));
	}

	private RedstoneSide getConnectingSide(BlockGetter worldIn, BlockPos pos, Direction face, boolean checkUp) {
		BlockPos blockpos = pos.relative(face);
		BlockState blockstate = worldIn.getBlockState(blockpos);
		if (checkUp) {
			boolean flag = canSurviveOn(worldIn, blockpos, blockstate);
			if (flag && shouldConnectTo(worldIn.getBlockState(blockpos.above()), worldIn, blockpos.above(), (Direction) null)) {
				if (blockstate.isFaceSturdy(worldIn, blockpos, face.getOpposite())) {
					return RedstoneSide.UP;
				}

				return RedstoneSide.SIDE;
			}
		}

		if (shouldConnectTo(blockstate, worldIn, blockpos, face))
			return RedstoneSide.SIDE;
		else if (blockstate.isRedstoneConductor(worldIn, blockpos))
			return RedstoneSide.NONE;
		else {
			BlockPos blockPosBelow = blockpos.below();
			return shouldConnectTo(worldIn.getBlockState(blockPosBelow), worldIn, blockPosBelow, (Direction) null)
					? RedstoneSide.SIDE : RedstoneSide.NONE;
		}
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return false;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		BlockPos blockpos = pos.below();
		BlockState blockstate = worldIn.getBlockState(blockpos);
		return canSurviveOn(worldIn, blockpos, blockstate);
	}

	private boolean canSurviveOn(BlockGetter world, BlockPos pos, BlockState state) {
		return state.isFaceSturdy(world, pos, Direction.UP) || state.is(Blocks.HOPPER);
	}

	private void updatePowerStrength(Level worldIn, BlockPos pos, BlockState state) {
		int i = calculateTargetStrength(worldIn, pos);
		if (state.getValue(POWER) != i) {
			if (worldIn.getBlockState(pos) == state) {
				worldIn.setBlock(pos, state.setValue(POWER, Integer.valueOf(i)), 2);
			}

			Set<BlockPos> set = Sets.newHashSet();
			set.add(pos);

			for (Direction direction : Direction.values()) {
				set.add(pos.relative(direction));
			}

			for (BlockPos blockpos : set) {
				worldIn.updateNeighborsAt(blockpos, this);
			}
		}
	}

	private int calculateTargetStrength(Level world, BlockPos posIn) {
		wire.shouldSignal = false;
		int i = world.getBestNeighborSignal(posIn);
		wire.shouldSignal = true;
		int j = 0;
		if (i < 15) {
			BlockPos blockpos1 = posIn.above();
			Boolean isUpNormal = world.getBlockState(blockpos1).isRedstoneConductor(world, blockpos1);
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				BlockPos blockpos = posIn.relative(direction);
				BlockState blockstate = world.getBlockState(blockpos);
				j = Math.max(j, getLineSignal(blockstate));
				if (blockstate.isRedstoneConductor(world, blockpos)) {
					if (!isUpNormal)
						j = Math.max(j, getWireSignal(world.getBlockState(blockpos.above())));
				} else {
					j = Math.max(j, getWireSignal(world.getBlockState(blockpos.below())));
				}
			}
		}

		return Math.max(i, j - 1);
	}

	private int getWireSignal(BlockState neighbor) {
		return neighbor.is(this) ? neighbor.getValue(POWER) : 0;
	}

	private int getLineSignal(BlockState neighbor) {
		return neighbor.is(this) || neighbor.is(ModBlocks.BLUESTONE_PIPE_BLOCK) ? neighbor.getValue(POWER) : 0;
	}

	/**
	 * Calls World.updateNeighborsAt() for all neighboring blocks, but
	 * only if the given block is a bluestone wire.
	 */
	private void checkCornerChangeAt(Level worldIn, BlockPos pos) {
		if (worldIn.getBlockState(pos).is(this)) {
			worldIn.updateNeighborsAt(pos, this);

			for (Direction direction : Direction.values()) {
				worldIn.updateNeighborsAt(pos.relative(direction), this);
			}
		}
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!oldState.is(state.getBlock()) && !worldIn.isClientSide) {
			updatePowerStrength(worldIn, pos, state);

			for (Direction direction : Direction.Plane.VERTICAL) {
				worldIn.updateNeighborsAt(pos.relative(direction), this);
			}

			updateNeighborsOfNeighboringWires(worldIn, pos);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!isMoving && !state.is(newState.getBlock())) {
			super.onRemove(state, worldIn, pos, newState, isMoving);
			if (!worldIn.isClientSide) {
				for (Direction direction : Direction.values()) {
					worldIn.updateNeighborsAt(pos.relative(direction), this);
				}

				updatePowerStrength(worldIn, pos, state);
				updateNeighborsOfNeighboringWires(worldIn, pos);
			}
		}
	}

	private void updateNeighborsOfNeighboringWires(Level world, BlockPos pos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			checkCornerChangeAt(world, pos.relative(direction));
		}

		for (Direction direction1 : Direction.Plane.HORIZONTAL) {
			BlockPos blockpos = pos.relative(direction1);
			if (world.getBlockState(blockpos).isRedstoneConductor(world, blockpos)) {
				checkCornerChangeAt(world, blockpos.above());
			} else {
				checkCornerChangeAt(world, blockpos.below());
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (!worldIn.isClientSide) {
			if (state.canSurvive(worldIn, pos)) {
				updatePowerStrength(worldIn, pos, state);
			} else {
				dropResources(state, worldIn, pos);
				worldIn.removeBlock(pos, false);
			}
		}
	}

	/**
	 * @deprecated call via
	 *             {@link BlockStateBase#getDirectSignal(BlockGetter,BlockPos,Direction)}
	 *             whenever possible. Implementing/overriding is fine.
	 */
	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return !wire.shouldSignal ? 0 : blockState.getSignal(blockAccess, pos, side);
	}

	/**
	 * @deprecated call via
	 *             {@link BlockStateBase#getSignal(BlockGetter,BlockPos,Direction)}
	 *             whenever possible. Implementing/overriding is fine.
	 */
	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		if (!wire.shouldSignal || side == Direction.DOWN) {
			return 0;
		}
		int i = blockState.getValue(POWER);
		if (i == 0)
			return 0;
		return side == Direction.UP ||
				getConnectionState(blockAccess, blockState, pos).getValue(FACING_PROPERTY_MAP.get(side.getOpposite())).isConnected() ? i : 0;
	}

	protected static boolean shouldConnectTo(BlockState blockState, BlockGetter world, BlockPos pos,
			@Nullable Direction side) {
		if (blockState.is(ModBlocks.BLUESTONE_WIRE)) {
			return true;
		} else if (blockState.is(ModBlocks.BLUESTONE_PIPE_BLOCK)) {
			return side != null;
		} else if (blockState.is(Blocks.REDSTONE_WIRE) || blockState.is(ModBlocks.REDSTONE_PIPE_BLOCK)) {
			return false;
		} else {
			return side != null && blockState.canRedstoneConnectTo(world, pos, side);
		}
	}

	/**
	 * Can this block provide power. Only wire currently seems to have this change
	 * based on its state.
	 * 
	 * @deprecated call via {@link BlockStateBase#isSignalSource()} whenever possible.
	 *             Implementing/overriding is fine.
	 */
	@Override
	public boolean isSignalSource(BlockState state) {
		return wire.shouldSignal;
	}

	@OnlyIn(Dist.CLIENT)
	public static int colorMultiplier(int power) {
		Vector3f vector3f = COLORS[power];
		return Mth.color(vector3f.x(), vector3f.y(), vector3f.z());
	}

	@OnlyIn(Dist.CLIENT)
	private void spawnParticlesAlongLine(Level world, RandomSource rand, BlockPos pos, Vector3f vec,
			Direction dir1, Direction dir2, float fv1, float fv2) {
		float f = fv2 - fv1;
		if (!(rand.nextFloat() >= 0.2F * f)) {
			float f1 = 0.4375F;
			float f2 = fv1 + f * rand.nextFloat();
			double d0 = 0.5D + (double) (f1 * (float) dir1.getStepX())
					+ (double) (f2 * (float) dir2.getStepX());
			double d1 = 0.5D + (double) (f1 * (float) dir1.getStepY())
					+ (double) (f2 * (float) dir2.getStepY());
			double d2 = 0.5D + (double) (f1 * (float) dir1.getStepZ())
					+ (double) (f2 * (float) dir2.getStepZ());
			world.addParticle(
					new DustParticleOptions(vec, 1.0F),
					(double) pos.getX() + d0, (double) pos.getY() + d1,
					(double) pos.getZ() + d2, 0.0D, 0.0D, 0.0D);
		}
	}

	/**
	 * Called periodically clientside on blocks near the player to show effects
	 * (like furnace fire particles). Note that this method is unrelated to
	 * {@link randomTick} and {@link #needsRandomTick}, and will always be called
	 * regardless of whether the block can receive random update ticks
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
		int i = stateIn.getValue(POWER);
		if (i != 0) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				RedstoneSide redstoneside = stateIn.getValue(FACING_PROPERTY_MAP.get(direction));
				switch (redstoneside) {
				case UP:
					spawnParticlesAlongLine(worldIn, rand, pos, COLORS[i], direction, Direction.UP, -0.5F, 0.5F);
				case SIDE:
					spawnParticlesAlongLine(worldIn, rand, pos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.5F);
					break;
				case NONE:
				default:
					spawnParticlesAlongLine(worldIn, rand, pos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.3F);
				}
			}
		}
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link BlockStateBase#rotation(Rotation)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		switch (rot) {
		case CLOCKWISE_180:
			return state.setValue(NORTH, state.getValue(SOUTH)).setValue(EAST, state.getValue(WEST)).setValue(SOUTH, state.getValue(NORTH))
					.setValue(WEST, state.getValue(EAST));
		case COUNTERCLOCKWISE_90:
			return state.setValue(NORTH, state.getValue(EAST)).setValue(EAST, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(WEST))
					.setValue(WEST, state.getValue(NORTH));
		case CLOCKWISE_90:
			return state.setValue(NORTH, state.getValue(WEST)).setValue(EAST, state.getValue(NORTH)).setValue(SOUTH, state.getValue(EAST))
					.setValue(WEST, state.getValue(SOUTH));
		default:
			return state;
		}
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link BlockStateBase#mirror(Mirror)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		switch (mirrorIn) {
		case LEFT_RIGHT:
			return state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
		case FRONT_BACK:
			return state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
		default:
			return super.mirror(state, mirrorIn);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, POWER);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult hit) {
		if (!player.mayBuild()) {
			return InteractionResult.PASS;
		} else {
			boolean flag = isCross(state);
			if (flag || isDot(state)) {
				BlockState blockstate = flag ? defaultBlockState() : powerDot;
				blockstate = blockstate.setValue(POWER, state.getValue(POWER));
				blockstate = getConnectionState(worldIn, blockstate, pos);
				if (blockstate != state) {
					worldIn.setBlock(pos, blockstate, 3);
					updatesOnShapeChange(worldIn, pos, state, blockstate);
					return InteractionResult.SUCCESS;
				}
			}

			return InteractionResult.PASS;
		}
	}

	private void updatesOnShapeChange(Level world, BlockPos pos, BlockState oldState, BlockState state) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockpos = pos.relative(direction);
			EnumProperty<RedstoneSide> prop = FACING_PROPERTY_MAP.get(direction);
			if (oldState.getValue(prop).isConnected() != state.getValue(prop).isConnected() &&
				world.getBlockState(blockpos).isRedstoneConductor(world, blockpos)) {
				world.updateNeighborsAtExceptFromFacing(blockpos, state.getBlock(), direction.getOpposite());
			}
		}
	}

	static {
		for (int i = 0; i <= 15; ++i) {
			float f = (float) i / 15.0F;
			float f3 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
			float f2 = Mth.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
			float f1 = Mth.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
			COLORS[i] = new Vector3f(f1, f2, f3);
		}
	}
}
