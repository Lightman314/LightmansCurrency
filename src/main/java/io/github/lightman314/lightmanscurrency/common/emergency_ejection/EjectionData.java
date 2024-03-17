package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.common.notifications.types.ejection.OwnableBlockEjectedNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

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
	
	public CompoundTag save() {
		
		CompoundTag compound = new CompoundTag();
		
		compound.put("Owner", this.owner.save());
		
		compound.putString("Name", Component.Serializer.toJson(this.traderName));
		
		ListTag itemList = new ListTag();
		for (ItemStack item : this.items)
			itemList.add(item.save(new CompoundTag()));
		compound.put("Items", itemList);
		
		return compound;
	}
	
	public void load(CompoundTag compound) {
		
		if(compound.contains("Owner"))
			this.owner.load(compound.getCompound("Owner"));
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

	public final void pushNotificationToOwner() {
		Team team = this.owner.getTeam();
		if(team != null)
		{
			//Push to admins and owner only
			for(PlayerReference admin : team.getAdminsAndOwner())
				NotificationAPI.PushPlayerNotification(admin.id, OwnableBlockEjectedNotification.create(this.traderName));
		}
		else
		{
			PlayerReference player = this.owner.getPlayer();
			if(player != null)
				NotificationAPI.PushPlayerNotification(player.id, OwnableBlockEjectedNotification.create(this.traderName));
		}
	}
	
	public static EjectionData create(Level level, BlockPos pos, BlockState state, IDumpable trader) {
		return create(level, pos, state, trader, true);
	}
	
	public static EjectionData create(Level level, BlockPos pos, BlockState state, IDumpable trader, boolean dropBlock) {
		
		OwnerData owner = trader.getOwner();
		
		MutableComponent traderName = trader.getName();
		
		List<ItemStack> items = trader.getContents(level, pos, state, dropBlock);
		
		return new EjectionData(owner, traderName, items);
		
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
