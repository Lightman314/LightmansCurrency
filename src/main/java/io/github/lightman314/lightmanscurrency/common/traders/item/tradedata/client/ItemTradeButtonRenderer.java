package io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.client;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ItemTradeButtonRenderer extends TradeRenderManager<ItemTradeData> {

    public static final ResourceLocation NBT_SLOT = new ResourceLocation(LightmansCurrency.MODID, "item/empty_nbt_highlight");
    public static final Pair<ResourceLocation,ResourceLocation> NBT_BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS,NBT_SLOT);

    public ItemTradeButtonRenderer(ItemTradeData trade) { super(trade); }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 94; }

    @Override
    public LazyOptional<ScreenPosition> arrowPosition(TradeContext context) { return ScreenPosition.ofOptional(36, 1); }

    @Override
    public TradeButton.DisplayData inputDisplayArea(TradeContext context) { return new TradeButton.DisplayData(1, 1, 34, 16); }

    @Override
    public List<TradeButton.DisplayEntry> getInputDisplays(TradeContext context) {
        //If this is a sale, this is the price
        if(this.trade.isSale())
            return Lists.newArrayList(TradeButton.DisplayEntry.of(this.trade.getCost(context), context.isStorageMode ? Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.trader.price_edit")) : null));
        if(this.trade.isPurchase())
            return this.getSaleItemEntries(context);
        if(this.trade.isBarter())
            return this.getBarterItemEntries(context);
        return new ArrayList<>();
    }


    @Override
    public TradeButton.DisplayData outputDisplayArea(TradeContext context) { return new TradeButton.DisplayData(59, 1, 34, 16); }

    @Override
    public List<TradeButton.DisplayEntry> getOutputDisplays(TradeContext context) {
        if(this.trade.isSale() || this.trade.isBarter())
            return this.getSaleItemEntries(context);
        if(this.trade.isPurchase())
            return Lists.newArrayList(TradeButton.DisplayEntry.of(this.trade.getCost(context)));
        return new ArrayList<>();
    }

    private List<TradeButton.DisplayEntry> getSaleItemEntries(TradeContext context) {

        List<TradeButton.DisplayEntry> entries = new ArrayList<>();
        for(int i = 0; i < 2; ++i)
        {
            ItemStack item = this.trade.getSellItem(i);
            if(!item.isEmpty())
                entries.add(TradeButton.DisplayEntry.of(item, item.getCount(), this.getSaleItemTooltip(item, this.trade.getCustomName(i), this.trade.getEnforceNBT(i), context), this.getNBTHightlight(this.trade.getEnforceNBT(i))));
            else if(context.isStorageMode)
                entries.add(TradeButton.DisplayEntry.of(this.trade.getRestriction().getEmptySlotBG(), Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.trader.item_edit"))));
        }
        return entries;
    }

    private List<Component> getSaleItemTooltip(ItemStack stack, String customName, boolean enforceNBT, TradeContext context)
    {
        if(stack.isEmpty())
        {
            if(context.isStorageMode)
                return Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.trader.item_edit"));
            return null;
        }

        List<Component> tooltips = this.getTooltipFromItem(stack, this.trade.isPurchase(), enforceNBT);
        Component originalName = null;
        if(!customName.isEmpty() && (this.trade.isSale() || this.trade.isBarter()))
        {
            originalName = tooltips.get(0);
            tooltips.set(0, EasyText.literal(customName).withStyle(ChatFormatting.GOLD));
        }
        //Stop here if this is in storage mode, and there's no custom name
        if(context.isStorageMode && originalName == null)
            return tooltips;

        //Trade Info
        tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.trader.info").withStyle(ChatFormatting.GOLD));
        //Custom Name
        if(originalName != null)
            tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.trader.originalname", originalName).withStyle(ChatFormatting.GOLD));

        if(context.hasTrader() && context.hasPlayerReference())
        {
            //Stock
            if(context.getTrader() instanceof ItemTraderData trader)
            {
                tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.trader.stock", trader.isCreative() ? EasyText.translatable("tooltip.lightmanscurrency.trader.stock.infinite").withStyle(ChatFormatting.GOLD) : EasyText.literal(String.valueOf(this.trade.stockCount(context))).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GOLD));
            }
        }

        return tooltips;

    }

    private List<TradeButton.DisplayEntry> getBarterItemEntries(TradeContext context) {
        List<TradeButton.DisplayEntry> entries = new ArrayList<>();
        for(int i = 0; i < 2; ++i)
        {
            ItemStack item = this.trade.getBarterItem(i);
            if(!item.isEmpty())
                entries.add(TradeButton.DisplayEntry.of(item, item.getCount(), this.getTooltipFromItem(item, true, this.trade.getEnforceNBT(i + 2)), this.getNBTHightlight(this.trade.getEnforceNBT(i + 2))));
            else if(context.isStorageMode)
                entries.add(TradeButton.DisplayEntry.of(EasySlot.BACKGROUND, Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.trader.item_edit"))));
        }
        return entries;
    }

    private Pair<ResourceLocation,ResourceLocation> getNBTHightlight(boolean enforceNBT) {
        return enforceNBT ? null : NBT_BACKGROUND;
    }

    private List<Component> getTooltipFromItem(ItemStack item, boolean purchase, boolean enforceNBT) {
        List<Component> tooltip = new ArrayList<>();
        if(!enforceNBT)
            tooltip.add(EasyText.translatable(purchase ? "gui.lightmanscurrency.warning.nbt.buying" : "gui.lightmanscurrency.warning.nbt.selling").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        tooltip.addAll(ItemRenderUtil.getTooltipFromItem(item));
        return tooltip;
    }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
        if(context.hasTrader() && context.getTrader() instanceof ItemTraderData trader)
        {
            if(!trader.isCreative())
            {
                //Check Stock
                if(this.trade.stockCount(context) <= 0)
                    alerts.add(AlertData.warn(EasyText.translatable("tooltip.lightmanscurrency.outofstock")));

                //Check Space (Purchase)
                if(this.trade.isPurchase())
                {
                    if(!this.trade.hasSpace(trader, context.getCollectableItems(this.trade.getItemRequirement(0), this.trade.getItemRequirement(1))))
                        alerts.add(AlertData.warn(EasyText.translatable("tooltip.lightmanscurrency.outofspace")));
                }
                //Check Space (Barter)
                if(this.trade.isBarter())
                {
                    if(!this.trade.hasSpace(trader, context.getCollectableItems(this.trade.getItemRequirement(2), this.trade.getItemRequirement(3))))
                        alerts.add(AlertData.warn(EasyText.translatable("tooltip.lightmanscurrency.outofspace")));
                }
            }
            //Check whether they can afford the cost
            if(!this.trade.canAfford(context))
                alerts.add(AlertData.warn(EasyText.translatable("tooltip.lightmanscurrency.cannotafford")));

        }
    }

}
