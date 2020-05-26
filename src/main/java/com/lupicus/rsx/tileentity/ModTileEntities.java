package com.lupicus.rsx.tileentity;

import com.lupicus.rsx.block.ModBlocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.IForgeRegistry;

public class ModTileEntities
{
	public static final TileEntityType<DaytimeSensorTileEntity> DAYTIME_SENSOR = create("daytime_sensor", TileEntityType.Builder.create(DaytimeSensorTileEntity::new, ModBlocks.DAYTIME_SENSOR).build(null));
	public static final TileEntityType<RedstoneEnergyTileEntity> REDSTONE_ENERGY_BLOCK = create("redstone_energy_block", TileEntityType.Builder.create(RedstoneEnergyTileEntity::new, ModBlocks.REDSTONE_ENERGY_BLOCK).build(null));

	public static <T extends TileEntity> TileEntityType<T> create(String key, TileEntityType<T> type)
	{
		type.setRegistryName(key);
		return type;
	}

	public static void register(IForgeRegistry<TileEntityType<?>> forgeRegistry)
	{
		forgeRegistry.register(DAYTIME_SENSOR);
		forgeRegistry.register(REDSTONE_ENERGY_BLOCK);
	}
}
