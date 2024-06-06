package io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.client;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class AuctionTradeButtonRenderer extends TradeRenderManager<AuctionTradeData> {

    public AuctionTradeButtonRenderer(AuctionTradeData trade) { super(trade); }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 94; }

    @Override
    public LazyOptional<ScreenPosition> arrowPosition(TradeContext context) { return ScreenPosition.ofOptional(36, 1); }

    @Override
    public DisplayData inputDisplayArea(TradeContext context) { return new DisplayData(1, 1, 34, 16); }

    @Override
    public List<DisplayEntry> getInputDisplays(TradeContext context) { return Lists.newArrayList(DisplayEntry.of(this.trade.getLastBidAmount(), this.getBidInfo(), true)); }

    private List<Component> getBidInfo() {
        List<Component> bidInfo = new ArrayList<>();
        if(this.trade.getLastBidPlayer() == null)
        {
            //First bid info
            bidInfo.add(LCText.TOOLTIP_TRADER_AUCTION_INFO_NO_BIDDER.get());
            bidInfo.add(LCText.TOOLTIP_TRADER_AUCTION_INFO_STARTING_BID.get(this.trade.getLastBidAmount().getText()));
        }
        else
        {
            //Last bid info
            bidInfo.add(LCText.TOOLTIP_TRADER_AUCTION_INFO_LAST_BIDDER.get(this.trade.getLastBidPlayer().getName(true)));
            bidInfo.add(LCText.TOOLTIP_TRADER_AUCTION_INFO_LAST_BID.get(this.trade.getLastBidAmount().getString()));
            //Next bid info
            bidInfo.add(LCText.TOOLTIP_TRADER_AUCTION_INFO_MIN_BID.get(this.trade.getMinNextBid().getString()));
        }
        return bidInfo;
    }

    @Override
    public DisplayData outputDisplayArea(TradeContext context) { return new DisplayData(58, 1, 34, 16); }

    @Override
    public List<DisplayEntry> getOutputDisplays(TradeContext context) {
        List<DisplayEntry> entries = new ArrayList<>();
        for (ItemStack item : this.trade.getAuctionItems()) {
            if (!item.isEmpty())
                entries.add(DisplayEntry.of(item, item.getCount(), Screen.getTooltipFromItem(Minecraft.getInstance(), item)));
        }
        return entries;
    }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) { alerts.clear(); }

    @Override
    public void renderAdditional(EasyWidget button, EasyGuiGraphics gui, TradeContext context) {
        //Draw remaining time
        TimeUtil.TimeData time = new TimeUtil.TimeData(this.trade.getRemainingTime(TimeUtil.getCurrentTime()));
        TextRenderUtil.drawCenteredText(gui, time.getShortString(1), button.getWidth() / 2, button.getHeight() - 9, this.getTextColor(time));
    }

    @Override
    public List<Component> getAdditionalTooltips(TradeContext context, int mouseX, int mouseY) {
        TimeUtil.TimeData time = new TimeUtil.TimeData(this.trade.getRemainingTime(TimeUtil.getCurrentTime()));
        return Lists.newArrayList(LCText.TOOLTIP_TRADER_AUCTION_TIME_REMAINING.get(EasyText.literal(time.getString()).withStyle(s -> s.withColor(this.getTextColor(time)))));
    }

    private int getTextColor(TimeUtil.TimeData remainingTime) {

        if(remainingTime.miliseconds < TimeUtil.DURATION_HOUR)
        {
            if(remainingTime.miliseconds < 5 * TimeUtil.DURATION_MINUTE) //Red if less than 5 minutes
                return 0xFF0000;
            //Yellow if less than 1 hour
            return 0xFFFF00;
        }
        //Green if more than 1 hour
        return 0x00FF00;
    }

}
