package com.lupicus.rsx.block;

import com.lupicus.rsx.sound.ModSounds;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class RedstonePulseBlock extends DiodeBlock
{
	public static final MapCodec<RedstonePulseBlock> CODEC = simpleCodec(RedstonePulseBlock::new);
	public static final BooleanProperty PULSE = BooleanProperty.create("pulse");
	public static final EnumProperty<PulseStrength> STRENGTH = EnumProperty.create("strength", PulseStrength.class);
	public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

	@Override
	protected MapCodec<RedstonePulseBlock> codec() {
		return CODEC;
	}

	public RedstonePulseBlock(Properties properties) {
		super(properties);
		registerDefaultState(
				stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(STRENGTH, PulseStrength.HIGH)
				.setValue(PULSE, Boolean.valueOf(false))
				.setValue(POWERED, false)
				.setValue(INVERTED, false));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
			BlockHitResult result) {
		if (!player.mayBuild()) {
			return InteractionResult.PASS;
		} else {
			float f;
			if (player.isSecondaryUseActive()) {
				state = state.cycle(STRENGTH);
				f = state.getValue(STRENGTH) == PulseStrength.LOW ? 0.5F : 0.55F;
			}
			else {
				state = state.cycle(INVERTED);
				f = state.getValue(INVERTED) ? 0.5F : 0.55F;
			}
			world.playSound(player, pos, ModSounds.REDSTONE_PULSE_CLICK, SoundSource.BLOCKS, 0.3F, f);
			world.setBlock(pos, state, 2);
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickAccess, BlockPos pos,
			Direction dir, BlockPos dirPos, BlockState dirState, RandomSource rand) {
		if (dir == Direction.DOWN)
			return !canSurviveOn(world, dirPos, dirState) ? Blocks.AIR.defaultBlockState() : state;
		return super.updateShape(state, world, tickAccess, pos, dir, dirPos, dirState, rand);
	}

	@Override
	protected void checkTickOnNeighbor(Level world, BlockPos pos, BlockState state)
	{
		if (!world.isClientSide)
		{
			boolean flag = state.getValue(POWERED);
			boolean flag1 = shouldTurnOn(world, pos, state);
			if (flag != flag1)
			{
				boolean flag2 = flag1;
				if (state.getValue(INVERTED)) flag2 = !flag2;
				if (flag2)
				{
					state = state.setValue(POWERED, flag1).setValue(PULSE, true);
					world.setBlock(pos, state, 3);
					world.neighborChanged(state, pos, (Block) null, null, false); // do special case below
				}
				else
				{
					state = state.setValue(POWERED, flag1);
					world.setBlock(pos, state, 2);
				}
			}
		}
	}

	@Override
	protected int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return getSignal(blockState, blockAccess, pos, side);
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return blockState.getValue(FACING) == side ? this.getOutputSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level world, BlockPos pos, int id, int param) {
		world.setBlock(pos, state.setValue(PULSE, false), 3);
		return false;
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
		checkTickOnNeighbor(world, pos, state);
	}

	@Override
	protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, Orientation orient, boolean isMoving) {
		if (!world.isClientSide && blockIn == null) {
			world.blockEvent(pos, this, 0, 0);
		}
		else
			super.neighborChanged(state, world, pos, blockIn, orient, isMoving);
	}

	@Override
	protected int getOutputSignal(BlockGetter worldIn, BlockPos pos, BlockState state) {
		if (state.getValue(PULSE))
			return state.getValue(STRENGTH) == PulseStrength.HIGH ? 15 : 1;
		return 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		Direction facing = state.getValue(FACING);
		return facing == side || facing.getOpposite() == side;
	}

	@Override
	protected int getDelay(BlockState p_196346_1_) {
		return 0;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, STRENGTH, PULSE, POWERED, INVERTED);
	}

	public enum PulseStrength implements StringRepresentable
	{
		LOW("low"),
		HIGH("high");

		private final String name;

		private PulseStrength(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.getSerializedName();
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
