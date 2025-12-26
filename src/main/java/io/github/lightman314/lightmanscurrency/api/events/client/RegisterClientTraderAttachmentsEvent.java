package io.github.lightman314.lightmanscurrency.api.events.client;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.events.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.client.ClientTraderAttachment;
import io.github.lightman314.lightmanscurrency.util.ListUtil;

import java.util.ArrayList;
import java.util.List;

public class RegisterClientTraderAttachmentsEvent extends TraderEvent {

    private final TraderData trader;
    private final List<ClientTraderAttachment> attachments = new ArrayList<>();
    public RegisterClientTraderAttachmentsEvent(TraderData trader) {
        super(trader.getID());
        this.trader = trader;
    }
    @Override
    public TraderData getTrader() { return this.trader; }

    public List<Object> getResults() { return ListUtil.convertList(this.attachments); }
    public List<ClientTraderAttachment> getAttachments() { return ImmutableList.copyOf(this.attachments); }
    public void register(ClientTraderAttachment attachment) { this.attachments.add(attachment); }
    public void remove(ClientTraderAttachment attachment) { this.attachments.remove(attachment); }
    public void clearAttachments() { this.attachments.clear(); }

}
