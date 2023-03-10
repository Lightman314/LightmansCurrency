package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.EquipmentRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTraderDataArmor extends ItemTraderData {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trader_armor");
	
	public ItemTraderDataArmor() { super(TYPE); }
	public ItemTraderDataArmor(World level, BlockPos pos) { super(TYPE, 4, level, pos); }

	@Override
	protected ItemTradeRestriction getTradeRestriction(int tradeIndex)
	{
		switch (tradeIndex % 4) {
			case 0: return EquipmentRestriction.HEAD;
			case 1: return EquipmentRestriction.CHEST;
			case 2: return EquipmentRestriction.LEGS;
			default: return EquipmentRestriction.FEET;
		}
	}
	
}