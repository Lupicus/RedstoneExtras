package com.lupicus.rsx.block;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks
{
	public static final Block DAYTIME_SENSOR = new DaytimeSensorBlock(Properties.of(Material.WOOD).strength(0.2F).sound(SoundType.WOOD)).setRegistryName("daytime_sensor");
	public static final Block REDSTONE_POWER_BLOCK = new RedstonePowerBlock(Properties.of(Material.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL).isRedstoneConductor(RedstonePowerBlock::isNormalCube)).setRegistryName("redstone_power_block");
	public static final Block REDSTONE_PIPE_BLOCK = new RedstonePipeBlock(Properties.of(Material.GLASS).noOcclusion().strength(0.25F).sound(SoundType.GLASS).isRedstoneConductor(RedstonePipeBlock::isNormalCube)).setRegistryName("redstone_pipe_block");
	public static final Block REDSTONE_PULSE_BLOCK = new RedstonePulseBlock(Properties.of(Material.DECORATION).instabreak().sound(SoundType.WOOD)).setRegistryName("redstone_pulse_block");
	public static final Block REDSTONE_RESISTOR_BLOCK = new RedstoneResistorBlock(Properties.of(Material.DECORATION).instabreak().sound(SoundType.WOOD)).setRegistryName("redstone_resistor_block");
	public static final Block REDSTONE_BENDER_BLOCK = new RedstoneBenderBlock(Properties.of(Material.DECORATION).instabreak().sound(SoundType.WOOD)).setRegistryName("redstone_bender_block");
	public static final Block REDSTONE_TEE_BLOCK = new RedstoneTeeBlock(Properties.of(Material.DECORATION).instabreak().sound(SoundType.WOOD)).setRegistryName("redstone_tee_block");
	public static final Block REDSTONE_STRAIGHT_BLOCK = new RedstoneStraightBlock(Properties.of(Material.DECORATION).instabreak().sound(SoundType.WOOD)).setRegistryName("redstone_straight_block");
	public static final Block REDSTONE_ENERGY_BLOCK = new RedstoneEnergyBlock(Properties.of(Material.STONE).strength(3.5F).isRedstoneConductor(RedstoneEnergyBlock::isNormalCube)).setRegistryName("redstone_energy_block");
	public static final Block BLUESTONE_WIRE = new BluestoneWireBlock(Properties.of(Material.DECORATION).noCollission().instabreak()).setRegistryName("bluestone_wire");
	public static final Block BLUESTONE_PIPE_BLOCK = new BluestonePipeBlock(Properties.of(Material.GLASS).noOcclusion().strength(0.25F).sound(SoundType.GLASS).isRedstoneConductor(BluestonePipeBlock::isNormalCube)).setRegistryName("bluestone_pipe_block");

	public static void register(IForgeRegistry<Block> forgeRegistry)
	{
		forgeRegistry.register(DAYTIME_SENSOR);
		forgeRegistry.register(REDSTONE_POWER_BLOCK);
		forgeRegistry.registerAll(REDSTONE_PIPE_BLOCK, REDSTONE_PULSE_BLOCK, REDSTONE_RESISTOR_BLOCK);
		forgeRegistry.registerAll(REDSTONE_BENDER_BLOCK, REDSTONE_TEE_BLOCK, REDSTONE_STRAIGHT_BLOCK);
		forgeRegistry.register(REDSTONE_ENERGY_BLOCK);
		forgeRegistry.registerAll(BLUESTONE_WIRE, BLUESTONE_PIPE_BLOCK);
	}

	@OnlyIn(Dist.CLIENT)
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
	public static void register(BlockColors blockColors)
	{
		blockColors.register((blockstate, lightreader, pos, index) -> {
			return RedstonePipeBlock.colorMultiplier(blockstate.getValue(RedstonePipeBlock.POWER));
		}, REDSTONE_PIPE_BLOCK);
		blockColors.register((blockstate, lightreader, pos, index) -> {
			return RedstonePowerBlock.colorMultiplier(blockstate.getValue(RedstonePowerBlock.POWER));
		}, REDSTONE_POWER_BLOCK);
		blockColors.register((blockstate, lightreader, pos, index) -> {
			return RedstoneBenderBlock.colorMultiplier(blockstate.getValue(RedstoneBenderBlock.POWER));
		}, REDSTONE_BENDER_BLOCK, REDSTONE_TEE_BLOCK, REDSTONE_STRAIGHT_BLOCK);
		blockColors.register((blockstate, lightreader, pos, index) -> {
			return RedstoneResistorBlock.colorMultiplier(blockstate.getValue(RedstoneResistorBlock.RESISTANCE));
		}, REDSTONE_RESISTOR_BLOCK);
		blockColors.register((blockstate, lightreader, pos, index) -> {
			return BluestoneWireBlock.colorMultiplier(blockstate.getValue(BluestoneWireBlock.POWER));
		}, BLUESTONE_WIRE);
		blockColors.register((blockstate, lightreader, pos, index) -> {
			return BluestonePipeBlock.colorMultiplier(blockstate.getValue(BluestonePipeBlock.POWER));
		}, BLUESTONE_PIPE_BLOCK);

		blockColors.addColoringState(RedstonePipeBlock.POWER, REDSTONE_PIPE_BLOCK);
		blockColors.addColoringState(RedstonePowerBlock.POWER, REDSTONE_POWER_BLOCK);
		blockColors.addColoringState(RedstoneBenderBlock.POWER, REDSTONE_BENDER_BLOCK, REDSTONE_TEE_BLOCK, REDSTONE_STRAIGHT_BLOCK);
		blockColors.addColoringState(RedstoneResistorBlock.RESISTANCE, REDSTONE_RESISTOR_BLOCK);
		blockColors.addColoringState(BluestoneWireBlock.POWER, BLUESTONE_WIRE);
		blockColors.addColoringState(BluestonePipeBlock.POWER, BLUESTONE_PIPE_BLOCK);
	}
}
