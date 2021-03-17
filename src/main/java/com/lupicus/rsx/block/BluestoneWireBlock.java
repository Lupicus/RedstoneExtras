package com.lupicus.rsx.block;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
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
	protected static final VoxelShape[] SHAPES = new VoxelShape[] {
			Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D),
			Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D),
			Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D),
			Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D),
			Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D),
			Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 16.0D),
			Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D),
			Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 13.0D, 1.0D, 16.0D),
			Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D),
			Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 16.0D),
			Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D),
			Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 16.0D, 1.0D, 16.0D),
			Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 16.0D, 1.0D, 13.0D),
			Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D),
			Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 13.0D),
			Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D) };
	private RedstoneWireBlock wire = (RedstoneWireBlock) Blocks.REDSTONE_WIRE;
	/** List of blocks to update with redstone. */
	private final Set<BlockPos> blocksNeedingUpdate = Sets.newHashSet();

	public BluestoneWireBlock(Block.Properties properties) {
		super(properties);
		setDefaultState(
				this.stateContainer.getBaseState().with(NORTH, RedstoneSide.NONE).with(EAST, RedstoneSide.NONE)
						.with(SOUTH, RedstoneSide.NONE).with(WEST, RedstoneSide.NONE).with(POWER, Integer.valueOf(0)));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPES[getAABBIndex(state)];
	}

	private static int getAABBIndex(BlockState state) {
		int i = 0;
		boolean flag = state.get(NORTH) != RedstoneSide.NONE;
		boolean flag1 = state.get(EAST) != RedstoneSide.NONE;
		boolean flag2 = state.get(SOUTH) != RedstoneSide.NONE;
		boolean flag3 = state.get(WEST) != RedstoneSide.NONE;
		if (flag || flag2 && !flag && !flag1 && !flag3) {
			i |= 1 << Direction.NORTH.getHorizontalIndex();
		}

		if (flag1 || flag3 && !flag && !flag1 && !flag2) {
			i |= 1 << Direction.EAST.getHorizontalIndex();
		}

		if (flag2 || flag && !flag1 && !flag2 && !flag3) {
			i |= 1 << Direction.SOUTH.getHorizontalIndex();
		}

		if (flag3 || flag1 && !flag && !flag2 && !flag3) {
			i |= 1 << Direction.WEST.getHorizontalIndex();
		}

		return i;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		IBlockReader iblockreader = context.getWorld();
		BlockPos blockpos = context.getPos();
		return getDefaultState().with(WEST, getSide(iblockreader, blockpos, Direction.WEST))
				.with(EAST, getSide(iblockreader, blockpos, Direction.EAST))
				.with(NORTH, getSide(iblockreader, blockpos, Direction.NORTH))
				.with(SOUTH, getSide(iblockreader, blockpos, Direction.SOUTH));
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
		} else {
			return facing == Direction.UP
					? stateIn.with(WEST, getSide(worldIn, currentPos, Direction.WEST))
							.with(EAST, getSide(worldIn, currentPos, Direction.EAST))
							.with(NORTH, getSide(worldIn, currentPos, Direction.NORTH))
							.with(SOUTH, getSide(worldIn, currentPos, Direction.SOUTH))
					: stateIn.with(FACING_PROPERTY_MAP.get(facing), getSide(worldIn, currentPos, facing));
		}
	}

	/**
	 * performs updates on diagonal neighbors of the target position and passes in
	 * the flags. The flags can be referenced from the docs for
	 * {@link IWorldWriter#setBlockState(IBlockState, BlockPos, int)}.
	 */
	@Override
	public void updateDiagonalNeighbors(BlockState state, IWorld worldIn, BlockPos pos, int flags) {
		try (BlockPos.PooledMutable blockpos$pooledmutable = BlockPos.PooledMutable.retain()) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				RedstoneSide redstoneside = state.get(FACING_PROPERTY_MAP.get(direction));
				if (redstoneside != RedstoneSide.NONE && worldIn
						.getBlockState(blockpos$pooledmutable.setPos(pos).move(direction)).getBlock() != this) {
					blockpos$pooledmutable.move(Direction.DOWN);
					BlockState blockstate = worldIn.getBlockState(blockpos$pooledmutable);
					if (blockstate.getBlock() != Blocks.OBSERVER) {
						BlockPos blockpos = blockpos$pooledmutable.offset(direction.getOpposite());
						BlockState blockstate1 = blockstate.updatePostPlacement(direction.getOpposite(),
								worldIn.getBlockState(blockpos), worldIn, blockpos$pooledmutable, blockpos);
						replaceBlock(blockstate, blockstate1, worldIn, blockpos$pooledmutable, flags);
					}

					blockpos$pooledmutable.setPos(pos).move(direction).move(Direction.UP);
					BlockState blockstate3 = worldIn.getBlockState(blockpos$pooledmutable);
					if (blockstate3.getBlock() != Blocks.OBSERVER) {
						BlockPos blockpos1 = blockpos$pooledmutable.offset(direction.getOpposite());
						BlockState blockstate2 = blockstate3.updatePostPlacement(direction.getOpposite(),
								worldIn.getBlockState(blockpos1), worldIn, blockpos$pooledmutable, blockpos1);
						replaceBlock(blockstate3, blockstate2, worldIn, blockpos$pooledmutable, flags);
					}
				}
			}
		}
	}

	private RedstoneSide getSide(IBlockReader worldIn, BlockPos pos, Direction face) {
		BlockPos blockpos = pos.offset(face);
		BlockState blockstate = worldIn.getBlockState(blockpos);
		BlockPos blockpos1 = pos.up();
		BlockState blockstate1 = worldIn.getBlockState(blockpos1);
		if (!blockstate1.isNormalCube(worldIn, blockpos1)) {
			boolean flag = blockstate.isSolidSide(worldIn, blockpos, Direction.UP)
					|| blockstate.getBlock() == Blocks.HOPPER;
			if (flag && canConnectTo(worldIn.getBlockState(blockpos.up()), worldIn, blockpos.up(), null)) {
				if (blockstate.isCollisionShapeOpaque(worldIn, blockpos)) {
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
		return blockstate.isSolidSide(worldIn, blockpos, Direction.UP) || blockstate.getBlock() == Blocks.HOPPER;
	}

	private BlockState updateSurroundingRedstone(World worldIn, BlockPos pos, BlockState state) {
		state = func_212568_b(worldIn, pos, state);
		List<BlockPos> list = Lists.newArrayList(blocksNeedingUpdate);
		blocksNeedingUpdate.clear();

		for (BlockPos blockpos : list) {
			worldIn.notifyNeighborsOfStateChange(blockpos, this);
		}

		return state;
	}

	private BlockState func_212568_b(World world, BlockPos posIn, BlockState stateIn) {
		BlockState blockstate = stateIn;
		int i = stateIn.get(POWER);
		wire.canProvidePower = false;
		int j = world.getRedstonePowerFromNeighbors(posIn);
		wire.canProvidePower = true;
		int k = 0;
		if (j < 15) {
			BlockPos blockpos1 = posIn.up();
			Boolean isUpNormal = world.getBlockState(blockpos1).isNormalCube(world, blockpos1);
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				BlockPos blockpos = posIn.offset(direction);
				BlockState blockstate1 = world.getBlockState(blockpos);
				k = maxSignal2(k, blockstate1);
				if (blockstate1.isNormalCube(world, blockpos)) {
					if (!isUpNormal)
						k = maxSignal(k, world.getBlockState(blockpos.up()));
				} else {
					k = maxSignal(k, world.getBlockState(blockpos.down()));
				}
			}
		}

		int l = k - 1;
		if (j > l) {
			l = j;
		}

		if (i != l) {
			stateIn = stateIn.with(POWER, Integer.valueOf(l));
			if (world.getBlockState(posIn) == blockstate) {
				world.setBlockState(posIn, stateIn, 2);
			}

			blocksNeedingUpdate.add(posIn);

			for (Direction direction1 : Direction.values()) {
				blocksNeedingUpdate.add(posIn.offset(direction1));
			}
		}

		return stateIn;
	}

	/**
	 * Calls World.notifyNeighborsOfStateChange() for all neighboring blocks, but
	 * only if the given block is a redstone wire.
	 */
	private void notifyWireNeighborsOfStateChange(World worldIn, BlockPos pos) {
		if (worldIn.getBlockState(pos).getBlock() == this) {
			worldIn.notifyNeighborsOfStateChange(pos, this);

			for (Direction direction : Direction.values()) {
				worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
			}
		}
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (oldState.getBlock() != state.getBlock() && !worldIn.isRemote) {
			updateSurroundingRedstone(worldIn, pos, state);

			for (Direction direction : Direction.Plane.VERTICAL) {
				worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
			}

			for (Direction direction1 : Direction.Plane.HORIZONTAL) {
				notifyWireNeighborsOfStateChange(worldIn, pos.offset(direction1));
			}

			for (Direction direction2 : Direction.Plane.HORIZONTAL) {
				BlockPos blockpos = pos.offset(direction2);
				if (worldIn.getBlockState(blockpos).isNormalCube(worldIn, blockpos)) {
					notifyWireNeighborsOfStateChange(worldIn, blockpos.up());
				} else {
					notifyWireNeighborsOfStateChange(worldIn, blockpos.down());
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!isMoving && state.getBlock() != newState.getBlock()) {
			super.onReplaced(state, worldIn, pos, newState, isMoving);
			if (!worldIn.isRemote) {
				for (Direction direction : Direction.values()) {
					worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
				}

				updateSurroundingRedstone(worldIn, pos, state);

				for (Direction direction1 : Direction.Plane.HORIZONTAL) {
					notifyWireNeighborsOfStateChange(worldIn, pos.offset(direction1));
				}

				for (Direction direction2 : Direction.Plane.HORIZONTAL) {
					BlockPos blockpos = pos.offset(direction2);
					if (worldIn.getBlockState(blockpos).isNormalCube(worldIn, blockpos)) {
						notifyWireNeighborsOfStateChange(worldIn, blockpos.up());
					} else {
						notifyWireNeighborsOfStateChange(worldIn, blockpos.down());
					}
				}
			}
		}
	}

	private int maxSignal(int existingSignal, BlockState neighbor) {
		if (neighbor.getBlock() != this) {
			return existingSignal;
		} else {
			int i = neighbor.get(POWER);
			return i > existingSignal ? i : existingSignal;
		}
	}

	private int maxSignal2(int existingSignal, BlockState neighbor) {
		Block test = neighbor.getBlock();
		if (test != this && test != ModBlocks.BLUESTONE_PIPE_BLOCK) {
			return existingSignal;
		} else {
			int i = neighbor.get(POWER);
			return i > existingSignal ? i : existingSignal;
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
		if (!wire.canProvidePower) {
			return 0;
		} else {
			int i = blockState.get(POWER);
			if (i == 0) {
				return 0;
			} else if (side == Direction.UP) {
				return i;
			} else {
				EnumSet<Direction> enumset = EnumSet.noneOf(Direction.class);

				for (Direction direction : Direction.Plane.HORIZONTAL) {
					if (isPowerSourceAt(blockAccess, pos, direction)) {
						enumset.add(direction);
					}
				}

				if (side.getAxis().isHorizontal() && enumset.isEmpty()) {
					return i;
				} else {
					return enumset.contains(side) && !enumset.contains(side.rotateYCCW())
							&& !enumset.contains(side.rotateY()) ? i : 0;
				}
			}
		}
	}

	private boolean isPowerSourceAt(IBlockReader worldIn, BlockPos pos, Direction side) {
		BlockPos blockpos = pos.offset(side);
		BlockState blockstate = worldIn.getBlockState(blockpos);
		boolean flag = blockstate.isNormalCube(worldIn, blockpos);
		BlockPos blockpos1 = pos.up();
		boolean flag1 = worldIn.getBlockState(blockpos1).isNormalCube(worldIn, blockpos1);
		if (!flag1 && flag && canConnectTo(worldIn.getBlockState(blockpos.up()), worldIn, blockpos.up(), null)) {
			return true;
		} else if (canConnectTo(blockstate, worldIn, blockpos, side)) {
			return true;
		} else if (blockstate.getBlock() == Blocks.REPEATER && blockstate.get(RedstoneDiodeBlock.POWERED)
				&& blockstate.get(RedstoneDiodeBlock.HORIZONTAL_FACING) == side) {
			return true;
		} else {
			return !flag && canConnectTo(worldIn.getBlockState(blockpos.down()), worldIn, blockpos.down(), null);
		}
	}

	protected static boolean canConnectTo(BlockState blockState, IBlockReader world, BlockPos pos,
			@Nullable Direction side) {
		Block block = blockState.getBlock();
		if (block == ModBlocks.BLUESTONE_WIRE) {
			return true;
		} else if (block == Blocks.REDSTONE_WIRE || block == ModBlocks.REDSTONE_PIPE_BLOCK) {
			return false;
		} else if (block == Blocks.REPEATER) {
			Direction direction = blockState.get(RepeaterBlock.HORIZONTAL_FACING);
			return direction == side || direction.getOpposite() == side;
		} else if (block == Blocks.OBSERVER) {
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
		float f = (float) power / 15.0F;
		float f3 = f * 0.6F + 0.4F;
		if (power == 0) {
			f3 = 0.3F;
		}

		float f2 = f * f * 0.7F - 0.5F;
		float f1 = f * f * 0.6F - 0.7F;
		if (f2 < 0.0F) {
			f2 = 0.0F;
		}

		if (f1 < 0.0F) {
			f1 = 0.0F;
		}

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
			double d1 = (double) ((float) pos.getY() + 0.0625F);
			double d2 = (double) pos.getZ() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.2D;
			float f = (float) i / 15.0F;
			float f3 = f * 0.6F + 0.4F;
			float f2 = Math.max(0.0F, f * f * 0.7F - 0.5F);
			float f1 = Math.max(0.0F, f * f * 0.6F - 0.7F);
			worldIn.addParticle(new RedstoneParticleData(f1, f2, f3, 1.0F), d0, d1, d2, 0.0D, 0.0D, 0.0D);
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
}
