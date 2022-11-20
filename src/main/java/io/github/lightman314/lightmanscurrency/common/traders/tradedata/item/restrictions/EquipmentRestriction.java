package io.github.lightman314.lightmanscurrency.common.traders.tradedata.item.restrictions;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;

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
		return switch (this.equipmentType) {
			case HEAD -> Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
			case CHEST -> Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
			case LEGS -> Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS);
			case FEET -> Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS);
			case OFFHAND -> Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			default -> null;
		};
	}

	private static ArmorStand safeGetDummyArmorStand() {
		return new ArmorStand(safeGetDummyLevel(), 0d, 0d, 0d);
	}

	private static Level safeGetDummyLevel() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			return server.overworld();
		else
		{
			try{
				return Minecraft.getInstance().level;
			} catch (Throwable ignored) {}
		}
		LightmansCurrency.LogWarning("Cannot safely get a Level from which to make a dummy Armor Stand. Will resort to vanilla equippable methods.");
		throw new RuntimeException("Cannot safely get a Level from which to make a dummy Armor Stand.");
	}
	
}