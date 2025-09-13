package io.github.lightman314.lightmanscurrency.common.traders.auction;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class AuctionPlayerStorage {

	PlayerReference owner;
	public PlayerReference getOwner() { return this.owner; }
	
	private final MoneyStorage storedCoins = new MoneyStorage(() -> {});
	public MoneyStorage getStoredCoins() { return this.storedCoins; }
	private final List<ItemStack> storedItems = new ArrayList<>();
	public List<ItemStack> getStoredItems() { return this.storedItems; }
    public int pendingWinStats = 0;
	
	public AuctionPlayerStorage(PlayerReference player) { this.owner = player; }
	
	public AuctionPlayerStorage(CompoundTag compound,@Nonnull HolderLookup.Provider lookup) { this.load(compound,lookup); }
	
	public CompoundTag save(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		compound.put("Owner", this.owner.save());

		compound.put("StoredMoney", this.storedCoins.save());
		ListTag itemList = new ListTag();
		for (ItemStack storedItem : this.storedItems)
			itemList.add(InventoryUtil.saveItemNoLimits(storedItem,lookup));
		compound.put("StoredItems", itemList);
        if(this.pendingWinStats > 0)
            compound.putInt("PendingStats",this.pendingWinStats);
		
		return compound;
	}
	
	protected void load(CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		
		this.owner = PlayerReference.load(compound.getCompound("Owner"));

		this.storedCoins.safeLoad(compound, "StoredMoney");
		
		this.storedItems.clear();
		ListTag itemList = compound.getList("StoredItems", Tag.TAG_COMPOUND);
		for(int i = 0; i < itemList.size(); ++i)
		{
			ItemStack stack = InventoryUtil.loadItemNoLimits(itemList.getCompound(i),lookup);
			if(!stack.isEmpty())
				this.storedItems.add(stack);
		}

        if(compound.contains("PendingStats"))
            this.pendingWinStats = compound.getInt("PendingStats");
		
	}
	
	public void giveMoney(@Nonnull MoneyValue amount) { this.storedCoins.addValue(amount); }
	
	public void collectedMoney(Player player) {
		this.storedCoins.GiveToPlayer(player);
	}
	
	public void giveItem(ItemStack item) {
		if(!item.isEmpty())
			this.storedItems.add(item);
	}
	
	public void collectItems(Player player) {
		for(ItemStack stack : this.storedItems) ItemHandlerHelper.giveItemToPlayer(player, stack);
		this.storedItems.clear();
	}
	
}
