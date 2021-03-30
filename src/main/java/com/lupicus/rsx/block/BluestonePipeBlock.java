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
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BluestonePipeBlock extends Block
{
	public static final EnumProperty<RedstoneSide> UP = RedstonePipeBlock.REDSTONE_UP;
	public static final EnumProperty<RedstoneSide> DOWN = RedstonePipeBlock.REDSTONE_DOWN;
	public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.REDSTONE_NORTH;
	public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.REDSTONE_EAST;
	public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.REDSTONE_SOUTH;
	public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.REDSTONE_WEST;
	public static final IntegerProperty POWER = BluestoneWireBlock.POWER; // == BlockStateProperties.POWER_0_15;
	public static final Map<Direction, EnumProperty<RedstoneSide>> FACING_PROPERTY_MAP = Maps.newEnumMap(ImmutableMap.<Direction, EnumProperty<RedstoneSide>>builder()
			.put(Direction.NORTH, NORTH)
			.put(Direction.EAST, EAST)
			.put(Direction.SOUTH, SOUTH)
			.put(Direction.WEST, WEST)
			.put(Direction.UP, UP)
			.put(Direction.DOWN, DOWN)
			.build());
	private static final Vector3f[] COLORS = new Vector3f[16];
	private RedstoneWireBlock wire = (RedstoneWireBlock) Blocks.REDSTONE_WIRE;

	public BluestonePipeBlock(Properties properties)
	{
		super(properties);
		setDefaultState(stateContainer.getBaseState().with(NORTH, RedstoneSide.NONE).with(EAST, RedstoneSide.NONE)
				.with(SOUTH, RedstoneSide.NONE).with(WEST, RedstoneSide.NONE).with(UP, RedstoneSide.NONE)
				.with(DOWN, RedstoneSide.NONE).with(POWER, Integer.valueOf(0)));
	}

	public static boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
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

	private void updateSurroundingRedstone(World worldIn, BlockPos pos, BlockState state) {
		int i = func_212568_b(worldIn, pos);
		if (state.get(POWER) != i) {
			if (worldIn.getBlockState(pos) == state) {
				worldIn.setBlockState(pos, state.with(POWER, Integer.valueOf(i)), 2);
			}

			Set<BlockPos> set = Sets.newHashSet();
			set.add(pos);

			for (Direction direction1 : Direction.values()) {
				set.add(pos.offset(direction1));
			}

			for (BlockPos blockpos : set) {
				worldIn.notifyNeighborsOfStateChange(blockpos, this);
			}
		}
	}

	private int func_212568_b(World world, BlockPos pos) {
		wire.canProvidePower = false;
		int i = world.getRedstonePowerFromNeighbors(pos);
		wire.canProvidePower = true;
		int j = 0;
		if (i < 15) {
			for (Direction direction : Direction.values()) {
				BlockState blockstate1 = world.getBlockState(pos.offset(direction));
				j = Math.max(j, getPower(blockstate1));
			}
		}

		return Math.max(i, j - 1);
	}

	/**
	 * Calls World.notifyNeighborsOfStateChange() for all neighboring blocks, but
	 * only if the given block is a bluestone wire.
	 */
	private void notifyWireNeighborsOfStateChange(World worldIn, BlockPos pos) {
		BlockState state = worldIn.getBlockState(pos);
		if (state.isIn(this) || state.isIn(ModBlocks.BLUESTONE_WIRE)) {
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

			for (Direction direction : Direction.values()) {
				notifyWireNeighborsOfStateChange(worldIn, pos.offset(direction));
			}
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

				for (Direction direction : Direction.values()) {
					notifyWireNeighborsOfStateChange(worldIn, pos.offset(direction));
				}
			}
		}
	}

	private int getPower(BlockState neighbor) {
		return neighbor.isIn(this) || neighbor.isIn(ModBlocks.BLUESTONE_WIRE) ? neighbor.get(POWER) : 0;
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
		if (blockState.isIn(ModBlocks.BLUESTONE_WIRE) || blockState.isIn(ModBlocks.BLUESTONE_PIPE_BLOCK)) {
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

	@Override
	public boolean canProvidePower(BlockState state) {
		return wire.canProvidePower;
	}

	@OnlyIn(Dist.CLIENT)
	public static int colorMultiplier(int power) {
		Vector3f vector3f = COLORS[power];
		return MathHelper.rgb(vector3f.getX(), vector3f.getY(), vector3f.getZ());
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
			Vector3f vec = COLORS[i];
			worldIn.addParticle(new RedstoneParticleData(vec.getX(), vec.getY(), vec.getZ(), 1.0F), d0, d1, d2, 0.0D, 0.0D, 0.0D);
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
