package io.github.lightman314.lightmanscurrency.features.trader.paygate.trade.client;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.TradeDisplayEntry;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader.trade_displays.TextDisplay;
import io.github.lightman314.lightmanscurrency.api.helpers.MathHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.TooltipHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TickTimeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeUnit;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.TradeButtonDisplay;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.WorldNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.message.MessageType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.message.TradeMessage;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.OutputConflictHandling;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.blocks.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.nodes.PaygateTradesNode;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.trade.PaygateTradeData;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PaygateTradeButtonDisplay extends TradeButtonDisplay {

    public static final TradeButtonDisplay INSTANCE = new PaygateTradeButtonDisplay();

    @Override
    public int getSlotWidth(TradeData trade,TradeSlotType section,TradeContext context,ITradeInteractionHandler handler) {
        if(section.isInput())
            return 33;
        if(section.isArrow())
            return ARROW_WIDTH;
        if(section.isOutput()) {
            //128 pixels if a description should be shown
            if(trade instanceof PaygateTradeData pt && !pt.getDescription().isBlank())
                return 128;
            //Otherwise only the default of 33
            return 33;
        }
        return 0;
    }

    @Override
    public List<TradeDisplayEntry> getDisplayEntries(TradeData trade, TradeSlotType section, TradeContext context, boolean hovered, ITradeInteractionHandler handler) {
        if(section.isArrow())
            return arrowWithMessages(hovered,trade,context,this);
        if(trade instanceof PaygateTradeData pt) {
            if(section.isInput())
                return forPrice(trade,context,33,handler);
            else if(section.isOutput()) {
                TradeDisplayEntry display;
                if(pt.getDescription().isBlank())
                    display = TextDisplay.of(TickTimeHelper.formatDurationDisplay(pt.getDuration()),List.of(TickTimeHelper.formatDuration(pt.getDuration()), PaygateTradesNode.GUI_REDSTONE_LEVEL.get(pt.getRedstoneLevel())),33);
                else
                    display = TextDisplay.of(Component.literal(pt.getDescription()),TooltipHelper.splitTooltips(pt.getDescriptionTooltip()),128);
                return List.of(display);
            }
        }
        return List.of();
    }

    @Override
    public void addAdditionalMessages(TradeData trade,TradeContext context,TradeEvent.Pre event) {
        super.addAdditionalMessages(trade,context,event);
        if(context.hasNode(PaygateTradesNode.TYPE) && context.hasNode(WorldNode.TYPE) && trade instanceof PaygateTradeData pt && context.getNode(WorldNode.TYPE).getBlockEntity(context.getLevel()) instanceof PaygateBlockEntity blockEntity) {
            OutputConflictHandling handling = context.getNodeValue(PaygateTradesNode.TYPE,PaygateTradesNode::getConflictHandling);
            PaygateTradesNode node = context.getNode(PaygateTradesNode.TYPE);
            if(node.hasConflict(blockEntity,pt.getOutputSides())) {
                event.addMessage(TradeMessage.of(PaygateTradesNode.TOOLTIP_ALREADY_ACTIVE.get(),handling.allowsConflicts ? MessageType.NEUTRAL : MessageType.WARN),false);
                int seconds = MathHelper.divideAndRoundUp(blockEntity.getTimeRemaining(pt.getOutputSides()),20);
                event.addNeutral(PaygateTradesNode.TOOLTIP_TIME_REMAINING.get(Component.literal(String.valueOf(seconds)).append(TimeUnit.SECOND.getShortText())));
            }
        }
    }
}