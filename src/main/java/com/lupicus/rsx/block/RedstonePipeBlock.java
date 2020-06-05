package com.lupicus.rsx.block;

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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstonePipeBlock extends Block
{
	public static final EnumProperty<RedstoneSide> REDSTONE_UP = EnumProperty.create("up", RedstoneSide.class);
	public static final EnumProperty<RedstoneSide> REDSTONE_DOWN = EnumProperty.create("down", RedstoneSide.class);
	public static final EnumProperty<RedstoneSide> UP = REDSTONE_UP;
	public static final EnumProperty<RedstoneSide> DOWN = REDSTONE_DOWN;
	public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.REDSTONE_NORTH;
	public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.REDSTONE_EAST;
	public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.REDSTONE_SOUTH;
	public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.REDSTONE_WEST;
	public static final IntegerProperty POWER = RedstoneWireBlock.POWER; // == BlockStateProperties.POWER_0_15;
	public static final Map<Direction, EnumProperty<RedstoneSide>> FACING_PROPERTY_MAP = Maps.newEnumMap(ImmutableMap.<Direction, EnumProperty<RedstoneSide>>builder()
			.put(Direction.NORTH, NORTH)
			.put(Direction.EAST, EAST)
			.put(Direction.SOUTH, SOUTH)
			.put(Direction.WEST, WEST)
			.put(Direction.UP, UP)
			.put(Direction.DOWN, DOWN)
			.build());
	private RedstoneWireBlock wire = (RedstoneWireBlock) Blocks.REDSTONE_WIRE;
	/** List of blocks to update with redstone. */
	private final Set<BlockPos> blocksNeedingUpdate = Sets.newHashSet();

	public RedstonePipeBlock(Properties properties)
	{
		super(properties);
		setDefaultState(stateContainer.getBaseState().with(NORTH, RedstoneSide.NONE).with(EAST, RedstoneSide.NONE)
				.with(SOUTH, RedstoneSide.NONE).with(WEST, RedstoneSide.NONE).with(UP, RedstoneSide.NONE)
				.with(DOWN, RedstoneSide.NONE).with(POWER, Integer.valueOf(0)));
	}

	@Override
	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		IBlockReader iblockreader = context.getWorld();
		BlockPos blockpos = context.getPos();
		return getDefaultState().with(WEST, getSide(iblockreader, blockpos, Direction.WEST))
				.with(EAST, getSide(iblockreader, blockpos, Direction.EAST))
				.with(NORTH, getSide(iblockreader, blockpos, Direction.NORTH))
				.with(SOUTH, getSide(iblockreader, blockpos, Direction.SOUTH))
				.with(UP, getSide(iblockreader, blockpos, Direction.UP))
				.with(DOWN, getSide(iblockreader, blockpos, Direction.DOWN));
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		return stateIn.with(FACING_PROPERTY_MAP.get(facing), getSide(worldIn, currentPos, facing));
	}

	private RedstoneSide getSide(IBlockReader worldIn, BlockPos pos, Direction face) {
		BlockPos blockpos = pos.offset(face);
		BlockState blockstate = worldIn.getBlockState(blockpos);
		return canConnectTo(blockstate, worldIn, blockpos, face) ? RedstoneSide.SIDE : RedstoneSide.NONE;
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

	private BlockState func_212568_b(World world, BlockPos pos, BlockState stateIn) {
		BlockState blockstate = stateIn;
		int i = stateIn.get(POWER);
		int j = 0;
		wire.canProvidePower = false;
		j = world.getRedstonePowerFromNeighbors(pos);
		wire.canProvidePower = true;
		int k = 0;
		if (j < 15) {
			for (Direction direction : Direction.values()) {
				BlockPos blockpos = pos.offset(direction);
				BlockState blockstate1 = world.getBlockState(blockpos);
				k = maxSignal(k, blockstate1);
			}
		}

		int l = k - 1;
		if (j > l) {
			l = j;
		}

		if (i != l) {
			stateIn = stateIn.with(POWER, Integer.valueOf(l));
			if (world.getBlockState(pos) == blockstate) {
				world.setBlockState(pos, stateIn, 2);
			}

			blocksNeedingUpdate.add(pos);

			for (Direction direction1 : Direction.values()) {
				blocksNeedingUpdate.add(pos.offset(direction1));
			}
		}

		return stateIn;
	}

	/**
	 * Calls World.notifyNeighborsOfStateChange() for all neighboring blocks, but
	 * only if the given block is a redstone wire.
	 */
	private void notifyWireNeighborsOfStateChange(World worldIn, BlockPos pos) {
		Block test = worldIn.getBlockState(pos).getBlock();
		if (test == this || test == Blocks.REDSTONE_WIRE) {
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

			for (Direction direction : Direction.values()) {
				notifyWireNeighborsOfStateChange(worldIn, pos.offset(direction));
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

				for (Direction direction : Direction.values()) {
					notifyWireNeighborsOfStateChange(worldIn, pos.offset(direction));
				}
			}
		}
	}

	private int maxSignal(int existingSignal, BlockState neighbor) {
		Block test = neighbor.getBlock();
		if (test != this && test != Blocks.REDSTONE_WIRE) {
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
			updateSurroundingRedstone(worldIn, pos, state);
		}
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return wire.canProvidePower ? blockState.getWeakPower(blockAccess, pos, side) : 0;
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return wire.canProvidePower ? blockState.get(POWER) : 0;
	}

	protected static boolean canConnectTo(BlockState blockState, IBlockReader world, BlockPos pos,
			@Nullable Direction side) {
		Block block = blockState.getBlock();
		if (block == Blocks.REDSTONE_WIRE || block == ModBlocks.REDSTONE_PIPE_BLOCK) {
			return true;
		} else if (block == Blocks.REPEATER) {
			Direction direction = blockState.get(RepeaterBlock.HORIZONTAL_FACING);
			return direction == side || direction.getOpposite() == side;
		} else if (block == Blocks.OBSERVER) {
			return side == blockState.get(ObserverBlock.FACING);
		} else {
			return blockState.canConnectRedstone(world, pos, side) && side != null;
		}
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return wire.canProvidePower;
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
			double d0 = (double) pos.getX() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.8D;
			double d1 = (double) pos.getY() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.8D;
			double d2 = (double) pos.getZ() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.8D;
			float f = (float) i / 15.0F;
			float f1 = f * 0.6F + 0.4F;
			float f2 = Math.max(0.0F, f * f * 0.7F - 0.5F);
			float f3 = Math.max(0.0F, f * f * 0.6F - 0.7F);
			worldIn.addParticle(new RedstoneParticleData(f1, f2, f3, 1.0F), d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	/**
	 * Special hook for remapping our block as redstone wire
	 * @param state
	 * @return
	 */
	public static Block getBlockHook(BlockState state)
	{
		Block block = state.getBlock();
		if (block == ModBlocks.REDSTONE_PIPE_BLOCK)
			block = Blocks.REDSTONE_WIRE;
		return block;
	}

	/**
	 * Maximum signal between input pipe/wire signal and the neighbor state
	 * (It is public so patched wire code can call us)
	 * @param existingSignal
	 * @param neighbor
	 * @return maximum pipe/wire signal
	 */
	public static int maxSignalHook(int existingSignal, BlockState neighbor) {
		Block test = neighbor.getBlock();
		if (test != ModBlocks.REDSTONE_PIPE_BLOCK && test != Blocks.REDSTONE_WIRE) {
			return existingSignal;
		} else {
			int i = neighbor.get(POWER);
			return i > existingSignal ? i : existingSignal;
		}
	}

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

	@SuppressWarnings("deprecation")
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
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, POWER);
	}
}
