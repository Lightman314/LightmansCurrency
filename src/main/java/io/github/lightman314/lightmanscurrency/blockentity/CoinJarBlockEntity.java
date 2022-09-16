package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CoinJarBlockEntity extends BlockEntity
{
	
	public static final int COIN_LIMIT = 64;
	
	List<ItemStack> storage = new ArrayList<>();
	public List<ItemStack> getStorage() { return storage; }
	
	public CoinJarBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.COIN_JAR.get(), pos, state);
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
			this.storage.add(newCoin);
		}
		
		if(!level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeStorage(new CompoundTag()));
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
	
	@Override
	public void saveAdditional(CompoundTag compound)
	{
		this.writeStorage(compound);
		
		super.saveAdditional(compound);
	}
	
	protected CompoundTag writeStorage(CompoundTag compound)
	{
		ListTag storageList = new ListTag();
		for(int i = 0; i < storage.size(); i++)
			storageList.add(storage.get(i).save(new CompoundTag()));
		compound.put("Coins", storageList);
		
		return compound;
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		
		if(compound.contains("Coins"))
		{
			storage = new ArrayList<>();
			ListTag storageList = compound.getList("Coins", Tag.TAG_COMPOUND);
			for(int i = 0; i < storageList.size(); i++)
			{
				CompoundTag thisItem = storageList.getCompound(i);
				storage.add(ItemStack.of(thisItem));
			}
		}
		
		super.load(compound);
		
	}

	@Override
	public void onLoad()
	{
		if(this.level.isClientSide)
		{
			BlockEntityUtil.requestUpdatePacket(this);
		}
	}
	
	@Override
	public CompoundTag getUpdateTag() { return this.saveWithFullMetadata(); }
	
	//For reading/writing the storage when silk touched.
	public void writeItemTag(ItemStack item)
	{
		CompoundTag compound = item.getOrCreateTag();
		compound.put("JarData", this.writeStorage(new CompoundTag()));
	}
	
	public void readItemTag(ItemStack item)
	{
		if(item.hasTag())
		{
			CompoundTag compound = item.getTag();
			if(compound.contains("JarData", Tag.TAG_COMPOUND))
			{
				CompoundTag jarData = compound.getCompound("JarData");
				if(jarData.contains("Coins"))
				{
					storage = new ArrayList<>();
					ListTag storageList = jarData.getList("Coins", Tag.TAG_COMPOUND);
					for(int i = 0; i < storageList.size(); i++)
					{
						CompoundTag thisItem = storageList.getCompound(i);
						storage.add(ItemStack.of(thisItem));
					}
				}
			}
		}
	}
	
}
