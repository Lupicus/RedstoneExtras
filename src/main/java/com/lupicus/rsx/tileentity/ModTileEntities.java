package com.lupicus.rsx.tileentity;

import java.util.Set;

import com.lupicus.rsx.block.ModBlocks;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.IForgeRegistry;

public class ModTileEntities
{
	public static final BlockEntityType<DaytimeSensorTileEntity> DAYTIME_SENSOR = new BlockEntityType<>(DaytimeSensorTileEntity::new, Set.of(ModBlocks.DAYTIME_SENSOR));
	public static final BlockEntityType<RedstoneEnergyTileEntity> REDSTONE_ENERGY_BLOCK = new BlockEntityType<>(RedstoneEnergyTileEntity::new, Set.of(ModBlocks.REDSTONE_ENERGY_BLOCK));

	public static void register(IForgeRegistry<BlockEntityType<?>> forgeRegistry)
	{
		forgeRegistry.register("daytime_sensor", DAYTIME_SENSOR);
		forgeRegistry.register("redstone_energy_block", REDSTONE_ENERGY_BLOCK);
	}
}
