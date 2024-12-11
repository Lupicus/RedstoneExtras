package com.lupicus.rsx.item;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.lupicus.rsx.Main;
import com.lupicus.rsx.block.ModBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems
{
	public static final Item DAYTIME_SENSOR_BLOCK = register(ModBlocks.DAYTIME_SENSOR, BlockItem::new, new Properties());
	public static final Item REDSTONE_POWER_BLOCK = register(ModBlocks.REDSTONE_POWER_BLOCK, BlockItem::new, new Properties());
	public static final Item REDSTONE_PIPE_BLOCK = register(ModBlocks.REDSTONE_PIPE_BLOCK, BlockItem::new, new Properties());
	public static final Item REDSTONE_PULSE_BLOCK = register(ModBlocks.REDSTONE_PULSE_BLOCK, BlockItem::new, new Properties());
	public static final Item REDSTONE_RESISTOR_BLOCK = register(ModBlocks.REDSTONE_RESISTOR_BLOCK, BlockItem::new, new Properties());
	public static final Item REDSTONE_BENDER_BLOCK = register(ModBlocks.REDSTONE_BENDER_BLOCK, BlockItem::new, new Properties());
	public static final Item REDSTONE_TEE_BLOCK = register(ModBlocks.REDSTONE_TEE_BLOCK, BlockItem::new, new Properties());
	public static final Item REDSTONE_STRAIGHT_BLOCK = register(ModBlocks.REDSTONE_STRAIGHT_BLOCK, BlockItem::new, new Properties());
	public static final Item REDSTONE_ENERGY_BLOCK = register(ModBlocks.REDSTONE_ENERGY_BLOCK, BlockItem::new, new Properties());
	public static final Item BLUESTONE = register("bluestone", createBlockItemWithCustomItemName(ModBlocks.BLUESTONE_WIRE), new Item.Properties());
	public static final Item BLUESTONE_PIPE_BLOCK = register(ModBlocks.BLUESTONE_PIPE_BLOCK, BlockItem::new, new Properties());

	public static void register(IForgeRegistry<Item> forgeRegistry)
	{
	}

	private static Item register(String name, Function<Properties, Item> func, Properties prop)
	{
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Main.MODID, name));
		return Items.registerItem(key, func, prop);
	}

	private static Item register(Block block, BiFunction<Block, Properties, Item> func, Properties prop)
	{
		return Items.registerBlock(block, func, prop);
	}

    private static Function<Item.Properties, Item> createBlockItemWithCustomItemName(Block block) {
        return prop -> new BlockItem(block, prop.useItemDescriptionPrefix());
    }

	public static void setupTabs(BuildCreativeModeTabContentsEvent event)
	{
		if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS)
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
}
