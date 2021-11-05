package io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions;

import com.mojang.datafixers.util.Pair;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EquipmentRestriction extends ItemTradeRestriction {

	private final EquipmentSlotType equipmentType;
	
	public EquipmentRestriction(EquipmentSlotType type)
	{
		this(type, "");
	}
	
	public EquipmentRestriction(EquipmentSlotType type, String classicType)
	{
		super(classicType);
		this.equipmentType = type;
	}
	
	public EquipmentSlotType getEquipmentSlot() { return this.equipmentType; }
	
	@Override
	public boolean allowSellItem(ItemStack itemStack)
	{
		return itemStack.canEquip(this.equipmentType, null);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public Pair<ResourceLocation,ResourceLocation> getEmptySlotBG()
	{
		switch(this.equipmentType)
		{
		case HEAD:
			return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET);
		case CHEST:
			return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE);
		case LEGS:
			return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS);
		case FEET:
			return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS);
			default:
				return null;
		}
	}
	
}
