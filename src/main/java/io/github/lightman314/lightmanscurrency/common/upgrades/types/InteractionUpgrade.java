package io.github.lightman314.lightmanscurrency.common.upgrades.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class InteractionUpgrade extends UpgradeType {

    public static final String INTERACTIONS = "interactions";

    @Nonnull
    @Override
    public List<Component> getTooltip(@Nonnull UpgradeData data)
    {
        return Lists.newArrayList(LCText.TOOLTIP_UPGRADE_INTERACTION.get(NumberUtil.GetPrettyString(data.getIntValue(INTERACTIONS))));
    }

    @Nonnull
    @Override
    protected List<Component> getBuiltInTargets() { return ImmutableList.of(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_INTERFACE.get()); }

}
