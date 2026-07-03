package io.github.lightman314.lightmanscurrency.features.trader.item.trade;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.builtin.ItemDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.builtin.SpriteDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.builtin.StackedDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.trade.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.TradeDisplay;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ItemTradeDisplay extends TradeDisplay {

    public static final TradeDisplay INSTANCE = new ItemTradeDisplay();

    public static final Identifier FILTER_HIGHLIGHT = LCApi.id("container/slot/filter");
    public static final Identifier UNFILTERED_HIGHLIGHT = LCApi.id("container/slot/unfiltered");

    protected ItemTradeDisplay() {}

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
            return arrowWithMessages(hovered,trade,context);
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
                    if(!item.isEmpty() || context.getCustomer().isEditing())
                        entries.add(forTradeItem(item,EMPTY_ITEM,context,handler));
                }
                return entries;
            }
        }
        return ImmutableList.of();
    }

    public static TradeDisplayEntry forTradeItem(TradeItem item,Identifier backgroundSprite,TradeContext context,ITradeInteractionHandler handler) {
        List<TradeDisplayEntry> displays = new ArrayList<>();
        if(item.isFilter())
            displays.add(SpriteDisplay.of(FILTER_HIGHLIGHT,16,16));
        if(!item.isStrict())
            displays.add(SpriteDisplay.of(UNFILTERED_HIGHLIGHT,16,16));
        displays.add(ItemDisplay.of(item.getDisplayStack(context),backgroundSprite,modifyTooltip(item,context,handler)));
        return StackedDisplay.of(displays);
    }

    private static Consumer<List<Component>> modifyTooltip(TradeItem item,TradeContext context,ITradeInteractionHandler handler) {
        return list -> {
            AtomicReference<Component> originalName = new AtomicReference<>(null);
            item.getNameChange().ifPresent(newName -> {
                originalName.set(list.getFirst());
                list.set(0,Component.literal(newName).withStyle(ChatFormatting.GOLD));
            });
            //Check if we should add the "hold shift to select" text
            if(context.getCustomer().isEditing())
            {
                //Add just below the items name
                list.add(1,(handler.isAdvancedEdit() ? Component.literal("?Hold Shift to Select") : Component.literal("?Hold Shift to Edit")).withStyle(ChatFormatting.YELLOW));
            }
            list.add(Component.literal("Trade Info:").withStyle(ChatFormatting.YELLOW));
            if(originalName.get() != null)
                list.add(Component.literal("Original Name: ").withStyle(ChatFormatting.YELLOW).append(originalName.get()));
        };
    }

}