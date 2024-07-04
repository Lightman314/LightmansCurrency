package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.common.notifications.types.ejection.OwnableBlockEjectedNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EjectionData implements Container, IClientTracker {

	private final OwnerData owner = new OwnerData(this, o -> {});
	MutableComponent traderName = Component.empty();
	public MutableComponent getTraderName() { return this.traderName; }
	List<ItemStack> items = new ArrayList<>();
	
	private boolean isClient = false;
	public void flagAsClient() { this.isClient = true; }
	public boolean isClient() { return this.isClient; }
	
	private EjectionData() {}
	
	private EjectionData(OwnerData owner, MutableComponent traderName, List<ItemStack> items) {
		this.owner.copyFrom(owner);
		this.traderName = traderName;
		this.items = items;
	}
	
	public boolean canAccess(Player player) {
		if(LCAdminMode.isAdminPlayer(player))
			return true;
		if(this.owner == null)
			return false;
		return this.owner.isMember(player);
	}
	
	public CompoundTag save(@Nonnull HolderLookup.Provider lookup) {
		
		CompoundTag compound = new CompoundTag();
		
		compound.put("Owner", this.owner.save(lookup));
		
		compound.putString("Name", Component.Serializer.toJson(this.traderName,lookup));

		InventoryUtil.saveAllItems("Items",compound,InventoryUtil.buildInventory(this.items),lookup);
		
		return compound;
	}
	
	public void load(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		if(compound.contains("Owner"))
			this.owner.load(compound.getCompound("Owner"),lookup);
		if(compound.contains("Name"))
			this.traderName = Component.Serializer.fromJson(compound.getString("Name"),lookup);
		if(compound.contains("Items"))
			this.items = InventoryUtil.buildList(InventoryUtil.loadAllItems("Items",compound,compound.getList("Items",Tag.TAG_COMPOUND).size(),lookup));
		
	}

	public final void pushNotificationToOwner() {
		Owner owner = this.owner.getValidOwner();
		if(owner != null)
			owner.pushNotification(OwnableBlockEjectedNotification.create(this.traderName), 1, true);
	}
	
	public static EjectionData create(Level level, BlockPos pos, BlockState state, IDumpable trader) {
		return create(level, pos, state, trader, true);
	}
	
	public static EjectionData create(@Nonnull Level level, @Nonnull BlockPos pos, @Nullable BlockState state, @Nonnull IDumpable trader, boolean dropBlock) {
		
		OwnerData owner = trader.getOwner();
		
		MutableComponent traderName = trader.getName();
		
		List<ItemStack> items = trader.getContents(level, pos, state, dropBlock);
		
		return new EjectionData(owner, traderName, items);
		
	}
	
	public static EjectionData loadData(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		EjectionData data = new EjectionData();
		data.load(compound,lookup);
		return data;
	}

	@Override
	public void clearContent() { this.items.clear(); }

	@Override
	public int getContainerSize() { return this.items.size(); }

	@Override
	public boolean isEmpty() {
		for(ItemStack stack : this.items)
		{
			if(!stack.isEmpty())
				return false;
		}
		return true;
	}

	@Nonnull
	@Override
	public ItemStack getItem(int slot) {
		if(slot >= this.items.size() || slot < 0)
			return ItemStack.EMPTY;
		return this.items.get(slot);
	}

	@Nonnull
	@Override
	public ItemStack removeItem(int slot, int count) {
		if(slot >= this.items.size() || slot < 0)
			return ItemStack.EMPTY;
		return this.items.get(slot).split(count);
	}

	@Nonnull
	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if(slot >= this.items.size() || slot < 0)
			return ItemStack.EMPTY;
		ItemStack stack = this.items.get(slot);
		this.items.set(slot, ItemStack.EMPTY);
		return stack;
	}

	@Override
	public void setItem(int slot, @Nonnull ItemStack item) {
		if(slot >= this.items.size() || slot < 0)
			return;
		this.items.set(slot, item);
	}
	
	private void clearEmptySlots() { this.items.removeIf(ItemStack::isEmpty); }

	@Override
	public void setChanged() {
		if(this.isClient)
			return;
		this.clearEmptySlots();
		if(this.isEmpty())
			EjectionSaveData.RemoveEjectionData(this);
		else
			EjectionSaveData.MarkEjectionDataDirty();
	}

	@Override
	public boolean stillValid(@Nonnull Player player) {
		return this.canAccess(player);
	}
	
}
