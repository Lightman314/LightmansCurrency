package io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.client.gui.components.AbstractWidget;
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
    public TradeButton.DisplayData inputDisplayArea(TradeContext context) { return new TradeButton.DisplayData(1, 1, 34, 16); }

    @Override
    public List<TradeButton.DisplayEntry> getInputDisplays(TradeContext context) { return Lists.newArrayList(TradeButton.DisplayEntry.of(this.trade.getLastBidAmount(), this.getBidInfo(), true)); }

    private List<Component> getBidInfo() {
        List<Component> bidInfo = new ArrayList<>();
        if(this.trade.getLastBidPlayer() == null)
        {
            //First bid info
            bidInfo.add(EasyText.translatable("tooltip.lightmanscurrency.auction.nobidder"));
            bidInfo.add(EasyText.translatable("tooltip.lightmanscurrency.auction.minbid", this.trade.getLastBidAmount().getString()));
        }
        else
        {
            //Last bid info
            bidInfo.add(EasyText.translatable("tooltip.lightmanscurrency.auction.lastbidder", this.trade.getLastBidPlayer().getName(true)));
            bidInfo.add(EasyText.translatable("tooltip.lightmanscurrency.auction.currentbid", this.trade.getLastBidAmount().getString()));
            //Next bid info
            bidInfo.add(EasyText.translatable("tooltip.lightmanscurrency.auction.minbid", this.trade.getMinNextBid().getString()));
        }
        return bidInfo;
    }

    @Override
    public TradeButton.DisplayData outputDisplayArea(TradeContext context) { return new TradeButton.DisplayData(58, 1, 34, 16); }

    @Override
    public List<TradeButton.DisplayEntry> getOutputDisplays(TradeContext context) {
        List<TradeButton.DisplayEntry> entries = new ArrayList<>();
        for (ItemStack item : this.trade.getAuctionItems()) {
            if (!item.isEmpty())
                entries.add(TradeButton.DisplayEntry.of(item, item.getCount(), ItemRenderUtil.getTooltipFromItem(item)));
        }
        return entries;
    }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) { alerts.clear(); }

    @Override
    public void renderAdditional(AbstractWidget button, PoseStack pose, int mouseX, int mouseY, TradeContext context) {
        //Draw remaining time
        TimeUtil.TimeData time = new TimeUtil.TimeData(this.trade.getRemainingTime(TimeUtil.getCurrentTime()));
        TextRenderUtil.drawCenteredText(pose, time.getShortString(1), button.x + button.getWidth() / 2, button.y + button.getHeight() - 9, this.getTextColor(time));
    }

    @Override
    public List<Component> getAdditionalTooltips(TradeContext context, int mouseX, int mouseY) {
        TimeUtil.TimeData time = new TimeUtil.TimeData(this.trade.getRemainingTime(TimeUtil.getCurrentTime()));
        return Lists.newArrayList(EasyText.translatable("gui.lightmanscurrency.auction.time_remaining", EasyText.literal(time.getString()).withStyle(s -> s.withColor(this.getTextColor(time)))));
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