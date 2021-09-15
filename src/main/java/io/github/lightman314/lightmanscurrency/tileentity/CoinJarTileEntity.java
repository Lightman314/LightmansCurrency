package io.github.lightman314.lightmanscurrency.tileentity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

public class CoinJarTileEntity extends TileEntity
{
	
	public static int COIN_LIMIT = 64;
	
	List<ItemStack> storage = new ArrayList<>();
	public List<ItemStack> getStorage() { return storage; }
	
	public CoinJarTileEntity()
	{
		super(ModTileEntities.COIN_JAR);
	}
	
	public boolean addCoin(ItemStack coin)
	{
		if(getCurrentCount() >= COIN_LIMIT)
			return false;
		if(!MoneyUtil.isCoin(coin, false))
			return false;
		
		boolean foundStack = false;
		for(int i = 0; i < storage.size() && !foundStack; i++)
		{
			if(InventoryUtil.ItemMatches(coin, storage.get(i)))
			{
				if(storage.get(i).getCount() < storage.get(i).getMaxStackSize())
				{
					storage.get(i).grow(1);
					foundStack = true;
				}
			}
		}
		if(!foundStack)
		{
			ItemStack newCoin = coin.copy();
			newCoin.setCount(1);
			storage.add(newCoin);
		}
		
		if(!world.isRemote)
		{
			CompoundNBT compound = this.writeStorage(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
		}
		return true;
	}
	
	protected int getCurrentCount()
	{
		int count = 0;
		for(int i = 0; i < storage.size(); i++)
			count += storage.get(i).getCount();
		return count;
	}
	
	protected CompoundNBT writeStorage(CompoundNBT compound)
	{
		ListNBT storageList = new ListNBT();
		for(int i = 0; i < storage.size(); i++)
			storageList.add(storage.get(i).write(new CompoundNBT()));
		compound.put("Coins", storageList);
		
		return compound;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		this.writeStorage(compound);
		
		return super.write(compound);
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound)
	{
		
		if(compound.contains("Coins"))
		{
			storage = new ArrayList<>();
			ListNBT storageList = compound.getList("Coins", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < storageList.size(); i++)
			{
				CompoundNBT thisItem = storageList.getCompound(i);
				storage.add(ItemStack.read(thisItem));
			}
		}
		
		super.read(state, compound);
		
	}

	@Override
	public void onLoad()
	{
		if(this.world.isRemote)
		{
			TileEntityUtil.requestUpdatePacket(world, pos);
		}
	}
	
	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(this.pos, 0, this.write(new CompoundNBT()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		CompoundNBT compound = pkt.getNbtCompound();
		this.read(this.getBlockState(), compound);
	}
	
	//For reading/writing the storage when silk touched.
	public void writeItemTag(ItemStack item)
	{
		CompoundNBT compound = item.getOrCreateTag();
		compound.put("JarData", this.writeStorage(new CompoundNBT()));
	}
	
	public void readItemTag(ItemStack item)
	{
		if(item.hasTag())
		{
			CompoundNBT compound = item.getTag();
			if(compound.contains("JarData", Constants.NBT.TAG_COMPOUND))
			{
				CompoundNBT jarData = compound.getCompound("JarData");
				if(jarData.contains("Coins"))
				{
					storage = new ArrayList<>();
					ListNBT storageList = jarData.getList("Coins", Constants.NBT.TAG_COMPOUND);
					for(int i = 0; i < storageList.size(); i++)
					{
						CompoundNBT thisItem = storageList.getCompound(i);
						storage.add(ItemStack.read(thisItem));
					}
				}
			}
		}
	}
	
}
