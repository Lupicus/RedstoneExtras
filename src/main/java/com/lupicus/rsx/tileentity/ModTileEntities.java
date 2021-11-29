package com.lupicus.rsx.tileentity;

import com.lupicus.rsx.block.ModBlocks;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.IForgeRegistry;

public class ModTileEntities
{
	public static final BlockEntityType<DaytimeSensorTileEntity> DAYTIME_SENSOR = create("daytime_sensor", BlockEntityType.Builder.of(DaytimeSensorTileEntity::new, ModBlocks.DAYTIME_SENSOR).build(null));
	public static final BlockEntityType<RedstoneEnergyTileEntity> REDSTONE_ENERGY_BLOCK = create("redstone_energy_block", BlockEntityType.Builder.of(RedstoneEnergyTileEntity::new, ModBlocks.REDSTONE_ENERGY_BLOCK).build(null));

	public static <T extends BlockEntity> BlockEntityType<T> create(String key, BlockEntityType<T> type)
	{
		type.setRegistryName(key);
		return type;
	}

	public static void register(IForgeRegistry<BlockEntityType<?>> forgeRegistry)
	{
		forgeRegistry.register(DAYTIME_SENSOR);
		forgeRegistry.register(REDSTONE_ENERGY_BLOCK);
	}
}
