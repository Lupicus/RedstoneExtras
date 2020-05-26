package com.lupicus.rsx.tileentity;

import com.lupicus.rsx.block.DaytimeSensorBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class DaytimeSensorTileEntity extends TileEntity implements ITickableTileEntity
{
	public DaytimeSensorTileEntity() {
		super(ModTileEntities.DAYTIME_SENSOR);
	}

	@Override
	public void tick() {
		if (this.world != null && !this.world.isRemote && this.world.getGameTime() % 20L == 0L) {
			BlockState blockstate = this.getBlockState();
			Block block = blockstate.getBlock();
			if (block instanceof DaytimeSensorBlock) {
				DaytimeSensorBlock.updatePower(blockstate, this.world, this.pos);
			}
		}
	}
}
