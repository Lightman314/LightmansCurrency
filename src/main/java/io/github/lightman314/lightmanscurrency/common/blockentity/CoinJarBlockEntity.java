package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class CoinJarBlockEntity extends BlockEntity
{
	
	public static final int COIN_LIMIT = 64;
	
	List<ItemStack> storage = new ArrayList<>();
	public List<ItemStack> getStorage() { return storage; }
	
	private final ItemViewer viewer = new ItemViewer(this);
	
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
	
	@Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if(cap == ForgeCapabilities.ITEM_HANDLER)
			return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> this.viewer));
		return super.getCapability(cap, side);
	}
	
	private static class ItemViewer implements IItemHandler
	{

		private final CoinJarBlockEntity be;
		ItemViewer(CoinJarBlockEntity be) { this.be = be; }
		
		@Override
		public int getSlots() { return this.be.storage.size(); }

		@Override
		public @NotNull ItemStack getStackInSlot(int slot) {
			if(slot >= 0 && slot < this.be.storage.size())
				return this.be.storage.get(slot).copy();
			return ItemStack.EMPTY;
		}

		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return stack.copy(); }

		@Override
		public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

		@Override
		public int getSlotLimit(int slot) { return 64; }

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
		
	}
	
}
