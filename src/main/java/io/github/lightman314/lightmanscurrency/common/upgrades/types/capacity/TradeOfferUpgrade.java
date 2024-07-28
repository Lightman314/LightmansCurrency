package io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class TradeOfferUpgrade extends CapacityUpgrade {

    @Override
    public boolean isUnique() { return true; }

    @Nonnull
    @Override
    public List<Component> getTooltip(@Nonnull UpgradeData data)
    {
        return Lists.newArrayList(LCText.TOOLTIP_UPGRADE_TRADE_OFFER.get(NumberUtil.GetPrettyString(data.getIntValue(CapacityUpgrade.CAPACITY))));
    }

    @Nonnull
    @Override
    protected List<Component> getBuiltInTargets() { return ImmutableList.of(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_ITEM.get()); }

    public static int getBonusTrades(@Nonnull Container upgradeContainer)
    {
        int count = 0;
        for(int i = 0; i < upgradeContainer.getContainerSize(); ++i)
        {
            ItemStack stack = upgradeContainer.getItem(i);
            if(!stack.isEmpty() && stack.getItem() instanceof UpgradeItem item)
            {
                if(item.getUpgradeType() == Upgrades.TRADE_OFFERS)
                    count += UpgradeItem.getUpgradeData(stack).getIntValue(CapacityUpgrade.CAPACITY);
            }
        }
        return count;
    }

}
