package com.lupicus.rsx.tileentity;

import com.lupicus.rsx.block.RedstoneEnergyBlock;
import com.lupicus.rsx.config.MyConfig;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class RedstoneEnergyTileEntity extends TileEntity implements IEnergyStorage, ITickableTileEntity, ICapabilityProvider
{
	private int count = -1;
	private int energy = -1;
	private IEnergyStorage[] sides = new IEnergyStorage[6];

	public RedstoneEnergyTileEntity() {
		super(ModTileEntities.REDSTONE_ENERGY_BLOCK);
	}

	@Override
	public void tick()
	{
		if (!world.isRemote && count != 0)
		{
			if (count < 0)
				updateSides();
			if (count > 0)
				giveToNeighbors();
		}
	}

	public void neighborChanged()
	{
		count = -1;
		energy = -1;
	}

	private void giveToNeighbors()
	{
		int[] want = new int[6];
		int total = 0;
		int num = 0;
		int max = getEnergyStored();
		for (int i = 0; i < 6; ++i)
		{
			if (sides[i] != null)
			{
				want[i] = sides[i].receiveEnergy(max, true);
				if (want[i] > 0)
					++num;
			}
			else
				want[i] = 0;
			total += want[i];
		}
		if (num == 0)
			return;
		if (num == 1)
		{
			for (int i = 0; i < 6; ++i)
			{
				if (want[i] > 0)
				{
					sides[i].receiveEnergy(max, false);
					break;
				}
			}
		}
		else if (total <= max)
		{
			for (int i = 0; i < 6; ++i)
			{
				if (want[i] > 0)
				{
					sides[i].receiveEnergy(want[i], false);
				}
			}
		}
		else
		{
			total = max;
			int avg = total / num;
			int[] send = new int[6];
			for (int i = 0; i < 6; ++i)
			{
				send[i] = (want[i] <= avg) ? want[i] : avg;
				total -= send[i];
			}
			if (total > 0)
			{
				for (int i = 0; i < 6; ++i)
				{
					int more = want[i] - send[i];
					if (more > 0)
					{
						if (more >= total)
						{
							send[i] += total;
							break;
						}
						else
						{
							send[i] += more;
							total -= more;
						}
					}
				}
			}
			for (int i = 0; i < 6; ++i)
			{
				if (send[i] > 0)
				{
					sides[i].receiveEnergy(send[i], false);
				}
			}
		}
	}

	private void updateSides()
	{
		count = 0;
		BlockPos pos = getPos();
		for (Direction dir : Direction.values())
		{
			IEnergyStorage storage = null;
			BlockPos otherPos = pos.offset(dir);
			BlockState other = world.getBlockState(otherPos);
			if (other.hasTileEntity())
			{
				TileEntity te = world.getTileEntity(otherPos);
				if (te instanceof IEnergyStorage)
				{
					storage = (IEnergyStorage) te;
				}
				else
				{
					LazyOptional<IEnergyStorage> opt = te.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite());
					if (opt.isPresent())
					{
						storage = opt.orElse(null);
					}
				}
			}
			sides[dir.getIndex()] = storage;
			if (storage != null)
				++count;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityEnergy.ENERGY)
			return LazyOptional.of(() -> (T) this);
		return super.getCapability(cap, side);
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		return 0;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		if (maxExtract > 0)
		{
			int amount = getEnergyStored();
			if (maxExtract < amount)
				amount = maxExtract;
			return amount;
		}
		return 0;
	}

	@Override
	public int getEnergyStored() {
		if (energy < 0)
		{
			BlockState blockstate = getBlockState();
			energy = (int) (blockstate.get(RedstoneEnergyBlock.POWER) * MyConfig.energyFactor);
		}
		return energy;
	}

	@Override
	public int getMaxEnergyStored() {
		return getEnergyStored();
	}

	@Override
	public boolean canExtract() {
		return true;
	}

	@Override
	public boolean canReceive() {
		return false;
	}
}
