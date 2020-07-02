package com.lupicus.rsx.block;

import java.util.Random;

import com.lupicus.rsx.sound.ModSounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class RedstonePulseBlock extends RedstoneDiodeBlock
{
	public static final BooleanProperty PULSE = BooleanProperty.create("pulse");
	public static final EnumProperty<PulseStrength> STRENGTH = EnumProperty.create("strength", PulseStrength.class);
	public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

	public RedstonePulseBlock(Properties properties) {
		super(properties);
		this.setDefaultState(
				this.stateContainer.getBaseState()
				.with(HORIZONTAL_FACING, Direction.NORTH)
				.with(STRENGTH, PulseStrength.HIGH)
				.with(PULSE, Boolean.valueOf(false))
				.with(POWERED, false)
				.with(INVERTED, false));
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {
		if (!player.abilities.allowEdit) {
			return ActionResultType.PASS;
		} else {
			float f;
			if (player.isSneaking()) {
				state = state.func_235896_a_(STRENGTH); // cycle
				f = state.get(STRENGTH) == PulseStrength.LOW ? 0.5F : 0.55F;
			}
			else {
				state = state.func_235896_a_(INVERTED); // cycle
				f = state.get(INVERTED) ? 0.5F : 0.55F;
			}
			world.playSound(player, pos, ModSounds.REDSTONE_PULSE_CLICK, SoundCategory.BLOCKS, 0.3F, f);
			world.setBlockState(pos, state, 2);
			return ActionResultType.SUCCESS;
		}
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		updateState(worldIn, pos, state);
	}

	@Override
	protected void updateState(World world, BlockPos pos, BlockState state)
	{
		if (!world.isRemote)
		{
			boolean flag = state.get(POWERED);
			boolean flag1 = shouldBePowered(world, pos, state);
			if (flag != flag1)
			{
				boolean flag2 = flag1;
				if (state.get(INVERTED)) flag2 = !flag2;
				if (flag2)
				{
					world.getPendingBlockTicks().scheduleTick(pos, this, 1);
					state = state.with(POWERED, flag1).with(PULSE, true);
					world.setBlockState(pos, state, 3);
				}
				else
				{
					state = state.with(POWERED, flag1);
					world.setBlockState(pos, state, 2);
				}
			}
		}
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return getWeakPower(blockState, blockAccess, pos, side);
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.get(HORIZONTAL_FACING) == side ? this.getActiveSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
		world.setBlockState(pos, state.with(PULSE, false), 3);
	}

	@Override
	protected int getActiveSignal(IBlockReader worldIn, BlockPos pos, BlockState state) {
		if (state.get(PULSE))
			return state.get(STRENGTH) == PulseStrength.HIGH ? 15 : 1;
		return 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		Direction facing = state.get(HORIZONTAL_FACING);
		return facing == side || facing.getOpposite() == side;
	}

	@Override
	protected int getDelay(BlockState p_196346_1_) {
		return 0;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, STRENGTH, PULSE, POWERED, INVERTED);
	}

	public enum PulseStrength implements IStringSerializable
	{
		LOW("low"),
		HIGH("high");

		private final String name;

		private PulseStrength(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.func_176610_l(); // getName
		}

		@Override
		public String func_176610_l() { // getName
			return this.name;
		}
	}
}
