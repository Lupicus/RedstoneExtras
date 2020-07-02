package com.lupicus.rsx.block;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Modified version of RedstoneWireBlock
public class BluestoneWireBlock extends Block
{
	public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.REDSTONE_NORTH;
	public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.REDSTONE_EAST;
	public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.REDSTONE_SOUTH;
	public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.REDSTONE_WEST;
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
	public static final Map<Direction, EnumProperty<RedstoneSide>> FACING_PROPERTY_MAP = Maps.newEnumMap(ImmutableMap
			.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
	private static final VoxelShape DOT_SHAPE = Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
	private static final Map<Direction, VoxelShape> SIDE_SHAPES = Maps
			.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D),
					Direction.SOUTH, Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Direction.EAST,
					Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Direction.WEST,
					Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)));
	private static final Map<Direction, VoxelShape> UP_SHAPES = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH,
			VoxelShapes.or(
					SIDE_SHAPES.get(Direction.NORTH), Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)),
			Direction.SOUTH,
			VoxelShapes.or(SIDE_SHAPES.get(Direction.SOUTH),
					Block.makeCuboidShape(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)),
			Direction.EAST,
			VoxelShapes.or(SIDE_SHAPES.get(Direction.EAST),
					Block.makeCuboidShape(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)),
			Direction.WEST, VoxelShapes.or(SIDE_SHAPES.get(Direction.WEST),
					Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))));
	private final Map<BlockState, VoxelShape> shapes = Maps.newHashMap();
	private static final Vector3f[] COLORS = new Vector3f[16];
	private final BlockState powerDot;
	private RedstoneWireBlock wire = (RedstoneWireBlock) Blocks.REDSTONE_WIRE;

	public BluestoneWireBlock(Properties properties) {
		super(properties);
		setDefaultState(
				this.stateContainer.getBaseState().with(NORTH, RedstoneSide.NONE).with(EAST, RedstoneSide.NONE)
						.with(SOUTH, RedstoneSide.NONE).with(WEST, RedstoneSide.NONE).with(POWER, Integer.valueOf(0)));
		powerDot = getDefaultState().with(NORTH, RedstoneSide.SIDE).with(EAST, RedstoneSide.SIDE)
				.with(SOUTH, RedstoneSide.SIDE).with(WEST, RedstoneSide.SIDE);

		for (BlockState blockstate : getStateContainer().getValidStates()) {
			if (blockstate.get(POWER) == 0) {
				shapes.put(blockstate, func_235554_l_(blockstate));
			}
		}
	}

	private VoxelShape func_235554_l_(BlockState state) {
		VoxelShape voxelshape = DOT_SHAPE;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneside = state.get(FACING_PROPERTY_MAP.get(direction));
			if (redstoneside == RedstoneSide.SIDE) {
				voxelshape = VoxelShapes.or(voxelshape, SIDE_SHAPES.get(direction));
			} else if (redstoneside == RedstoneSide.UP) {
				voxelshape = VoxelShapes.or(voxelshape, UP_SHAPES.get(direction));
			}
		}

		return voxelshape;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return shapes.get(state.with(POWER, Integer.valueOf(0)));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getSides(context.getWorld(), powerDot, context.getPos());
	}

	private BlockState getSides(IBlockReader world, BlockState state, BlockPos pos) {
		boolean flag = isAllOff(state);
		state = getOffSides(world, getDefaultState().with(POWER, state.get(POWER)), pos);
		if (flag && isAllOff(state)) {
			return state;
		} else {
			boolean flag1 = state.get(NORTH).func_235921_b_();
			boolean flag2 = state.get(SOUTH).func_235921_b_();
			boolean flag3 = state.get(EAST).func_235921_b_();
			boolean flag4 = state.get(WEST).func_235921_b_();

			if (!flag1 && !flag2) {
				if (!flag4) {
					state = state.with(WEST, RedstoneSide.SIDE);
				}
				if (!flag3) {
					state = state.with(EAST, RedstoneSide.SIDE);
				}
			}
			if (!flag3 && !flag4) {
				if (!flag1) {
					state = state.with(NORTH, RedstoneSide.SIDE);
				}
				if (!flag2) {
					state = state.with(SOUTH, RedstoneSide.SIDE);
				}
			}

			return state;
		}
	}

	private BlockState getOffSides(IBlockReader world, BlockState state, BlockPos pos) {
		boolean flag = !world.getBlockState(pos.up()).isNormalCube(world, pos);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			EnumProperty<RedstoneSide> prop = FACING_PROPERTY_MAP.get(direction);
			if (!state.get(prop).func_235921_b_()) {
				state = state.with(prop, getSide(world, pos, direction, flag));
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
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (facing == Direction.DOWN) {
			return stateIn;
		} else if (facing == Direction.UP) {
			return getSides(worldIn, stateIn, currentPos);
		} else {
			RedstoneSide redstoneside = getSide(worldIn, currentPos, facing);
			return redstoneside.func_235921_b_() == stateIn.get(FACING_PROPERTY_MAP.get(facing)).func_235921_b_()
					&& !isAllOn(stateIn) ? stateIn.with(FACING_PROPERTY_MAP.get(facing), redstoneside)
							: getSides(worldIn, powerDot.with(POWER, stateIn.get(POWER))
									.with(FACING_PROPERTY_MAP.get(facing), redstoneside), currentPos);
		}
	}

	private static boolean isAllOn(BlockState state) {
		return state.get(NORTH).func_235921_b_() && state.get(SOUTH).func_235921_b_() &&
			   state.get(EAST).func_235921_b_() && state.get(WEST).func_235921_b_();
	}

	private static boolean isAllOff(BlockState state) {
		return !state.get(NORTH).func_235921_b_() && !state.get(SOUTH).func_235921_b_() &&
			   !state.get(EAST).func_235921_b_() && !state.get(WEST).func_235921_b_();
	}

	/**
	 * performs updates on diagonal neighbors of the target position and passes in
	 * the flags. The flags can be referenced from the docs for
	 * {@link IWorldWriter#setBlockState(IBlockState, BlockPos, int)}.
	 */
	@Override
	public void updateDiagonalNeighbors(BlockState state, IWorld worldIn, BlockPos pos, int flags, int p_196248_5_) {
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneside = state.get(FACING_PROPERTY_MAP.get(direction));
			if (redstoneside != RedstoneSide.NONE &&
				!worldIn.getBlockState(blockpos$mutable.func_239622_a_(pos, direction)).isIn(this)) { // setPos
				blockpos$mutable.move(Direction.DOWN);
				BlockState blockstate = worldIn.getBlockState(blockpos$mutable);
				if (!blockstate.isIn(Blocks.OBSERVER)) {
					BlockPos blockpos = blockpos$mutable.offset(direction.getOpposite());
					BlockState blockstate1 = blockstate.updatePostPlacement(direction.getOpposite(),
							worldIn.getBlockState(blockpos), worldIn, blockpos$mutable, blockpos);
					func_241468_a_(blockstate, blockstate1, worldIn, blockpos$mutable, flags, p_196248_5_); // replaceBlock
				}

				blockpos$mutable.func_239622_a_(pos, direction).move(Direction.UP); // setPos
				BlockState blockstate3 = worldIn.getBlockState(blockpos$mutable);
				if (!blockstate3.isIn(Blocks.OBSERVER)) {
					BlockPos blockpos1 = blockpos$mutable.offset(direction.getOpposite());
					BlockState blockstate2 = blockstate3.updatePostPlacement(direction.getOpposite(),
							worldIn.getBlockState(blockpos1), worldIn, blockpos$mutable, blockpos1);
					func_241468_a_(blockstate3, blockstate2, worldIn, blockpos$mutable, flags, p_196248_5_); // replaceBlock
				}
			}
		}
	}

	private RedstoneSide getSide(IBlockReader worldIn, BlockPos pos, Direction face) {
		BlockPos blockpos1 = pos.up();
		return getSide(worldIn, pos, face, !worldIn.getBlockState(blockpos1).isNormalCube(worldIn, blockpos1));
	}

	private RedstoneSide getSide(IBlockReader worldIn, BlockPos pos, Direction face, boolean checkUp) {
		BlockPos blockpos = pos.offset(face);
		BlockState blockstate = worldIn.getBlockState(blockpos);
		if (checkUp) {
			boolean flag = blockstate.isSolidSide(worldIn, blockpos, Direction.UP)
					|| blockstate.isIn(Blocks.HOPPER);
			if (flag && canConnectTo(worldIn.getBlockState(blockpos.up()), worldIn, blockpos.up(), null)) {
				if (blockstate.isSolidSide(worldIn, blockpos, face.getOpposite())) {
					return RedstoneSide.UP;
				}

				return RedstoneSide.SIDE;
			}
		}

		return !canConnectTo(blockstate, worldIn, blockpos, face) && (blockstate.isNormalCube(worldIn, blockpos)
				|| !canConnectTo(worldIn.getBlockState(blockpos.down()), worldIn, blockpos.down(), null))
						? RedstoneSide.NONE
						: RedstoneSide.SIDE;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockPos blockpos = pos.down();
		BlockState blockstate = worldIn.getBlockState(blockpos);
		return blockstate.isSolidSide(worldIn, blockpos, Direction.UP) || blockstate.isIn(Blocks.HOPPER);
	}

	private void updateSurroundingRedstone(World worldIn, BlockPos pos, BlockState state) {
		int i = func_235546_a_(worldIn, pos, state);
		if (state.get(POWER) != i) {
			if (worldIn.getBlockState(pos) == state) {
				worldIn.setBlockState(pos, state.with(POWER, Integer.valueOf(i)), 2);
			}

			Set<BlockPos> set = Sets.newHashSet();
			set.add(pos);

			for (Direction direction : Direction.values()) {
				set.add(pos.offset(direction));
			}

			for (BlockPos blockpos : set) {
				worldIn.notifyNeighborsOfStateChange(blockpos, this);
			}
		}
	}

	private int func_235546_a_(World world, BlockPos posIn, BlockState state) {
		wire.canProvidePower = false;
		int i = world.getRedstonePowerFromNeighbors(posIn);
		wire.canProvidePower = true;
		int j = 0;
		if (i < 15) {
			BlockPos blockpos1 = posIn.up();
			Boolean isUpNormal = world.getBlockState(blockpos1).isNormalCube(world, blockpos1);
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				BlockPos blockpos = posIn.offset(direction);
				BlockState blockstate1 = world.getBlockState(blockpos);
				j = Math.max(j, getPower(blockstate1));
				if (blockstate1.isNormalCube(world, blockpos)) {
					if (!isUpNormal)
						j = Math.max(j, getPower(world.getBlockState(blockpos.up())));
				} else {
					j = Math.max(j, getPower(world.getBlockState(blockpos.down())));
				}
			}
		}

		return Math.max(i, j - 1);
	}

	private int getPower(BlockState neighbor) {
		return neighbor.isIn(this) ? neighbor.get(POWER) : 0;
	}

	/**
	 * Calls World.notifyNeighborsOfStateChange() for all neighboring blocks, but
	 * only if the given block is a redstone wire.
	 */
	private void notifyWireNeighborsOfStateChange(World worldIn, BlockPos pos) {
		if (worldIn.getBlockState(pos).isIn(this)) {
			worldIn.notifyNeighborsOfStateChange(pos, this);

			for (Direction direction : Direction.values()) {
				worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
			}
		}
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!oldState.isIn(state.getBlock()) && !worldIn.isRemote) {
			updateSurroundingRedstone(worldIn, pos, state);

			for (Direction direction : Direction.Plane.VERTICAL) {
				worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
			}

			func_235553_d_(worldIn, pos);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!isMoving && !state.isIn(newState.getBlock())) {
			super.onReplaced(state, worldIn, pos, newState, isMoving);
			if (!worldIn.isRemote) {
				for (Direction direction : Direction.values()) {
					worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
				}

				updateSurroundingRedstone(worldIn, pos, state);
				func_235553_d_(worldIn, pos);
			}
		}
	}

	private void func_235553_d_(World world, BlockPos pos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			notifyWireNeighborsOfStateChange(world, pos.offset(direction));
		}

		for (Direction direction1 : Direction.Plane.HORIZONTAL) {
			BlockPos blockpos = pos.offset(direction1);
			if (world.getBlockState(blockpos).isNormalCube(world, blockpos)) {
				notifyWireNeighborsOfStateChange(world, blockpos.up());
			} else {
				notifyWireNeighborsOfStateChange(world, blockpos.down());
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (!worldIn.isRemote) {
			if (state.isValidPosition(worldIn, pos)) {
				updateSurroundingRedstone(worldIn, pos, state);
			} else {
				spawnDrops(state, worldIn, pos);
				worldIn.removeBlock(pos, false);
			}
		}
	}

	/**
	 * @deprecated call via
	 *             {@link IBlockState#getStrongPower(IBlockAccess,BlockPos,EnumFacing)}
	 *             whenever possible. Implementing/overriding is fine.
	 */
	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return !wire.canProvidePower ? 0 : blockState.getWeakPower(blockAccess, pos, side);
	}

	/**
	 * @deprecated call via
	 *             {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)}
	 *             whenever possible. Implementing/overriding is fine.
	 */
	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (!wire.canProvidePower || side == Direction.DOWN) {
			return 0;
		}
		int i = blockState.get(POWER);
		if (i == 0)
			return 0;
		return side == Direction.UP ||
				getSides(blockAccess, blockState, pos).get(FACING_PROPERTY_MAP.get(side.getOpposite())).func_235921_b_() ? i : 0;
	}

	protected static boolean canConnectTo(BlockState blockState, IBlockReader world, BlockPos pos,
			@Nullable Direction side) {
		if (blockState.isIn(ModBlocks.BLUESTONE_WIRE)) {
			return true;
		} else if (blockState.isIn(Blocks.REDSTONE_WIRE) || blockState.isIn(ModBlocks.REDSTONE_PIPE_BLOCK)) {
			return false;
		} else if (blockState.isIn(Blocks.REPEATER)) {
			Direction direction = blockState.get(RepeaterBlock.HORIZONTAL_FACING);
			return direction == side || direction.getOpposite() == side;
		} else if (blockState.isIn(Blocks.OBSERVER)) {
			return side == blockState.get(ObserverBlock.FACING);
		} else {
			return blockState.canConnectRedstone(world, pos, side) && side != null;
		}
	}

	/**
	 * Can this block provide power. Only wire currently seems to have this change
	 * based on its state.
	 * 
	 * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible.
	 *             Implementing/overriding is fine.
	 */
	@Override
	public boolean canProvidePower(BlockState state) {
		return wire.canProvidePower;
	}

	@OnlyIn(Dist.CLIENT)
	public static int colorMultiplier(int power) {
		Vector3f vector3f = COLORS[power];
		return MathHelper.rgb(vector3f.getX(), vector3f.getY(), vector3f.getZ());
	}

	@OnlyIn(Dist.CLIENT)
	private void doParticle(World world, Random rand, BlockPos pos, Vector3f vec,
			Direction dir1, Direction dir2, float fv1, float fv2) {
		float f = fv2 - fv1;
		if (!(rand.nextFloat() >= 0.2F * f)) {
			float f1 = 0.4375F;
			float f2 = fv1 + f * rand.nextFloat();
			double d0 = 0.5D + (double) (f1 * (float) dir1.getXOffset())
					+ (double) (f2 * (float) dir2.getXOffset());
			double d1 = 0.5D + (double) (f1 * (float) dir1.getYOffset())
					+ (double) (f2 * (float) dir2.getYOffset());
			double d2 = 0.5D + (double) (f1 * (float) dir1.getZOffset())
					+ (double) (f2 * (float) dir2.getZOffset());
			world.addParticle(
					new RedstoneParticleData(vec.getX(), vec.getY(), vec.getZ(), 1.0F),
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
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		int i = stateIn.get(POWER);
		if (i != 0) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				RedstoneSide redstoneside = stateIn.get(FACING_PROPERTY_MAP.get(direction));
				switch (redstoneside) {
				case UP:
					doParticle(worldIn, rand, pos, COLORS[i], direction, Direction.UP, -0.5F, 0.5F);
				case SIDE:
					doParticle(worldIn, rand, pos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.5F);
					break;
				case NONE:
				default:
					doParticle(worldIn, rand, pos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.3F);
				}
			}
		}
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		switch (rot) {
		case CLOCKWISE_180:
			return state.with(NORTH, state.get(SOUTH)).with(EAST, state.get(WEST)).with(SOUTH, state.get(NORTH))
					.with(WEST, state.get(EAST));
		case COUNTERCLOCKWISE_90:
			return state.with(NORTH, state.get(EAST)).with(EAST, state.get(SOUTH)).with(SOUTH, state.get(WEST))
					.with(WEST, state.get(NORTH));
		case CLOCKWISE_90:
			return state.with(NORTH, state.get(WEST)).with(EAST, state.get(NORTH)).with(SOUTH, state.get(EAST))
					.with(WEST, state.get(SOUTH));
		default:
			return state;
		}
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		switch (mirrorIn) {
		case LEFT_RIGHT:
			return state.with(NORTH, state.get(SOUTH)).with(SOUTH, state.get(NORTH));
		case FRONT_BACK:
			return state.with(EAST, state.get(WEST)).with(WEST, state.get(EAST));
		default:
			return super.mirror(state, mirrorIn);
		}
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, POWER);
	}

	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		if (!player.abilities.allowEdit) {
			return ActionResultType.PASS;
		} else {
			boolean flag = isAllOn(state);
			if (flag || isAllOff(state)) {
				BlockState blockstate = flag ? getDefaultState() : powerDot;
				blockstate = blockstate.with(POWER, state.get(POWER));
				blockstate = getSides(worldIn, blockstate, pos);
				if (blockstate != state) {
					worldIn.setBlockState(pos, blockstate, 3);
					notifyChangedSides(worldIn, pos, state, blockstate);
					return ActionResultType.SUCCESS;
				}
			}

			return ActionResultType.PASS;
		}
	}

	private void notifyChangedSides(World world, BlockPos pos, BlockState oldState, BlockState state) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockpos = pos.offset(direction);
			EnumProperty<RedstoneSide> prop = FACING_PROPERTY_MAP.get(direction);
			if (oldState.get(prop).func_235921_b_() != state.get(prop).func_235921_b_() &&
				world.getBlockState(blockpos).isNormalCube(world, blockpos)) {
				world.notifyNeighborsOfStateExcept(blockpos, state.getBlock(), direction.getOpposite());
			}
		}
	}

	static {
		for (int i = 0; i <= 15; ++i) {
			float f = (float) i / 15.0F;
			float f3 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
			float f2 = MathHelper.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
			float f1 = MathHelper.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
			COLORS[i] = new Vector3f(f1, f2, f3);
		}
	}
}
