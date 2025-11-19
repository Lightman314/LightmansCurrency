package io.github.lightman314.lightmanscurrency.api.events;

import com.google.common.collect.ImmutableSet;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.attachments.TraderAttachment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TraderEvent extends Event {

    private final long traderID;
    public final long getID() { return this.traderID; }
    public final OwnerData getOwner() { return this.getTrader() == null ? null : this.getTrader().getOwner(); }
    public TraderData getTrader() { return TraderAPI.getApi().GetTrader(false, this.traderID); }

    protected TraderEvent(long traderID) { this.traderID = traderID; }

    public static class RegisterAttachmentEvent extends TraderEvent
    {
        private final TraderData trader;
        private final Set<TraderAttachment.TraderAttachmentType<?>> attachments;
        public Set<TraderAttachment.TraderAttachmentType<?>> getAttachments() { return ImmutableSet.copyOf(this.attachments); }
        public RegisterAttachmentEvent(TraderData trader)
        {
            super(trader.getID());
            this.trader = trader;
            this.attachments = new HashSet<>();
        }
        @Override
        public TraderData getTrader() { return this.trader; }
        public void addAttachment(TraderAttachment.TraderAttachmentType<?> type) { this.attachments.add(type); }
    }

    public static class CreateNetworkTraderEvent extends TraderEvent
    {

        private final Player player;
        public Player getPlayer() { return this.player; }

        public CreateNetworkTraderEvent(long traderID, Player player)
        {
            super(traderID);
            this.player = player;
        }
    }

    public static class RemoveNetworkTraderEvent extends TraderEvent
    {

        private final TraderData data;
        @Override
        public TraderData getTrader() { return this.data; }

        public RemoveNetworkTraderEvent(long traderID, TraderData removedData)
        {
            super(traderID);
            this.data = removedData;
        }
    }

}