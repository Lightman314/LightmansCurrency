package io.github.lightman314.lightmanscurrency.api.trader.trade.message;

import io.github.lightman314.lightmanscurrency.api.helpers.TooltipHelper;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TradeMessage {

    public static final Comparator<TradeMessage> SORTER = Comparator.comparingInt(TradeMessage::getPriority).reversed();

    private final Component message;
    private int getPriority() { return this.type.priority(); }
    public final MessageType type;
    private TradeMessage(Component message, MessageType type)
    {
        this.message = message;
        this.type = type;
    }

    public List<Component> getFormattedMessage() { return TooltipHelper.splitTooltips(this.message,this.type); }

    public static List<Component> getFormattedMessages(Iterable<TradeMessage> messages) {
        List<Component> list = new ArrayList<>();
        for(TradeMessage message : messages)
            list.addAll(message.getFormattedMessage());
        return list;
    }

    public static TradeMessage helpful(TextEntry message) { return helpful(message.get()); }
    public static TradeMessage helpful(Component message) { return of(message,MessageType.HELPFUL); }
    public static TradeMessage neutral(TextEntry message) { return neutral(message.get()); }
    public static TradeMessage neutral(Component message) { return of(message,MessageType.NEUTRAL); }
    public static TradeMessage warn(TextEntry message) { return warn(message.get()); }
    public static TradeMessage warn(Component message) { return of(message,MessageType.WARN); }
    public static TradeMessage error(TextEntry message) { return error(message.get()); }
    public static TradeMessage error(Component message) { return of(message,MessageType.ERROR); }

    public static TradeMessage hidden(TextEntry message) { return hidden(message.get()); }
    public static TradeMessage hidden(Component message) { return of(message,MessageType.INVISIBLE); }

    public static TradeMessage of(Component message,MessageType type) { return new TradeMessage(message,type); }

}