package io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EquipmentRestriction extends ItemTradeRestriction {

	private final EquipmentSlotType equipmentType;

	public static final EquipmentRestriction HEAD = new EquipmentRestriction(EquipmentSlotType.HEAD);
	public static final EquipmentRestriction CHEST = new EquipmentRestriction(EquipmentSlotType.CHEST);
	public static final EquipmentRestriction LEGS = new EquipmentRestriction(EquipmentSlotType.LEGS);
	public static final EquipmentRestriction FEET = new EquipmentRestriction(EquipmentSlotType.FEET);

	protected EquipmentRestriction(EquipmentSlotType type) { this.equipmentType = type; }
	
	public EquipmentSlotType getEquipmentSlot() { return this.equipmentType; }
	
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
		catch(Exception e) { return this.vanillaEquippable(item); }
	}

	private boolean vanillaEquippable(ItemStack item) {
		try {
			return MobEntity.getEquipmentSlotForItem(item) == this.equipmentType;
		} catch(Throwable t) { t.printStackTrace(); return false; }
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public Pair<ResourceLocation,ResourceLocation> getEmptySlotBG()
	{
		switch (this.equipmentType) {
			case HEAD: return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET);
			case CHEST: return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE);
			case LEGS: return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS);
			case FEET: return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS);
			case OFFHAND: return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
			default: return null;
		}
	}

	private static ArmorStandEntity safeGetDummyArmorStand() throws Exception {
		return new ArmorStandEntity(LightmansCurrency.PROXY.safeGetDummyLevel(), 0d, 0d, 0d);
	}
	
}