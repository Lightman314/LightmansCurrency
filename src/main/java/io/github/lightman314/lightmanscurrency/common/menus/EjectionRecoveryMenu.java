package io.github.lightman314.lightmanscurrency.common.menus;

import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.SafeEjectionAPI;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.common.menus.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class EjectionRecoveryMenu extends LazyMessageMenu {

	public static final MenuProvider PROVIDER = new Provider();
	
	public EjectionRecoveryMenu(int menuID, Inventory inventory) { this(ModMenus.EJECTION_RECOVERY.get(), menuID, inventory); }

    public List<EjectionData> getValidEjectionData() {
		return SafeEjectionAPI.getApi().getDataForPlayer(this.player);
	}
	
	private int selectedIndex = 0;
	public int getSelectedIndex() { return this.selectedIndex; }
	public EjectionData getSelectedData() { 
		List<EjectionData> data = this.getValidEjectionData();
		if(!data.isEmpty() && this.selectedIndex >= 0 && this.selectedIndex < data.size())
			return data.get(this.selectedIndex);
		return null;
	}

	private final Container dummyContainer = new SimpleContainer(54);
	
	private Container getSelectedContainer() { 
		//Get valid data
		List<EjectionData> data = this.getValidEjectionData();
		//Check if selection is no longer valid
		if(this.isServer() && !data.isEmpty() && (this.selectedIndex < 0 || this.selectedIndex >= data.size()))
			this.changeSelection(this.selectedIndex);
		if(!data.isEmpty() && this.selectedIndex >= 0 && this.selectedIndex < data.size())
			return data.get(this.selectedIndex).getContents();
		return this.dummyContainer;
	}

	private void markSelectedDirty() {
		EjectionData data = this.getSelectedData();
		if(data != null)
			data.setChanged();
	}
	
	protected EjectionRecoveryMenu(MenuType<?> type, int menuID, Inventory inventory) {
		super(type, menuID, inventory);
		
		Container ejectionContainer = new SuppliedContainer(this::getSelectedContainer);
		
		//Menu slots
		for(int y = 0; y < 6; ++y)
		{
			for(int x = 0; x < 9; ++x)
			{
				OutputSlot slot = new OutputSlot(ejectionContainer, x + y * 9, 8 + x * 18, 18 + y * 18);
				slot.setListener(this::markSelectedDirty);
				 this.addSlot(slot);
			}
		}
		
		//Player's Inventory
		for(int y = 0; y < 3; ++y) {
			for(int x = 0; x < 9; ++x) {
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 140 + y * 18));
			}
		}

		//Player's hotbar
		for(int x = 0; x < 9; ++x) {
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 198));
		}
		
	}

	@Override
	public void HandleMessage(@Nonnull LazyPacketData message) {
		if(message.contains("ChangeSelection", LazyPacketData.TYPE_INT))
			this.changeSelection(message.getInt("ChangeSelection"));
		if(message.contains("SelectionChanged"))
			this.selectedIndex = message.getInt("SelectionChanged");
		if(message.contains("SplitData"))
			this.splitSelectedData();
	}

	@Nonnull
	public ItemStack quickMoveStack(@Nonnull Player player, int slotIndex) {
	      ItemStack itemstack = ItemStack.EMPTY;
	      Slot slot = this.slots.get(slotIndex);
	      if (slot != null && slot.hasItem()) {
	         ItemStack itemstack1 = slot.getItem();
	         itemstack = itemstack1.copy();
	         if (slotIndex < 54) {
	            if (!this.moveItemStackTo(itemstack1, 54, this.slots.size(), true)) {
	               return ItemStack.EMPTY;
	            }
	         } else if (!this.moveItemStackTo(itemstack1, 0, 54, false)) {
	            return ItemStack.EMPTY;
	         }

	         if (itemstack1.isEmpty()) {
	            slot.set(ItemStack.EMPTY);
	         } else {
	            slot.setChanged();
	         }
	      }

	      return itemstack;
   }
	
	@Override
	public void removed(@Nonnull Player player) {
		super.removed(player);
		//Clear the dummy container for safety.
		this.clearContainer(player, this.dummyContainer);
	}

	public void splitSelectedData()
	{
		if(this.isClient())
		{
			this.SendMessage(this.builder().setFlag("SplitData"));
			return;
		}
		EjectionData data = this.getSelectedData();
		if(data != null && data.canSplit())
		{
			try {
				data.splitContents();
				data.setChanged();
			} catch (Throwable t) { LightmansCurrency.LogError("Error splitting Ejection Contents!",t); }
		}
	}

	public void changeSelection(int newSelection) {
		this.changeSelection(newSelection, this.getValidEjectionData().size());
	}
	
	private void changeSelection(int newSelection, int dataSize) {
		if(this.isClient())
		{
			this.SendMessage(LazyPacketData.simpleInt("ChangeSelection", newSelection));
			return;
		}
		int oldSelection = this.selectedIndex;
		this.selectedIndex = MathUtil.clamp(newSelection, 0, dataSize - 1);
		if(this.selectedIndex != oldSelection && this.isServer())
		{
			//Inform the other side of the change
			this.SendMessage(LazyPacketData.simpleInt("SelectionChanged", this.selectedIndex));
		}
	}
	
	private static class Provider implements MenuProvider {

		@Override
		public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) { return new EjectionRecoveryMenu(id, inventory); }

		@Nonnull
		@Override
		public Component getDisplayName() { return Component.empty(); }
		
	}
	
}
