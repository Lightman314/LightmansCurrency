package io.github.lightman314.lightmanscurrency.common.traders.auction;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class AuctionPlayerStorage {

	PlayerReference owner;
	public PlayerReference getOwner() { return this.owner; }
	
	CoinValue storedCoins = CoinValue.EMPTY;
	public CoinValue getStoredCoins() { return this.storedCoins; }
	List<ItemStack> storedItems = new ArrayList<>();
	public List<ItemStack> getStoredItems() { return this.storedItems; }
	
	public AuctionPlayerStorage(PlayerReference player) { this.owner = player; }
	
	public AuctionPlayerStorage(CompoundTag compound) { this.load(compound); }
	
	public CompoundTag save(CompoundTag compound) {
		
		compound.put("Owner", this.owner.save());

		compound.put("StoredMoney", this.storedCoins.save());
		ListTag itemList = new ListTag();
		for (ItemStack storedItem : this.storedItems)
			itemList.add(storedItem.save(new CompoundTag()));
		compound.put("StoredItems", itemList);
		
		return compound;
	}
	
	protected void load(CompoundTag compound) {
		
		this.owner = PlayerReference.load(compound.getCompound("Owner"));

		this.storedCoins = CoinValue.safeLoad(compound, "StoredMoney");
		
		this.storedItems.clear();
		ListTag itemList = compound.getList("StoredItems", Tag.TAG_COMPOUND);
		for(int i = 0; i < itemList.size(); ++i)
		{
			ItemStack stack = ItemStack.of(itemList.getCompound(i));
			if(!stack.isEmpty())
				this.storedItems.add(stack);
		}
		
	}
	
	public void giveMoney(CoinValue amount) { this.storedCoins = this.storedCoins.plusValue(amount); }
	
	public void collectedMoney(Player player) {
		MoneyUtil.ProcessChange(null, player, this.storedCoins);
		this.storedCoins = CoinValue.EMPTY;
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
	
	public void collectItems(Player player) {
		for(ItemStack stack : this.storedItems) ItemHandlerHelper.giveItemToPlayer(player, stack);
		this.storedItems = new ArrayList<>();
	}
	
}
