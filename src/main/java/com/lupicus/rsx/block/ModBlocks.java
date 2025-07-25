package com.lupicus.rsx.block;

import java.util.function.Function;

import com.lupicus.rsx.Main;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks
{
	public static final Block DAYTIME_SENSOR = register("daytime_sensor", DaytimeSensorBlock::new, Properties.of().mapColor(MapColor.WOOD).strength(0.2F).sound(SoundType.WOOD).ignitedByLava());
	public static final Block REDSTONE_POWER_BLOCK = register("redstone_power_block", RedstonePowerBlock::new, Properties.of().mapColor(MapColor.FIRE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL).isRedstoneConductor(RedstonePowerBlock::isNormalCube));
	public static final Block REDSTONE_PIPE_BLOCK = register("redstone_pipe_block", RedstonePipeBlock::new, Properties.of().mapColor(DyeColor.RED).noOcclusion().strength(0.3F).sound(SoundType.GLASS).isRedstoneConductor(RedstonePipeBlock::isNormalCube).isValidSpawn(ModBlocks::never).isSuffocating(ModBlocks::never).isViewBlocking(ModBlocks::never));
	public static final Block REDSTONE_PULSE_BLOCK = register("redstone_pulse_block", RedstonePulseBlock::new, Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY));
	public static final Block REDSTONE_RESISTOR_BLOCK = register("redstone_resistor_block", RedstoneResistorBlock::new, Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY));
	public static final Block REDSTONE_BENDER_BLOCK = register("redstone_bender_block", RedstoneBenderBlock::new, Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY));
	public static final Block REDSTONE_TEE_BLOCK = register("redstone_tee_block", RedstoneTeeBlock::new, Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY));
	public static final Block REDSTONE_STRAIGHT_BLOCK = register("redstone_straight_block", RedstoneStraightBlock::new, Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY));
	public static final Block REDSTONE_ENERGY_BLOCK = register("redstone_energy_block", RedstoneEnergyBlock::new, Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.5F).isRedstoneConductor(RedstoneEnergyBlock::isNormalCube));
	public static final Block BLUESTONE_WIRE = register("bluestone_wire", BluestoneWireBlock::new, Properties.of().noCollission().instabreak().pushReaction(PushReaction.DESTROY));
	public static final Block BLUESTONE_PIPE_BLOCK = register("bluestone_pipe_block", BluestonePipeBlock::new, Properties.of().mapColor(DyeColor.BLUE).noOcclusion().strength(0.3F).sound(SoundType.GLASS).isRedstoneConductor(BluestonePipeBlock::isNormalCube).isValidSpawn(ModBlocks::never).isSuffocating(ModBlocks::never).isViewBlocking(ModBlocks::never));

	public static void register(IForgeRegistry<Block> forgeRegistry)
	{
	}

	private static Block register(String name, Function<Properties, Block> func, Properties prop)
	{
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Main.MODID, name));
		return register(key, func, prop);
	}

	// same as Blocks#register
	private static Block register(ResourceKey<Block> key, Function<Properties, Block> func, Properties prop) {
		Block block = func.apply(prop.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("removal")
	public static void setRenderLayer()
	{
		ItemBlockRenderTypes.setRenderLayer(REDSTONE_PIPE_BLOCK, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(REDSTONE_PULSE_BLOCK, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(REDSTONE_BENDER_BLOCK, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(REDSTONE_TEE_BLOCK, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(REDSTONE_STRAIGHT_BLOCK, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BLUESTONE_WIRE, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BLUESTONE_PIPE_BLOCK, RenderType.cutout());
	}

	@OnlyIn(Dist.CLIENT)
	public static void register(RegisterColorHandlersEvent.Block event)
	{
		event.register((blockstate, lightreader, pos, index) -> {
			return RedstonePipeBlock.getColorForPower(blockstate.getValue(RedstonePipeBlock.POWER));
		}, REDSTONE_PIPE_BLOCK);
		event.register((blockstate, lightreader, pos, index) -> {
			return RedstonePowerBlock.getColorForPower(blockstate.getValue(RedstonePowerBlock.POWER));
		}, REDSTONE_POWER_BLOCK);
		event.register((blockstate, lightreader, pos, index) -> {
			return RedstoneBenderBlock.getColorForPower(blockstate.getValue(RedstoneBenderBlock.POWER));
		}, REDSTONE_BENDER_BLOCK, REDSTONE_TEE_BLOCK, REDSTONE_STRAIGHT_BLOCK);
		event.register((blockstate, lightreader, pos, index) -> {
			return RedstoneResistorBlock.getColorForResistance(blockstate.getValue(RedstoneResistorBlock.RESISTANCE));
		}, REDSTONE_RESISTOR_BLOCK);
		event.register((blockstate, lightreader, pos, index) -> {
			return BluestoneWireBlock.getColorForPower(blockstate.getValue(BluestoneWireBlock.POWER));
		}, BLUESTONE_WIRE);
		event.register((blockstate, lightreader, pos, index) -> {
			return BluestonePipeBlock.getColorForPower(blockstate.getValue(BluestonePipeBlock.POWER));
		}, BLUESTONE_PIPE_BLOCK);

		BlockColors blockColors = event.getBlockColors();
		blockColors.addColoringState(RedstonePipeBlock.POWER, REDSTONE_PIPE_BLOCK);
		blockColors.addColoringState(RedstonePowerBlock.POWER, REDSTONE_POWER_BLOCK);
		blockColors.addColoringState(RedstoneBenderBlock.POWER, REDSTONE_BENDER_BLOCK, REDSTONE_TEE_BLOCK, REDSTONE_STRAIGHT_BLOCK);
		blockColors.addColoringState(RedstoneResistorBlock.RESISTANCE, REDSTONE_RESISTOR_BLOCK);
		blockColors.addColoringState(BluestoneWireBlock.POWER, BLUESTONE_WIRE);
		blockColors.addColoringState(BluestonePipeBlock.POWER, BLUESTONE_PIPE_BLOCK);
	}

	private static boolean never(BlockState state, BlockGetter w, BlockPos pos)
	{
		return false;
	}

	private static boolean never(BlockState state, BlockGetter w, BlockPos pos, EntityType<?> type)
	{
		return false;
	}
}
