package io.github.lightman314.lightmanscurrency.containers;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.containers.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerInventoryWalletContainer extends Container{

	//PlayerContainer variables
	public final PlayerEntity player;
	private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET};
	private static final EquipmentSlotType[] VALID_EQUIPMENT_SLOTS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};
	
	//My variables
	public static final int WALLET_SLOT_X = 152;
	public static final int WALLET_SLOT_Y = 62;
	
	IInventory walletInventory = null;
	WalletSlot walletSlot = null;
	
	public PlayerInventoryWalletContainer(int windowID, PlayerInventory inventory) {
		super(ModContainers.INVENTORY_WALLET, windowID);
		this.player = inventory.player;
		
		//Equipment slots
		for(int k = 0; k < 4; ++k) {
			final EquipmentSlotType equipmentslottype = VALID_EQUIPMENT_SLOTS[k];
			this.addSlot(new Slot(inventory, 39 - k, 8, 8 + k * 18) {
	            public int getSlotStackLimit() {
	               return 1;
	            }
	            public boolean isItemValid(ItemStack stack) {
	               return stack.canEquip(equipmentslottype, player);
	            }
	            public boolean canTakeStack(PlayerEntity playerIn) {
	               ItemStack itemstack = this.getStack();
	               return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
	            }
	            @OnlyIn(Dist.CLIENT)
	            public Pair<ResourceLocation, ResourceLocation> getBackground() {
	            	return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, ARMOR_SLOT_TEXTURES[equipmentslottype.getIndex()]);
	            }
			});
		}
		
		//Inventory slots
		for(int l = 0; l < 3; ++l) {
			for(int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(inventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
			}
		}
		
		//Hotbar slots
		for(int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(inventory, i1, 8 + i1 * 18, 142));
		}
		
		//Shield slot
		this.addSlot(new Slot(inventory, 40, 77, 62) {
			@OnlyIn(Dist.CLIENT)
			public Pair<ResourceLocation, ResourceLocation> getBackground() {
				return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
			}
		});
		
		//Wallet slot
		WalletCapability.getWalletHandler(this.player).ifPresent(walletHandler ->{
			this.walletInventory = walletHandler.getInventory();
		});
		if(this.walletInventory == null)
			this.walletInventory = new Inventory(1);
		this.walletSlot = new WalletSlot(this.walletInventory, 0, WALLET_SLOT_X, WALLET_SLOT_Y);
		this.addSlot(this.walletSlot);
		
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}
	
	/**
	 * Called when the container is closed.
	 */
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
	}
	
	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			EquipmentSlotType equipmentslottype = MobEntity.getSlotForItemStack(itemstack);
			if(slot == this.walletSlot) { //Wallet slot to inventory
				if(!this.mergeItemStack(itemstack1, 0, this.inventorySlots.size() - 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if(WalletSlot.isValidWallet(itemstack1)) { //Inventory to wallet slot
				if(!this.mergeItemStack(itemstack1, 41, 42, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 0 && index < 4) { //From armor slot to inventory
				if (!this.mergeItemStack(itemstack1, 4, 40, false)) {
					return ItemStack.EMPTY;
				}
			} else if (equipmentslottype.getSlotType() == EquipmentSlotType.Group.ARMOR && !this.inventorySlots.get(3 - equipmentslottype.getIndex()).getHasStack()) { //To armor slot
				int i = 3 - equipmentslottype.getIndex();
				if (!this.mergeItemStack(itemstack1, i, i + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (equipmentslottype == EquipmentSlotType.OFFHAND && !this.inventorySlots.get(40).getHasStack()) { //To offhand
				if (!this.mergeItemStack(itemstack1, 40, 41, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 4 && index < 31) { //Main inventory to hotbar
				if (!this.mergeItemStack(itemstack1, 31, 40, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 31 && index < 40) { //Hotbar to main inventory
				if (!this.mergeItemStack(itemstack1, 4, 31, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 4, 40, false)) { //Offhand to inventory
				return ItemStack.EMPTY;
			} 

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);
			if (index == 0) {
				playerIn.dropItem(itemstack2, false);
			}
		}
		
		return itemstack;
	}
	

}
