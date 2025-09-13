package io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.client;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.filter.FilterAPI;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display.EmptySlotEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display.ItemAndBackgroundEntry;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.IItemTradeFilter;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.ListUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ItemTradeButtonRenderer extends TradeRenderManager<ItemTradeData> {

    public static final ResourceLocation NBT_SLOT = VersionUtil.lcResource("item/empty_nbt_highlight");
    public static final ResourceLocation FILTER_SLOT = VersionUtil.lcResource("item/filter_highlight");
    public static final Pair<ResourceLocation,ResourceLocation> NBT_BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS,NBT_SLOT);
    public static final Pair<ResourceLocation,ResourceLocation> FILTER_BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS,FILTER_SLOT);

    public ItemTradeButtonRenderer(ItemTradeData trade) { super(trade); }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 94; }

    @Override
    public Optional<ScreenPosition> arrowPosition(TradeContext context) { return ScreenPosition.ofOptional(36, 1); }

    @Override
    public DisplayData inputDisplayArea(TradeContext context) { return new DisplayData(1, 1, 34, 16); }

    @Override
    public List<DisplayEntry> getInputDisplays(TradeContext context) {
        //If this is a sale, this is the price
        if(this.trade.isSale())
            return this.lazyPriceDisplayList(context);
        if(this.trade.isPurchase())
            return this.getSaleItemEntries(context);
        if(this.trade.isBarter())
            return this.getBarterItemEntries(context);
        return new ArrayList<>();
    }


    @Override
    public DisplayData outputDisplayArea(TradeContext context) { return new DisplayData(59, 1, 34, 16); }

    @Override
    public List<DisplayEntry> getOutputDisplays(TradeContext context) {
        if(this.trade.isSale() || this.trade.isBarter())
            return this.getSaleItemEntries(context);
        if(this.trade.isPurchase())
            return this.lazyPriceDisplayList(context);
        return new ArrayList<>();
    }

    private List<DisplayEntry> getSaleItemEntries(TradeContext context) {

        List<DisplayEntry> entries = new ArrayList<>();
        for(int i = 0; i < 2; ++i)
        {
            DisplayEntry entry = this.getSaleItemEntry(context,i);
            if(entry != null)
                entries.add(entry);
        }
        return entries;
    }

    @Nullable
    private DisplayEntry getSaleItemEntry(TradeContext context, int index)
    {
        ItemStack internalItem = this.trade.getActualItem(index);
        IItemTradeFilter filter = FilterAPI.tryGetFilter(internalItem);
        if(filter != null && this.trade.allowFilters() && filter.getFilter(internalItem) != null && context.getTrader() instanceof ItemTraderData trader)
        {
            List<ItemStack> displayItems;
            if(this.trade.isSale())
                displayItems = filter.getDisplayableItems(internalItem,trader.getStorage());
            else
                displayItems = filter.getDisplayableItems(internalItem,null);
            ItemStack displayItem = ListUtil.randomItemFromList(displayItems,internalItem);
            List<Component> tooltip = new ArrayList<>();
            List<Component> customTooltip = filter.getCustomTooltip(internalItem);
            if(customTooltip != null)
                tooltip.addAll(customTooltip);
            this.addItemEditInfo(tooltip,context);
            //Stock Info
            if(!context.isStorageMode && context.hasPlayerReference())
            {
                tooltip.add(LCText.TOOLTIP_TRADE_INFO_TITLE.getWithStyle(ChatFormatting.GOLD));
                tooltip.add(this.getStockTooltip(trader.isCreative(), this.trade.getStock(context)));
            }
            return ItemAndBackgroundEntry.of(displayItem,tooltip,FILTER_BACKGROUND);
        }
        ItemStack item = this.trade.getSellItem(index);
        if(!item.isEmpty())
            return ItemAndBackgroundEntry.of(item, this.getSaleItemTooltip(item, this.trade.getCustomName(index), this.trade.getEnforceNBT(index), context, index), this.getNBTHightlight(this.trade.getEnforceNBT(index)));
        else if(context.isStorageMode)
            return this.makeEmptySlot(this.trade.getRestriction().getEmptySlotBG(),context);
        return null;
    }

    private Consumer<List<Component>> getSaleItemTooltip(ItemStack stack, String customName, boolean enforceNBT, TradeContext context, int index)
    {
        return tooltips -> {
            boolean showCustomName = this.trade.getRestriction().displayCustomName(stack,this.trade,index);
            Component originalName = null;
            if(showCustomName && !customName.isEmpty() && (this.trade.isSale() || this.trade.isBarter()))
            {
                originalName = stack.getHoverName();
                tooltips.set(0,EasyText.literal(customName).withStyle(ChatFormatting.GOLD));
            }

            this.addNBTWarning(tooltips, this.trade.isPurchase(), enforceNBT);
            this.addItemEditInfo(tooltips, context);

            //Stop here if this is in storage mode, and there's no custom name
            if(context.isStorageMode && originalName == null)
                return;

            //Trade Info
            tooltips.add(LCText.TOOLTIP_TRADE_INFO_TITLE.getWithStyle(ChatFormatting.GOLD));
            //Custom Name
            if(originalName != null)
                tooltips.add(LCText.TOOLTIP_TRADE_INFO_ORIGINAL_NAME.get(originalName).withStyle(ChatFormatting.GOLD));

            if(context.hasTrader() && context.hasPlayerReference())
            {
                //Stock
                if(context.getTrader() instanceof ItemTraderData trader)
                {
                    tooltips.add(this.getStockTooltip(trader.isCreative(), this.trade.getStock(context)));
                }
            }
        };
    }

    private List<DisplayEntry> getBarterItemEntries(TradeContext context) {
        List<DisplayEntry> entries = new ArrayList<>();
        for(int i = 0; i < 2; ++i)
        {
            DisplayEntry entry = this.getBarterEntry(context,i);
            if(entry != null)
                entries.add(entry);
        }
        return entries;
    }

    @Nullable
    private DisplayEntry getBarterEntry(TradeContext context, int index)
    {
        ItemStack internalItem = this.trade.getActualItem(index + 2);
        IItemTradeFilter filter = FilterAPI.tryGetFilter(internalItem);
        if(filter != null && this.trade.allowFilters() && filter.getFilter(internalItem) != null && context.getTrader() instanceof ItemTraderData trader)
        {
            List<ItemStack> displayItems = filter.getDisplayableItems(internalItem,null);
            ItemStack displayItem = ListUtil.randomItemFromList(displayItems,internalItem);
            List<Component> tooltip = new ArrayList<>();
            List<Component> customTooltip = filter.getCustomTooltip(internalItem);
            if(customTooltip != null)
                tooltip.addAll(customTooltip);
            this.addItemEditInfo(tooltip,context);
            return ItemAndBackgroundEntry.of(displayItem,tooltip,FILTER_BACKGROUND);
        }
        ItemStack item = this.trade.getBarterItem(index);
        if(!item.isEmpty())
            return ItemAndBackgroundEntry.of(item, this.getBarterTooltips(this.trade.getEnforceNBT(index + 2), context), this.getNBTHightlight(this.trade.getEnforceNBT(index + 2)));
        else if(context.isStorageMode)
            return this.makeEmptySlot(EasySlot.BACKGROUND,context);
        return null;
    }

    private DisplayEntry makeEmptySlot(Pair<ResourceLocation,ResourceLocation> background, TradeContext context)
    {
        return EmptySlotEntry.of(background,this.hasPermission(context,Permissions.EDIT_TRADES) ? LCText.TOOLTIP_TRADE_ITEM_EDIT_EMPTY.getAsListWithStyle(ChatFormatting.YELLOW) : null);
    }

    private Pair<ResourceLocation,ResourceLocation> getNBTHightlight(boolean enforceNBT) {
        return enforceNBT ? null : NBT_BACKGROUND;
    }

    private void addItemEditInfo(@Nonnull List<Component> tooltips, TradeContext context)
    {
        if(context.isStorageMode && this.hasPermission(context,Permissions.EDIT_TRADES))
            tooltips.addFirst(LCText.TOOLTIP_TRADE_ITEM_EDIT_SHIFT.getWithStyle(ChatFormatting.YELLOW));
    }

    private void addNBTWarning(@Nonnull List<Component> tooltips, boolean purchase, boolean enforceNBT)
    {
        if(!enforceNBT) //Put NBT warning at the top of the tooltip. Should only be called after swapping out any custom names, etc.
            tooltips.addFirst((purchase ? LCText.TOOLTIP_TRADE_ITEM_NBT_WARNING_PURCHASE.get() : LCText.TOOLTIP_TRADE_ITEM_NBT_WARNING_SALE.get()).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
    }

    private Consumer<List<Component>> getBarterTooltips(boolean enforceNBT, TradeContext context) {
        return tooltips -> {
            this.addNBTWarning(tooltips, true, enforceNBT);
            this.addItemEditInfo(tooltips, context);
        };
    }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
        if(context.hasTrader() && context.getTrader() instanceof ItemTraderData trader)
        {
            if(!trader.isCreative())
            {
                //Check Stock
                if(this.trade.outOfStock(context))
                    alerts.add(AlertData.warn(LCText.TOOLTIP_OUT_OF_STOCK));

                //Check Space (Purchase)
                if(this.trade.isPurchase())
                {
                    if(!this.trade.hasSpace(trader, context.getCollectableItems(this.trade.getItemRequirement(0), this.trade.getItemRequirement(1))))
                        alerts.add(AlertData.warn(LCText.TOOLTIP_OUT_OF_SPACE));
                }
                //Check Space (Barter)
                if(this.trade.isBarter())
                {
                    if(!this.trade.hasSpace(trader, context.getCollectableItems(this.trade.getItemRequirement(2), this.trade.getItemRequirement(3))))
                        alerts.add(AlertData.warn(LCText.TOOLTIP_OUT_OF_SPACE));
                }
            }
            //Check whether they can afford the cost
            if(!this.trade.canAfford(context))
                alerts.add(AlertData.warn(LCText.TOOLTIP_CANNOT_AFFORD));

        }
    }

}
