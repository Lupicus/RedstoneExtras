package com.lupicus.rsx.item;

import com.lupicus.rsx.block.ModBlocks;
import com.lupicus.rsx.block.RedstoneBenderBlock;
import com.lupicus.rsx.block.RedstonePowerBlock;
import com.lupicus.rsx.block.RedstoneResistorBlock;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems
{
	public static final Item DAYTIME_SENSOR_BLOCK = new BlockItem(ModBlocks.DAYTIME_SENSOR, new Properties().tab(CreativeModeTab.TAB_REDSTONE)).setRegistryName("daytime_sensor");
	public static final Item REDSTONE_POWER_BLOCK = new BlockItem(ModBlocks.REDSTONE_POWER_BLOCK, new Properties().tab(CreativeModeTab.TAB_REDSTONE)).setRegistryName("redstone_power_block");
	public static final Item REDSTONE_PIPE_BLOCK = new BlockItem(ModBlocks.REDSTONE_PIPE_BLOCK, new Properties().tab(CreativeModeTab.TAB_REDSTONE)).setRegistryName("redstone_pipe_block");
	public static final Item REDSTONE_PULSE_BLOCK = new BlockItem(ModBlocks.REDSTONE_PULSE_BLOCK, new Properties().tab(CreativeModeTab.TAB_REDSTONE)).setRegistryName("redstone_pulse_block");
	public static final Item REDSTONE_RESISTOR_BLOCK = new BlockItem(ModBlocks.REDSTONE_RESISTOR_BLOCK, new Properties().tab(CreativeModeTab.TAB_REDSTONE)).setRegistryName("redstone_resistor_block");
	public static final Item REDSTONE_BENDER_BLOCK = new BlockItem(ModBlocks.REDSTONE_BENDER_BLOCK, new Properties().tab(CreativeModeTab.TAB_REDSTONE)).setRegistryName("redstone_bender_block");
	public static final Item REDSTONE_TEE_BLOCK = new BlockItem(ModBlocks.REDSTONE_TEE_BLOCK, new Properties().tab(CreativeModeTab.TAB_REDSTONE)).setRegistryName("redstone_tee_block");
	public static final Item REDSTONE_STRAIGHT_BLOCK = new BlockItem(ModBlocks.REDSTONE_STRAIGHT_BLOCK, new Properties().tab(CreativeModeTab.TAB_REDSTONE)).setRegistryName("redstone_straight_block");
	public static final Item REDSTONE_ENERGY_BLOCK = new BlockItem(ModBlocks.REDSTONE_ENERGY_BLOCK, new Properties().tab(CreativeModeTab.TAB_REDSTONE)).setRegistryName("redstone_energy_block");
	public static final Item BLUESTONE = new ItemNameBlockItem(ModBlocks.BLUESTONE_WIRE, new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)).setRegistryName("bluestone");
	public static final Item BLUESTONE_PIPE_BLOCK = new BlockItem(ModBlocks.BLUESTONE_PIPE_BLOCK, new Properties().tab(CreativeModeTab.TAB_REDSTONE)).setRegistryName("bluestone_pipe_block");

	public static void register(IForgeRegistry<Item> forgeRegistry)
	{
		forgeRegistry.register(DAYTIME_SENSOR_BLOCK);
		forgeRegistry.register(REDSTONE_POWER_BLOCK);
		forgeRegistry.registerAll(REDSTONE_PIPE_BLOCK, REDSTONE_PULSE_BLOCK, REDSTONE_RESISTOR_BLOCK);
		forgeRegistry.registerAll(REDSTONE_BENDER_BLOCK, REDSTONE_TEE_BLOCK, REDSTONE_STRAIGHT_BLOCK);
		forgeRegistry.register(REDSTONE_ENERGY_BLOCK);
		forgeRegistry.registerAll(BLUESTONE, BLUESTONE_PIPE_BLOCK);
	}

	@OnlyIn(Dist.CLIENT)
	public static void register(ItemColors itemColors)
	{
		itemColors.register((itemstack, index) -> {
			BlockState blockstate = ((BlockItem)itemstack.getItem()).getBlock().defaultBlockState();
			return RedstonePowerBlock.colorMultiplier(blockstate.getValue(RedStoneWireBlock.POWER));
		}, REDSTONE_POWER_BLOCK);
		itemColors.register((itemstack, index) -> {
			return RedstoneBenderBlock.colorMultiplier(15);
		}, REDSTONE_BENDER_BLOCK, REDSTONE_TEE_BLOCK, REDSTONE_STRAIGHT_BLOCK);
		itemColors.register((itemstack, index) -> {
			BlockState blockstate = ((BlockItem)itemstack.getItem()).getBlock().defaultBlockState();
			return RedstoneResistorBlock.colorMultiplier(blockstate.getValue(RedstoneResistorBlock.RESISTANCE));
		}, REDSTONE_RESISTOR_BLOCK);
	}
}
