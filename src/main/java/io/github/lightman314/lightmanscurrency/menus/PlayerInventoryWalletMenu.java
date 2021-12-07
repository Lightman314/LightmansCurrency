package io.github.lightman314.lightmanscurrency.menus;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.menus.slots.WalletSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerInventoryWalletMenu extends AbstractContainerMenu{

	//PlayerContainer variables
	public final Player player;
	private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};
	private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	
	//My variables
	public static final int WALLET_SLOT_X = 152;
	public static final int WALLET_SLOT_Y = 62;
	
	Container walletInventory = null;
	WalletSlot walletSlot = null;
	
	public PlayerInventoryWalletMenu(int windowID, Inventory inventory) {
		super(ModContainers.INVENTORY_WALLET, windowID);
		this.player = inventory.player;
		
		//Equipment slots
		for(int k = 0; k < 4; ++k) {
			final EquipmentSlot equipmentslottype = SLOT_IDS[k];
			this.addSlot(new Slot(inventory, 39 - k, 8, 8 + k * 18) {
	            public int getMaxStackSize() {
	               return 1;
	            }
	            public boolean mayPlace(ItemStack stack) {
	               return stack.canEquip(equipmentslottype, player);
	            }
	            public boolean mayPickup(Player playerIn) {
	               ItemStack itemstack = this.getItem();
	               return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.mayPickup(playerIn);
	            }
	            @OnlyIn(Dist.CLIENT)
	            public Pair<ResourceLocation, ResourceLocation> getBackground() {
	            	return Pair.of(InventoryMenu.BLOCK_ATLAS, TEXTURE_EMPTY_SLOTS[equipmentslottype.getIndex()]);
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
				return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			}
		});
		
		//Wallet slot
		WalletCapability.getWalletHandler(this.player).ifPresent(walletHandler ->{
			this.walletInventory = walletHandler.getInventory();
		});
		if(this.walletInventory == null)
			this.walletInventory = new SimpleContainer(1);
		this.walletSlot = new WalletSlot(this.walletInventory, 0, WALLET_SLOT_X, WALLET_SLOT_Y);
		this.addSlot(this.walletSlot);
		
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return true;
	}
	
	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			EquipmentSlot equipmentslottype = Mob.getEquipmentSlotForItem(itemstack);
			if(slot == this.walletSlot) { //Wallet slot to inventory
				if(!this.moveItemStackTo(itemstack1, 0, this.slots.size() - 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if(WalletSlot.isValidWallet(itemstack1)) { //Inventory to wallet slot
				if(!this.moveItemStackTo(itemstack1, 41, 42, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 0 && index < 4) { //From armor slot to inventory
				if (!this.moveItemStackTo(itemstack1, 4, 40, false)) {
					return ItemStack.EMPTY;
				}
			} else if (equipmentslottype.getType() == EquipmentSlot.Type.ARMOR && !this.slots.get(3 - equipmentslottype.getIndex()).hasItem()) { //To armor slot
				int i = 3 - equipmentslottype.getIndex();
				if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (equipmentslottype == EquipmentSlot.OFFHAND && !this.slots.get(40).hasItem()) { //To offhand
				if (!this.moveItemStackTo(itemstack1, 40, 41, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 4 && index < 31) { //Main inventory to hotbar
				if (!this.moveItemStackTo(itemstack1, 31, 40, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 31 && index < 40) { //Hotbar to main inventory
				if (!this.moveItemStackTo(itemstack1, 4, 31, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 4, 40, false)) { //Offhand to inventory
				return ItemStack.EMPTY;
			} 

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack1);
			
		}
		
		return itemstack;
	}
	

}
