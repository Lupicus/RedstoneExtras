package com.lupicus.rsx;

import org.jetbrains.annotations.NotNull;

import com.lupicus.rsx.block.ModBlocks;
import com.lupicus.rsx.config.MyConfig;
import com.lupicus.rsx.item.ModItems;
import com.lupicus.rsx.sound.ModSounds;
import com.lupicus.rsx.tileentity.ModTileEntities;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Main.MODID)
public class Main
{
	public static final String MODID = "rsx";

	public Main(FMLJavaModLoadingContext context)
	{
		context.registerConfig(ModConfig.Type.COMMON, MyConfig.COMMON_SPEC);
	}

	@Mod.EventBusSubscriber(bus = Bus.MOD)
	public static class ModEvents
	{
		@SubscribeEvent
		public static void onRegister(final RegisterEvent event)
		{
			@NotNull
			ResourceKey<? extends Registry<?>> key = event.getRegistryKey();
			if (key.equals(ForgeRegistries.Keys.BLOCKS))
				ModBlocks.register(event.getForgeRegistry());
			else if (key.equals(ForgeRegistries.Keys.ITEMS))
				ModItems.register(event.getForgeRegistry());
			else if (key.equals(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES))
				ModTileEntities.register(event.getForgeRegistry());
			else if (key.equals(ForgeRegistries.Keys.SOUND_EVENTS))
				ModSounds.register(event.getForgeRegistry());
		}
	}

	@Mod.EventBusSubscriber(bus = Bus.FORGE)
	public static class ForgeEvents
	{
		@SubscribeEvent
		public static void onCreativeTab(BuildCreativeModeTabContentsEvent event)
		{
			ModItems.setupTabs(event);
		}

		@SubscribeEvent
		public static void onColorsRegistry(final RegisterColorHandlersEvent.Block event)
		{
			ModBlocks.register(event);
		}
	}
}
