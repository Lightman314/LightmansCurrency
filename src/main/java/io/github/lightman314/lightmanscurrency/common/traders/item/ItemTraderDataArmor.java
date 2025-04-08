package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.EquipmentRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class ItemTraderDataArmor extends ItemTraderData {

	public static final TraderType<ItemTraderDataArmor> TYPE = new TraderType<>(VersionUtil.lcResource("item_trader_armor"),ItemTraderDataArmor::new);
	
	private ItemTraderDataArmor() { super(TYPE); }
	public ItemTraderDataArmor(Level level, BlockPos pos) { super(TYPE, 4, level, pos); }

	@Nonnull
	@Override
	protected ItemTradeRestriction getTradeRestriction(int tradeIndex)
	{
		return switch (tradeIndex % 4) {
			case 0 -> EquipmentRestriction.HEAD;
			case 1 -> EquipmentRestriction.CHEST;
			case 2 -> EquipmentRestriction.LEGS;
			default -> EquipmentRestriction.FEET;
		};
	}
	
	
}
