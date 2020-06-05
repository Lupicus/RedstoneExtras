package com.lupicus.rsx.block;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks
{
	public static final Block DAYTIME_SENSOR = new DaytimeSensorBlock(Properties.create(Material.WOOD).hardnessAndResistance(0.2F).sound(SoundType.WOOD)).setRegistryName("daytime_sensor");
	public static final Block REDSTONE_POWER_BLOCK = new RedstonePowerBlock(Properties.create(Material.IRON).hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)).setRegistryName("redstone_power_block");
	public static final Block REDSTONE_PIPE_BLOCK = new RedstonePipeBlock(Properties.create(Material.GLASS).notSolid().hardnessAndResistance(0.25F).sound(SoundType.GLASS)).setRegistryName("redstone_pipe_block");
	public static final Block REDSTONE_PULSE_BLOCK = new RedstonePulseBlock(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F).sound(SoundType.WOOD)).setRegistryName("redstone_pulse_block");
	public static final Block REDSTONE_RESISTOR_BLOCK = new RedstoneResistorBlock(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F).sound(SoundType.WOOD)).setRegistryName("redstone_resistor_block");
	public static final Block REDSTONE_BENDER_BLOCK = new RedstoneBenderBlock(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F).sound(SoundType.WOOD)).setRegistryName("redstone_bender_block");
	public static final Block REDSTONE_TEE_BLOCK = new RedstoneTeeBlock(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F).sound(SoundType.WOOD)).setRegistryName("redstone_tee_block");
	public static final Block REDSTONE_STRAIGHT_BLOCK = new RedstoneStraightBlock(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F).sound(SoundType.WOOD)).setRegistryName("redstone_straight_block");
	public static final Block REDSTONE_ENERGY_BLOCK = new RedstoneEnergyBlock(Properties.create(Material.ROCK).hardnessAndResistance(3.5F)).setRegistryName("redstone_energy_block");
	public static final Block BLUESTONE_WIRE = new BluestoneWireBlock(Properties.create(Material.MISCELLANEOUS).doesNotBlockMovement().hardnessAndResistance(0.0F)).setRegistryName("bluestone_wire");

	public static void register(IForgeRegistry<Block> forgeRegistry)
	{
		forgeRegistry.register(DAYTIME_SENSOR);
		forgeRegistry.register(REDSTONE_POWER_BLOCK);
		forgeRegistry.registerAll(REDSTONE_PIPE_BLOCK, REDSTONE_PULSE_BLOCK, REDSTONE_RESISTOR_BLOCK);
		forgeRegistry.registerAll(REDSTONE_BENDER_BLOCK, REDSTONE_TEE_BLOCK, REDSTONE_STRAIGHT_BLOCK);
		forgeRegistry.register(REDSTONE_ENERGY_BLOCK);
		forgeRegistry.register(BLUESTONE_WIRE);
	}

	@OnlyIn(Dist.CLIENT)
	public static void setRenderLayer()
	{
		RenderTypeLookup.setRenderLayer(REDSTONE_PIPE_BLOCK, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(REDSTONE_PULSE_BLOCK, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(REDSTONE_BENDER_BLOCK, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(REDSTONE_TEE_BLOCK, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(REDSTONE_STRAIGHT_BLOCK, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(BLUESTONE_WIRE, RenderType.getCutout());
	}

	@OnlyIn(Dist.CLIENT)
	public static void register(BlockColors blockColors)
	{
		blockColors.register((blockstate, lightreader, pos, index) -> {
			return RedstonePipeBlock.colorMultiplier(blockstate.get(RedstonePipeBlock.POWER));
		}, REDSTONE_PIPE_BLOCK);
		blockColors.register((blockstate, lightreader, pos, index) -> {
			return RedstonePowerBlock.colorMultiplier(blockstate.get(RedstonePowerBlock.POWER));
		}, REDSTONE_POWER_BLOCK);
		blockColors.register((blockstate, lightreader, pos, index) -> {
			return RedstoneBenderBlock.colorMultiplier(blockstate.get(RedstoneBenderBlock.POWER));
		}, REDSTONE_BENDER_BLOCK, REDSTONE_TEE_BLOCK, REDSTONE_STRAIGHT_BLOCK);
		blockColors.register((blockstate, lightreader, pos, index) -> {
			return RedstoneResistorBlock.colorMultiplier(blockstate.get(RedstoneResistorBlock.RESISTANCE));
		}, REDSTONE_RESISTOR_BLOCK);
		blockColors.register((blockstate, lightreader, pos, index) -> {
			return BluestoneWireBlock.colorMultiplier(blockstate.get(BluestoneWireBlock.POWER));
		}, BLUESTONE_WIRE);

		blockColors.addColorState(RedstonePipeBlock.POWER, REDSTONE_PIPE_BLOCK);
		blockColors.addColorState(RedstonePowerBlock.POWER, REDSTONE_POWER_BLOCK);
		blockColors.addColorState(RedstoneBenderBlock.POWER, REDSTONE_BENDER_BLOCK, REDSTONE_TEE_BLOCK, REDSTONE_STRAIGHT_BLOCK);
		blockColors.addColorState(RedstoneResistorBlock.RESISTANCE, REDSTONE_RESISTOR_BLOCK);
		blockColors.addColorState(BluestoneWireBlock.POWER, BLUESTONE_WIRE);
	}
}
