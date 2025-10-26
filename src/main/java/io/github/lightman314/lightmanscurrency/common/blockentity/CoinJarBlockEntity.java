package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.items.CoinJarItem;
import net.minecraft.core.HolderLookup;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class CoinJarBlockEntity extends EasyBlockEntity
{
	
	public static final int COIN_LIMIT = 64;

	private int color = -1;
	public int getColor() { return this.color >= 0 ? this.color : 0xFFFFFF; }

	List<ItemStack> storage = new ArrayList<>();
	public List<ItemStack> getStorage() { return this.storage; }
	public void clearStorage() { this.storage.clear(); }
	
	private final ItemViewer viewer = new ItemViewer(this);
	public IItemHandler getViewer() { return this.viewer; }
	
	public CoinJarBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.COIN_JAR.get(), pos, state);
	}
	
	public boolean addCoin(ItemStack coin)
	{
		if(getCurrentCount() >= COIN_LIMIT)
			return false;
		if(!CoinAPI.getApi().IsAllowedInCoinContainer(coin, false))
			return false;
		
		boolean foundStack = false;
		for(int i = 0; i < storage.size() && !foundStack; i++)
		{
			if(InventoryUtil.ItemMatches(coin, this.storage.get(i)))
			{
				if(this.storage.get(i).getCount() < this.storage.get(i).getMaxStackSize())
				{
					this.storage.get(i).grow(1);
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
		
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeStorage(new CompoundTag(),this.level.registryAccess()));
		}
		return true;
	}
	
	protected int getCurrentCount()
	{
		int count = 0;
		for (ItemStack stack : storage)
			count += stack.getCount();
		return count;
	}
	
	@Override
	public void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		this.writeStorage(compound, lookup);

		if(this.color >= 0)
			compound.putInt("Color", this.color);
		
		super.saveAdditional(compound, lookup);
	}
	
	protected CompoundTag writeStorage(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		ListTag storageList = new ListTag();
		for (ItemStack stack : this.storage)
			storageList.add(InventoryUtil.saveItemNoLimits(stack,lookup));
		compound.put("Coins", storageList);
		
		return compound;
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {

		if(compound.contains("Coins"))
		{
			storage = new ArrayList<>();
			ListTag storageList = compound.getList("Coins", Tag.TAG_COMPOUND);
			for(int i = 0; i < storageList.size(); i++)
			{
				ItemStack result = InventoryUtil.loadItemNoLimits(storageList.getCompound(i),lookup);
				if(!result.isEmpty())
					storage.add(result);
			}
		}

		if(compound.contains("Color"))
			this.color = compound.getInt("Color");

		super.loadAdditional(compound, lookup);
	}
	
	//For reading/writing the storage when silk touched.
	public void addFullData(ItemStack item)
	{
		if(!this.storage.isEmpty())
			CoinJarItem.setJarContents(item, this.storage);
		this.addSimpleData(item);
	}

	//For writing the color when picked
	public void addSimpleData(ItemStack item)
	{
		if(this.color >= 0)
			CoinJarItem.setJarColor(item,this.color);
	}
	
	public void readItemData(ItemStack item)
	{
		this.storage = InventoryUtil.copyList(CoinJarItem.getJarContents(item));
		if(item.getItem() instanceof CoinJarItem jar && jar.canDye(item))
			this.color = CoinJarItem.getJarColor(item);
	}

	private record ItemViewer(CoinJarBlockEntity be) implements IItemHandler {

		@Override
		public int getSlots() { return this.be.storage.size(); }

		@Override
		@Nonnull
		public ItemStack getStackInSlot(int slot) {
			if (slot >= 0 && slot < this.be.storage.size())
				return this.be.storage.get(slot).copy();
			return ItemStack.EMPTY;
		}

		@Override
		@Nonnull
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			return stack.copy();
		}

		@Override
		@Nonnull
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) { return 64; }

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return false; }
	}
	
}
