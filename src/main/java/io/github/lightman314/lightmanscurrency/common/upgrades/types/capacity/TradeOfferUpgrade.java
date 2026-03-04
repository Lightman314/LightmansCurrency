package io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeStackHandler;
import io.github.lightman314.lightmanscurrency.api.upgrades.types.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TradeOfferUpgrade extends CapacityUpgrade {

    @Override
    public boolean isUnique() { return true; }

    @Override
    public List<Component> getTooltip(UpgradeData data)
    {
        return Lists.newArrayList(LCText.TOOLTIP_UPGRADE_TRADE_OFFER.get(NumberUtil.GetPrettyString(data.getIntValue(CapacityUpgrade.CAPACITY))));
    }

    @Override
    protected List<Component> getBuiltInTargets() { return ImmutableList.of(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_ITEM.get()); }

    public static int getBonusTrades(UpgradeStackHandler container)
    {
        int count = 0;
        for(var entry : container)
        {
            if(entry.getFirst() == Upgrades.TRADE_OFFERS)
                count += entry.getSecond().getIntValue(CapacityUpgrade.CAPACITY);
        }
        return count;
    }

}
