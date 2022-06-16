package com.lupicus.rsx.tileentity;

import com.lupicus.rsx.block.ModBlocks;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.IForgeRegistry;

public class ModTileEntities
{
	public static final BlockEntityType<DaytimeSensorTileEntity> DAYTIME_SENSOR = BlockEntityType.Builder.of(DaytimeSensorTileEntity::new, ModBlocks.DAYTIME_SENSOR).build(null);
	public static final BlockEntityType<RedstoneEnergyTileEntity> REDSTONE_ENERGY_BLOCK = BlockEntityType.Builder.of(RedstoneEnergyTileEntity::new, ModBlocks.REDSTONE_ENERGY_BLOCK).build(null);

	public static void register(IForgeRegistry<BlockEntityType<?>> forgeRegistry)
	{
		forgeRegistry.register("daytime_sensor", DAYTIME_SENSOR);
		forgeRegistry.register("redstone_energy_block", REDSTONE_ENERGY_BLOCK);
	}
}
