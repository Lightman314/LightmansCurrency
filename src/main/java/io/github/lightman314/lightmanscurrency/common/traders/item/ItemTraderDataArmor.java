package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.item.restrictions.EquipmentRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.item.restrictions.ItemTradeRestriction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;

public class ItemTraderDataArmor extends ItemTraderData {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trader_armor");
	
	public ItemTraderDataArmor() { super(TYPE); }
	public ItemTraderDataArmor(Level level, BlockPos pos) { super(TYPE, 4, level, pos); }

	@Override
	protected ItemTradeRestriction getTradeRestriction(int tradeIndex)
	{
		switch(tradeIndex % 4)
		{
		case 0:
			return new EquipmentRestriction(EquipmentSlot.HEAD);
		case 1:
			return new EquipmentRestriction(EquipmentSlot.CHEST);
		case 2:
			return new EquipmentRestriction(EquipmentSlot.LEGS);
		default:
			return new EquipmentRestriction(EquipmentSlot.FEET);
		}
	}
	
}