package com.lupicus.rsx.sound;

import com.lupicus.rsx.Main;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModSounds
{
	public static final SoundEvent REDSTONE_PULSE_CLICK = create("redstone_pulse_block.click");

	private static SoundEvent create(String key)
	{
		ResourceLocation res = ResourceLocation.fromNamespaceAndPath(Main.MODID, key);
		SoundEvent ret = SoundEvent.createVariableRangeEvent(res);
		return ret;
	}

	public static void register(IForgeRegistry<SoundEvent> registry)
	{
		registry.register(REDSTONE_PULSE_CLICK.getLocation(), REDSTONE_PULSE_CLICK);
	}
}
