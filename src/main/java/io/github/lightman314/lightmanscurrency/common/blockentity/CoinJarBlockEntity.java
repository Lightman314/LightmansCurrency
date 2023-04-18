package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoinJarBlockEntity extends EasyBlockEntity
{
	
	public static final int COIN_LIMIT = 64;
	
	List<ItemStack> storage = new ArrayList<>();
	public List<ItemStack> getStorage() { return storage; }
	
	private final ItemViewer viewer = new ItemViewer(this);
	
	public CoinJarBlockEntity()
	{
		super(ModBlockEntities.COIN_JAR.get());
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
			BlockEntityUtil.sendUpdatePacket(this, this.writeStorage(new CompoundNBT()));
		}
		return true;
	}
	
	protected int getCurrentCount()
	{
		int count = 0;
		for (ItemStack itemStack : storage) count += itemStack.getCount();
		return count;
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundNBT compound)
	{
		this.writeStorage(compound);
	}
	
	protected CompoundNBT writeStorage(CompoundNBT compound)
	{
		ListNBT storageList = new ListNBT();
		for (ItemStack itemStack : storage) storageList.add(itemStack.save(new CompoundNBT()));
		compound.put("Coins", storageList);
		
		return compound;
	}
	
	@Override
	protected void loadAdditional(@Nonnull CompoundNBT compound)
	{
		
		if(compound.contains("Coins"))
		{
			storage = new ArrayList<>();
			ListNBT storageList = compound.getList("Coins", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < storageList.size(); i++)
			{
				CompoundNBT thisItem = storageList.getCompound(i);
				storage.add(ItemStack.of(thisItem));
			}
		}
		
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
						storage.add(ItemStack.of(thisItem));
					}
				}
			}
		}
	}
	
	@Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this.viewer));
		return super.getCapability(cap, side);
	}
	
	private static class ItemViewer implements IItemHandler
	{

		private final CoinJarBlockEntity be;
		ItemViewer(CoinJarBlockEntity be) { this.be = be; }
		
		@Override
		public int getSlots() { return this.be.storage.size(); }

		@Override
		public @Nonnull ItemStack getStackInSlot(int slot) {
			if(slot >= 0 && slot < this.be.storage.size())
				return this.be.storage.get(slot).copy();
			return ItemStack.EMPTY;
		}

		@Override
		public @Nonnull ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) { return stack.copy(); }

		@Override
		public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

		@Override
		public int getSlotLimit(int slot) { return 64; }

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return false; }
		
	}
	
}