package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.trader.IDumpable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class EjectionData implements Container {

	boolean playerOwner = false;
	UUID ownerID = null;
	MutableComponent traderName = Component.empty();
	public MutableComponent getTraderName() { return this.traderName; }
	List<ItemStack> items = new ArrayList<>();
	
	private boolean isClient = false;
	public void flagAsClient() { this.isClient = true; }
	
	private EjectionData() {}
	
	private EjectionData(boolean isPlayer, UUID ownerID, MutableComponent traderName, List<ItemStack> items) {
		this.playerOwner = isPlayer;
		this.ownerID = ownerID;
		this.traderName = traderName;
		this.items = items;
	}
	
	public boolean canAccess(Player player) {
		if(TradingOffice.isAdminPlayer(player))
			return true;
		if(this.ownerID == null)
			return false;
		if(this.playerOwner)
			return player.getUUID().equals(this.ownerID);
		else
		{
			Team team = player.level.isClientSide ? ClientTradingOffice.getTeam(this.ownerID) : TradingOffice.getTeam(this.ownerID);
			if(team != null)
				return team.isMember(player);
		}
		return false;
	}
	
	public CompoundTag save(CompoundTag compound) {
		if(this.ownerID != null)
		{
			compound.putBoolean("PlayerOwned", this.playerOwner);
			compound.putUUID("Owner", this.ownerID);
		}
		
		compound.putString("Name", Component.Serializer.toJson(this.traderName));
		
		ListTag itemList = new ListTag();
		for(int i = 0; i < this.items.size(); ++i)
		{
			itemList.add(this.items.get(i).save(new CompoundTag()));
		}
		compound.put("Items", itemList);
		
		return compound;
	}
	
	public void load(CompoundTag compound) {
		
		if(compound.contains("PlayerOwned"))
			this.playerOwner = compound.getBoolean("PlayerOwned");
		if(compound.contains("Owner"))
			this.ownerID = compound.getUUID("Owner");
		if(compound.contains("Name"))
			this.traderName = Component.Serializer.fromJson(compound.getString("Name"));
		if(compound.contains("Items"))
		{
			ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
			this.items = new ArrayList<>();
			for(int i = 0; i < itemList.size(); ++i)
			{
				this.items.add(ItemStack.of(itemList.getCompound(i)));
			}
		}
		
	}
	
	public static EjectionData create(BlockState state, IDumpable trader) {
		
		boolean playerOwner = true;
		UUID ownerID = null;
		Team team = trader.getTeam();
		if(team != null)
		{
			playerOwner = false;
			ownerID = team.getID();
		}
		else if(trader.getOwner() != null)
			ownerID = trader.getOwner().id;
		
		MutableComponent traderName = trader.getName();
		
		List<ItemStack> items = trader.dumpContents(state, true);
		
		return new EjectionData(playerOwner, ownerID, traderName, items);
		
	}
	
	public static EjectionData loadData(CompoundTag compound) {
		EjectionData data = new EjectionData();
		data.load(compound);
		return data;
	}

	@Override
	public void clearContent() { this.items.clear(); }

	@Override
	public int getContainerSize() { return this.items.size(); }

	@Override
	public boolean isEmpty() {
		for(ItemStack i : this.items)
		{
			if(!i.isEmpty())
				return false;
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		if(slot >= this.items.size() || slot < 0)
			return ItemStack.EMPTY;
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int count) {
		if(slot >= this.items.size() || slot < 0)
			return ItemStack.EMPTY;
		return this.items.get(slot).split(count);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if(slot >= this.items.size() || slot < 0)
			return ItemStack.EMPTY;
		ItemStack stack = this.items.get(slot);
		this.items.set(slot, ItemStack.EMPTY);
		return stack;
	}

	@Override
	public void setItem(int slot, ItemStack item) {
		if(slot >= this.items.size() || slot < 0)
			return;
		this.items.set(slot, item);
	}
	
	private void clearEmptySlots() {
		for(int i = 0; i < this.items.size(); ++i)
		{
			if(this.items.get(i).isEmpty())
			{
				this.items.remove(i);
				--i;
			}
		}
	}

	@Override
	public void setChanged() {
		if(this.isClient)
			return;
		this.clearEmptySlots();
		if(this.isEmpty())
			TradingOffice.removeEjectionData(this);
		else
			TradingOffice.MarkEjectionDataDirty();
	}

	@Override
	public boolean stillValid(Player player) {
		return this.canAccess(player);
	}
	
}
