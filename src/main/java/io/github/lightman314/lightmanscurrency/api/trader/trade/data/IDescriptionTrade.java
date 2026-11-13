package io.github.lightman314.lightmanscurrency.api.trader.trade.data;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IDescriptionTrade {

    TextEntry GUI_DESCRIPTION = TextEntry.gui(LCApi.MODID,"trader.trade.description");
    TextEntry GUI_TOOLTIP = TextEntry.gui(LCApi.MODID,"trader.trade.tooltip");

    DescriptionData getDescriptionData();

    default String getDescription() { return this.getDescriptionData().description; }
    default String getTooltip() { return this.getDescriptionData().tooltip; }
    default List<Component> getDescriptionTooltip() { return this.getDescriptionData().getTooltipLines(); }

}
