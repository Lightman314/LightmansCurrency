package io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EquipmentRestriction extends ItemTradeRestriction {

	private final EquipmentSlot equipmentType;

	public static final EquipmentRestriction HEAD = new EquipmentRestriction(EquipmentSlot.HEAD);
	public static final EquipmentRestriction CHEST = new EquipmentRestriction(EquipmentSlot.CHEST);
	public static final EquipmentRestriction LEGS = new EquipmentRestriction(EquipmentSlot.LEGS);
	public static final EquipmentRestriction FEET = new EquipmentRestriction(EquipmentSlot.FEET);

	protected EquipmentRestriction(EquipmentSlot type) { this.equipmentType = type; }
	
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
		try { return item.canEquip(this.equipmentType, safeGetDummyArmorStand()) || this.vanillaEquippable(item); }
		catch(Throwable e) { return this.vanillaEquippable(item); }
	}

	private boolean vanillaEquippable(ItemStack item) {
		try {
			Equipable equipable = Equipable.get(item);
			if(equipable != null)
				return equipable.getEquipmentSlot() == this.equipmentType;
			return false;
		} catch(Throwable t) { LightmansCurrency.LogError("Error attempting to get equipment slot for " + BuiltInRegistries.ITEM.getKey(item.getItem()) + "!",t); return false; }
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public Pair<ResourceLocation,ResourceLocation> getEmptySlotBG()
	{
		return switch (this.equipmentType) {
			case HEAD -> Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
			case CHEST -> Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
			case LEGS -> Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS);
			case FEET -> Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS);
			case OFFHAND -> Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			default -> null;
		};
	}

	private static ArmorStand safeGetDummyArmorStand() throws Exception {
		return new ArmorStand(LightmansCurrency.getProxy().safeGetDummyLevel(), 0d, 0d, 0d);
	}
	
}
