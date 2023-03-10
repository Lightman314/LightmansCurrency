package io.github.lightman314.lightmanscurrency.common.traders.auction;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;

public class AuctionPlayerStorage {

	PlayerReference owner;
	public PlayerReference getOwner() { return this.owner; }
	
	List<AuctionTradeData> expiredTrades = new ArrayList<>();
	
	CoinValue storedCoins = new CoinValue();
	public CoinValue getStoredCoins() { return this.storedCoins; }
	List<ItemStack> storedItems = new ArrayList<>();
	public List<ItemStack> getStoredItems() { return this.storedItems; }
	
	public AuctionPlayerStorage(PlayerReference player) { this.owner = player; }
	
	public AuctionPlayerStorage(CompoundNBT compound) { this.load(compound); }
	
	public CompoundNBT save(CompoundNBT compound) {
		
		compound.put("Owner", this.owner.save());
		
		this.storedCoins.save(compound, "StoredMoney");
		ListNBT itemList = new ListNBT();
		for (ItemStack storedItem : this.storedItems)
			itemList.add(storedItem.save(new CompoundNBT()));
		compound.put("StoredItems", itemList);
		
		return compound;
	}
	
	protected void load(CompoundNBT compound) {
		
		this.owner = PlayerReference.load(compound.getCompound("Owner"));
		
		this.storedCoins.load(compound, "StoredMoney");
		
		this.storedItems.clear();
		ListNBT itemList = compound.getList("StoredItems", Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < itemList.size(); ++i)
		{
			ItemStack stack = ItemStack.of(itemList.getCompound(i));
			if(!stack.isEmpty())
				this.storedItems.add(stack);
		}
		
	}
	
	public void giveMoney(CoinValue amount) {
		this.storedCoins.addValue(amount);
	}
	
	/**
	 * Removes the given amount of money from the stored money.
	 * Returns the money amount that was unable to be removed.
	 */
	public CoinValue takeMoney(CoinValue amount) {
		long newValue = this.storedCoins.getRawValue() - amount.getRawValue();
		if(newValue < 0)
		{
			this.storedCoins = new CoinValue();
			return new CoinValue(-newValue);
		}
		else
		{
			this.storedCoins.loadFromOldValue(newValue);
			return new CoinValue();
		}	
	}
	
	public void collectedMoney(PlayerEntity player) {
		MoneyUtil.ProcessChange(null, player, this.storedCoins.copy());
		this.storedCoins = new CoinValue();
	}
	
	public void giveItem(ItemStack item) {
		if(!item.isEmpty())
		{
			this.storedItems.add(item);
		}
	}
	
	public void removePartial(int itemSlot, int count) {
		if(this.storedItems.size() >= itemSlot || itemSlot < 0)
			return;
		this.storedItems.get(itemSlot).shrink(count);
		if(this.storedItems.get(itemSlot).isEmpty())
			this.storedItems.remove(itemSlot);
	}
	
	public void collectItems(PlayerEntity player) {
		for(ItemStack stack : this.storedItems) ItemHandlerHelper.giveItemToPlayer(player, stack);
		this.storedItems = new ArrayList<>();
	}
	
}