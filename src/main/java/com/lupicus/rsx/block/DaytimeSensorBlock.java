package com.lupicus.rsx.block;

import javax.annotation.Nullable;

import com.lupicus.rsx.tileentity.DaytimeSensorTileEntity;
import com.lupicus.rsx.tileentity.ModTileEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class DaytimeSensorBlock extends DaylightDetectorBlock
{
	public static final MapCodec<DaylightDetectorBlock> CODEC = simpleCodec(DaytimeSensorBlock::new);
	private static long time;
	private static ResourceKey<Level> dim;
	private static int skyDarken;

	@Override
	public MapCodec<DaylightDetectorBlock> codec() {
		return CODEC;
	}

	public DaytimeSensorBlock(Properties properties) {
		super(properties);
	}

	private static void updateSignalStrength(BlockState state, Level world, BlockPos pos)
	{
		long time = world.getDayTime();
		ResourceKey<Level> curdim = world.dimension();
		if (time != DaytimeSensorBlock.time || curdim != dim)
		{
			DaytimeSensorBlock.time = time;
			dim = curdim;
			// updateSkyBrightness without rain and thunder
			double d2 = 0.5D + 2.0D * Mth
					.clamp((double) Mth.cos(world.getSunAngle(1.0F)), -0.25D, 0.25D);
			skyDarken = (int) ((1.0D - d2) * 11.0D);
		}
		int i = (skyDarken < 4) ? 15 : 0;
		if (state.getValue(INVERTED)) {
			i = 15 - i;
		}
		if (state.getValue(POWER) != i) {
			world.setBlock(pos, state.setValue(POWER, Integer.valueOf(i)), 3);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		if (player.mayBuild()) {
			if (worldIn.isClientSide) {
				return InteractionResult.SUCCESS;
			} else {
				BlockState blockstate = state.cycle(INVERTED);
				worldIn.setBlock(pos, blockstate, 2);
				worldIn.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, blockstate));
				updateSignalStrength(blockstate, worldIn, pos);
				return InteractionResult.CONSUME;
			}
		} else {
			return super.useWithoutItem(state, worldIn, pos, player, result);
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DaytimeSensorTileEntity(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return !world.isClientSide && world.dimensionType().hasSkyLight() ? createTickerHelper(type, ModTileEntities.DAYTIME_SENSOR, DaytimeSensorBlock::tickEntity) : null;
	}

	private static void tickEntity(Level world, BlockPos pos, BlockState state, DaytimeSensorTileEntity blockEntity) {
		if (world.getGameTime() % 20L == 0L) {
			updateSignalStrength(state, world, pos);
		}
	}
}
