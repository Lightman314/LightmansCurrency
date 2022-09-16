package io.github.lightman314.lightmanscurrency.common.traders.tradedata.item.restrictions;

import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EquipmentRestriction extends ItemTradeRestriction {

	private final EquipmentSlot equipmentType;
	
	public EquipmentRestriction(EquipmentSlot type)
	{
		this.equipmentType = type;
	}
	
	public EquipmentSlot getEquipmentSlot() { return this.equipmentType; }
	
	@Override
	public boolean allowSellItem(ItemStack itemStack)
	{
		return this.equippable(itemStack);
	}
	
	@Override
	public boolean allowItemSelectItem(ItemStack itemStack)
	{
		return this.equippable(itemStack);
	}
	
	private boolean equippable(ItemStack item) {
		try { return item.canEquip(this.equipmentType, new ArmorStand(null, 0d, 0d, 0d)) || this.vanillaEquippable(item); }
		catch(Exception e) { return this.vanillaEquippable(item); }
	}

	private boolean vanillaEquippable(ItemStack item) {
		try {
			return Mob.getEquipmentSlotForItem(item) == this.equipmentType;
		} catch(Throwable t) { t.printStackTrace(); return false; }
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public Pair<ResourceLocation,ResourceLocation> getEmptySlotBG()
	{
		switch(this.equipmentType)
		{
		case HEAD:
			return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
		case CHEST:
			return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
		case LEGS:
			return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS);
		case FEET:
			return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS);
			default:
				return null;
		}
	}
	
}
