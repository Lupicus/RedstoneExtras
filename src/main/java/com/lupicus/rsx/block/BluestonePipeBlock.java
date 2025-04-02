package com.lupicus.rsx.block;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BluestonePipeBlock extends TransparentBlock
{
	public static final MapCodec<BluestonePipeBlock> CODEC = simpleCodec(BluestonePipeBlock::new);
	public static final EnumProperty<RedstoneSide> UP = RedstonePipeBlock.REDSTONE_UP;
	public static final EnumProperty<RedstoneSide> DOWN = RedstonePipeBlock.REDSTONE_DOWN;
	public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
	public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
	public static final IntegerProperty POWER = BluestoneWireBlock.POWER; // == BlockStateProperties.POWER;
	public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.<Direction, EnumProperty<RedstoneSide>>builder()
			.put(Direction.NORTH, NORTH)
			.put(Direction.EAST, EAST)
			.put(Direction.SOUTH, SOUTH)
			.put(Direction.WEST, WEST)
			.put(Direction.UP, UP)
			.put(Direction.DOWN, DOWN)
			.build());
	private static final int[] COLORS = new int[16];
	private RedStoneWireBlock wire = (RedStoneWireBlock) Blocks.REDSTONE_WIRE;

	@Override
	protected MapCodec<BluestonePipeBlock> codec() {
		return CODEC;
	}

	public BluestonePipeBlock(Properties properties)
	{
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE)
				.setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(UP, RedstoneSide.NONE)
				.setValue(DOWN, RedstoneSide.NONE).setValue(POWER, Integer.valueOf(0)));
	}

	public static boolean isNormalCube(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockGetter iblockreader = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		return defaultBlockState().setValue(WEST, getSide(iblockreader, blockpos, Direction.WEST))
				.setValue(EAST, getSide(iblockreader, blockpos, Direction.EAST))
				.setValue(NORTH, getSide(iblockreader, blockpos, Direction.NORTH))
				.setValue(SOUTH, getSide(iblockreader, blockpos, Direction.SOUTH))
				.setValue(UP, getSide(iblockreader, blockpos, Direction.UP))
				.setValue(DOWN, getSide(iblockreader, blockpos, Direction.DOWN));
	}

	@Override
	protected BlockState updateShape(BlockState stateIn, LevelReader worldIn, ScheduledTickAccess tickAccess, BlockPos currentPos,
			Direction facing, BlockPos facingPos, BlockState facingState, RandomSource rand) {
		return stateIn.setValue(PROPERTY_BY_DIRECTION.get(facing), getSide(worldIn, currentPos, facing));
	}

	private RedstoneSide getSide(BlockGetter worldIn, BlockPos pos, Direction face) {
		BlockPos blockpos = pos.relative(face);
		BlockState blockstate = worldIn.getBlockState(blockpos);
		return shouldConnectTo(blockstate, worldIn, blockpos, face) ? RedstoneSide.SIDE : RedstoneSide.NONE;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return false;
	}

	private void updatePowerStrength(Level worldIn, BlockPos pos, BlockState state) {
		int i = calculateTargetStrength(worldIn, pos);
		if (state.getValue(POWER) != i) {
			if (worldIn.getBlockState(pos) == state) {
				worldIn.setBlock(pos, state.setValue(POWER, Integer.valueOf(i)), 2);
			}

			Set<BlockPos> set = Sets.newHashSet();
			set.add(pos);

			for (Direction direction1 : Direction.values()) {
				set.add(pos.relative(direction1));
			}

			for (BlockPos blockpos : set) {
				worldIn.updateNeighborsAt(blockpos, this);
			}
		}
	}

	private int calculateTargetStrength(Level world, BlockPos pos) {
		wire.shouldSignal = false;
		int i = world.getBestNeighborSignal(pos);
		wire.shouldSignal = true;
		int j = 0;
		if (i < 15) {
			for (Direction direction : Direction.values()) {
				BlockState blockstate1 = world.getBlockState(pos.relative(direction));
				j = Math.max(j, getWireSignal(blockstate1));
			}
		}

		return Math.max(i, j - 1);
	}

	/**
	 * Calls World.updateNeighborsAt() for all neighboring blocks, but
	 * only if the given block is a bluestone wire/pipe.
	 */
	private void updateNeighborsOfNeighboringWires(Level worldIn, BlockPos pos) {
		BlockState state = worldIn.getBlockState(pos);
		if (state.is(this) || state.is(ModBlocks.BLUESTONE_WIRE)) {
			worldIn.updateNeighborsAt(pos, this);

			for (Direction direction : Direction.values()) {
				worldIn.updateNeighborsAt(pos.relative(direction), this);
			}
		}
	}

	@Override
	protected void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!oldState.is(state.getBlock()) && !worldIn.isClientSide) {
			updatePowerStrength(worldIn, pos, state);

			for (Direction direction : Direction.values()) {
				updateNeighborsOfNeighboringWires(worldIn, pos.relative(direction));
			}
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel worldIn, BlockPos pos, boolean isMoving) {
		if (!isMoving) {
			for (Direction direction : Direction.values()) {
				worldIn.updateNeighborsAt(pos.relative(direction), this);
			}
	
			updatePowerStrength(worldIn, pos, state);
	
			for (Direction direction : Direction.values()) {
				updateNeighborsOfNeighboringWires(worldIn, pos.relative(direction));
			}
		}
	}

	private int getWireSignal(BlockState neighbor) {
		return neighbor.is(this) || neighbor.is(ModBlocks.BLUESTONE_WIRE) ? neighbor.getValue(POWER) : 0;
	}

	@Override
	protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, Orientation orient,
			boolean isMoving) {
		if (!worldIn.isClientSide) {
			updatePowerStrength(worldIn, pos, state);
		}
	}

	@Override
	protected int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return wire.shouldSignal ? blockState.getSignal(blockAccess, pos, side) : 0;
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return wire.shouldSignal ? blockState.getValue(POWER) : 0;
	}

	protected static boolean shouldConnectTo(BlockState blockState, BlockGetter world, BlockPos pos,
			Direction side) {
		if (blockState.is(ModBlocks.BLUESTONE_WIRE) || blockState.is(ModBlocks.BLUESTONE_PIPE_BLOCK)) {
			return true;
		} else if (blockState.is(Blocks.REDSTONE_WIRE) || blockState.is(ModBlocks.REDSTONE_PIPE_BLOCK)) {
			return false;
		} else {
			return blockState.canRedstoneConnectTo(world, pos, side);
		}
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return wire.shouldSignal;
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
			double d0 = (double) pos.getX() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.8D;
			double d1 = (double) pos.getY() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.8D;
			double d2 = (double) pos.getZ() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.8D;
			worldIn.addParticle(new DustParticleOptions(COLORS[i], 1.0F), d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rot) {
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

	@Override
	protected BlockState mirror(BlockState state, Mirror mirrorIn) {
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
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, POWER);
	}

	static {
		for (int i = 0; i <= 15; ++i) {
			float f = (float) i / 15.0F;
			float f3 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
			float f2 = Mth.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
			float f1 = Mth.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
			COLORS[i] = ARGB.colorFromFloat(1.0F, f1, f2, f3);
		}
	}
}
