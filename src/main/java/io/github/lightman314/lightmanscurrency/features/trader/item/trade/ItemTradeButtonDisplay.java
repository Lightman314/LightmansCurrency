package io.github.lightman314.lightmanscurrency.features.trader.item.trade;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeTooltipBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays.ItemDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays.SpriteDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays.StackedDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.TradeButtonDisplay;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminSettingProvider;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemTradeButtonDisplay extends TradeButtonDisplay {

    public static final TradeButtonDisplay INSTANCE = new ItemTradeButtonDisplay();

    public static final Identifier FILTER_HIGHLIGHT = LCApi.id("container/slot/filter");
    public static final Identifier UNFILTERED_HIGHLIGHT = LCApi.id("container/slot/unfiltered");

    protected ItemTradeButtonDisplay() {}

    @Override
    public int getSlotWidth(TradeData trade,TradeSlotType section,TradeContext context,@Nullable ITradeInteractionHandler handler) {
        if(section.isInput() || section.isOutput())
            return 33;
        if(section.isArrow())
            return ARROW_WIDTH;
        return 0;
    }

    @Override
    public List<TradeDisplayEntry> getDisplayEntries(TradeData trade,TradeSlotType section,TradeContext context,boolean hovered,@Nullable ITradeInteractionHandler handler) {
        //Always show the arrow
        if(section.isArrow())
            return arrowWithMessages(hovered,trade,context,this);
        if(trade instanceof ItemTradeData it) {
            if(section.isPriceSlot(trade.getDirection()))
                return forPrice(trade,context,33,handler);
            else if(section.isInput() || section.isOutput())
            {
                List<TradeDisplayEntry> entries = new ArrayList<>();
                //Item Display
                for(int i = 0; i < 2; ++i)
                {
                    TradeItem item = it.getItem(i);
                    if(!item.isEmpty() || context.isEditingView())
                        entries.add(forTradeItem(item,section,getBackgroundSprite(it),context,handler));
                }
                return entries;
            }
        }
        return ImmutableList.of();
    }

    public static Identifier getBackgroundSprite(ItemTradeData trade) {
        Identifier background = trade.getBackgroundOverride();
        return background == null ? EMPTY_ITEM : background;
    }

    public static TradeDisplayEntry forTradeItem(TradeItem item,TradeSlotType section,Identifier backgroundSprite, TradeContext context, ITradeInteractionHandler handler) {
        List<TradeDisplayEntry> displays = new ArrayList<>();
        if(item.isFilter())
            displays.add(SpriteDisplay.of(FILTER_HIGHLIGHT,16,16));
        if(!item.isStrict())
            displays.add(SpriteDisplay.of(UNFILTERED_HIGHLIGHT,16,16));
        displays.add(ItemDisplay.of(item.getDisplayStack(context),backgroundSprite,modifyTooltip(item,section,context,handler)));
        return StackedDisplay.of(displays);
    }

    private static ItemDisplay.ItemTooltipModifier modifyTooltip(TradeItem item,TradeSlotType section,TradeContext context, ITradeInteractionHandler handler) {
        return (list,infoBuilder) -> {
            Component originalName = null;
            String nameChange = item.getNameChange();
            if(nameChange != null)
                originalName = list.set(0,Component.literal(nameChange).withStyle(ChatFormatting.GOLD));
            //Check if we should add the "hold shift to select" text
            if(context.isEditingView())
            {
                //Add just below the items name
                list.add(1,TradeItem.TOOLTIP_TRADE_ITEM_EDIT_SHIFT.getWithStyle(ChatFormatting.YELLOW));
            }
            if(!item.isStrict()) {
                //Add Data Enforcement Tooltip
                if(section.isInput())
                    list.add(1,TradeItem.TOOLTIP_TRADE_ITEM_DATA_WARNING_INPUT.getWithStyle(ChatFormatting.DARK_PURPLE,ChatFormatting.BOLD));
                if(section.isOutput())
                    list.add(1,TradeItem.TOOLTIP_TRADE_ITEM_DATA_WARNING_OUTPUT.getWithStyle(ChatFormatting.DARK_PURPLE,ChatFormatting.BOLD));
            }
            if(originalName != null)
                infoBuilder.accept(TradeItem.TOOLTIP_TRADE_INFO_ORIGINAL_NAME.get(originalName).withStyle(ChatFormatting.YELLOW));
        };
    }

    @Override
    public void appendTooltips(TradeTooltipBuilder builder, TradeData trade, TradeContext context, ScreenPosition mousePos, ITradeInteractionHandler handler) {
        //Add trade stock, etc.
        Component stockCount;
        if(IAdminSettingProvider.hasInfiniteStock(context.getTrader()))
            stockCount = TradeData.TOOLTIP_TRADE_INFO_STOCK_INFINITE.get();
        else
            stockCount = Component.literal(NumberHelper.prettyInteger(trade.getStock(context)));
        builder.addInfo(TradeData.TOOLTIP_TRADE_INFO_STOCK.get(stockCount).withStyle(ChatFormatting.YELLOW));
    }

}