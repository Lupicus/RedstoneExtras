package com.lupicus.rsx.item;

import com.lupicus.rsx.block.ModBlocks;
import com.lupicus.rsx.block.RedstoneBenderBlock;
import com.lupicus.rsx.block.RedstonePowerBlock;
import com.lupicus.rsx.block.RedstoneResistorBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems
{
	public static final Item DAYTIME_SENSOR_BLOCK = new BlockItem(ModBlocks.DAYTIME_SENSOR, new Properties());
	public static final Item REDSTONE_POWER_BLOCK = new BlockItem(ModBlocks.REDSTONE_POWER_BLOCK, new Properties());
	public static final Item REDSTONE_PIPE_BLOCK = new BlockItem(ModBlocks.REDSTONE_PIPE_BLOCK, new Properties());
	public static final Item REDSTONE_PULSE_BLOCK = new BlockItem(ModBlocks.REDSTONE_PULSE_BLOCK, new Properties());
	public static final Item REDSTONE_RESISTOR_BLOCK = new BlockItem(ModBlocks.REDSTONE_RESISTOR_BLOCK, new Properties());
	public static final Item REDSTONE_BENDER_BLOCK = new BlockItem(ModBlocks.REDSTONE_BENDER_BLOCK, new Properties());
	public static final Item REDSTONE_TEE_BLOCK = new BlockItem(ModBlocks.REDSTONE_TEE_BLOCK, new Properties());
	public static final Item REDSTONE_STRAIGHT_BLOCK = new BlockItem(ModBlocks.REDSTONE_STRAIGHT_BLOCK, new Properties());
	public static final Item REDSTONE_ENERGY_BLOCK = new BlockItem(ModBlocks.REDSTONE_ENERGY_BLOCK, new Properties());
	public static final Item BLUESTONE = new ItemNameBlockItem(ModBlocks.BLUESTONE_WIRE, new Item.Properties());
	public static final Item BLUESTONE_PIPE_BLOCK = new BlockItem(ModBlocks.BLUESTONE_PIPE_BLOCK, new Properties());

	public static void register(IForgeRegistry<Item> forgeRegistry)
	{
		forgeRegistry.register("daytime_sensor", DAYTIME_SENSOR_BLOCK);
		forgeRegistry.register("redstone_power_block", REDSTONE_POWER_BLOCK);
		forgeRegistry.register("redstone_pipe_block", REDSTONE_PIPE_BLOCK);
		forgeRegistry.register("redstone_pulse_block", REDSTONE_PULSE_BLOCK);
		forgeRegistry.register("redstone_resistor_block", REDSTONE_RESISTOR_BLOCK);
		forgeRegistry.register("redstone_bender_block", REDSTONE_BENDER_BLOCK);
		forgeRegistry.register("redstone_tee_block", REDSTONE_TEE_BLOCK);
		forgeRegistry.register("redstone_straight_block", REDSTONE_STRAIGHT_BLOCK);
		forgeRegistry.register("redstone_energy_block", REDSTONE_ENERGY_BLOCK);
		forgeRegistry.register("bluestone", BLUESTONE);
		forgeRegistry.register("bluestone_pipe_block", BLUESTONE_PIPE_BLOCK);
	}

	public static void setupTabs(CreativeModeTabEvent.BuildContents event)
	{
		if (event.getTab() == CreativeModeTabs.REDSTONE_BLOCKS)
		{
			event.accept(BLUESTONE);
			event.accept(REDSTONE_PIPE_BLOCK);
			event.accept(BLUESTONE_PIPE_BLOCK);
			event.accept(REDSTONE_POWER_BLOCK);
			event.accept(REDSTONE_BENDER_BLOCK);
			event.accept(REDSTONE_STRAIGHT_BLOCK);
			event.accept(REDSTONE_TEE_BLOCK);
			event.accept(REDSTONE_PULSE_BLOCK);
			event.accept(REDSTONE_RESISTOR_BLOCK);
			event.accept(DAYTIME_SENSOR_BLOCK);
			event.accept(REDSTONE_ENERGY_BLOCK);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void register(RegisterColorHandlersEvent.Item event)
	{
		event.register((itemstack, index) -> {
			BlockState blockstate = ((BlockItem)itemstack.getItem()).getBlock().defaultBlockState();
			return RedstonePowerBlock.colorMultiplier(blockstate.getValue(RedStoneWireBlock.POWER));
		}, REDSTONE_POWER_BLOCK);
		event.register((itemstack, index) -> {
			return RedstoneBenderBlock.colorMultiplier(15);
		}, REDSTONE_BENDER_BLOCK, REDSTONE_TEE_BLOCK, REDSTONE_STRAIGHT_BLOCK);
		event.register((itemstack, index) -> {
			BlockState blockstate = ((BlockItem)itemstack.getItem()).getBlock().defaultBlockState();
			return RedstoneResistorBlock.colorMultiplier(blockstate.getValue(RedstoneResistorBlock.RESISTANCE));
		}, REDSTONE_RESISTOR_BLOCK);
	}
}
