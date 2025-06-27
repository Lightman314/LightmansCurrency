package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.BooleanOption;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.UnaryOperator;

public class SPacketTaxInfo extends ServerToClientPacket {

    public static final Handler<SPacketTaxInfo> HANDLER = new H();

    private final boolean serverTax;
    private final boolean playerTax;

    public SPacketTaxInfo(boolean server, boolean player) { this.serverTax = server; this.playerTax = player; }

    public static void sendPacket(List<ITaxCollector> taxCollectors, Player player) {
        boolean serverTax = false;
        boolean playerTax = false;
        for(ITaxCollector tc : taxCollectors)
        {
            if(playerTax && serverTax)
                break;
            if(tc.isServerEntry())
                serverTax = true;
            else
                playerTax = true;
        }
        new SPacketTaxInfo(serverTax,playerTax).sendTo(player);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.serverTax);
        buffer.writeBoolean(this.playerTax);
    }

    private static class H extends Handler<SPacketTaxInfo>
    {

        private H() { }

        @Nonnull
        @Override
        public SPacketTaxInfo decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketTaxInfo(buffer.readBoolean(),buffer.readBoolean()); }

        @Override
        protected void handle(@Nonnull SPacketTaxInfo message, @Nullable ServerPlayer sender) {
            TextEntry firstMessage;
            BooleanOption config;
            if(message.serverTax && (!message.playerTax || !LCConfig.CLIENT.playerTaxWarning.get()))
            {
                config = LCConfig.CLIENT.serverTaxWarning;
                firstMessage = LCText.MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER_SERVER_ONLY;
            }
            else
            {
                config = LCConfig.CLIENT.playerTaxWarning;
                firstMessage = LCText.MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER;
            }
            if(!config.get())
                return;

            //Send Messages
            LightmansCurrency.getProxy().sendClientMessage(firstMessage.get().withStyle(disableEffect(config)));
            LightmansCurrency.getProxy().sendClientMessage(LCText.MESSAGE_TAX_COLLECTOR_PLACEMENT_TRADER_INFO.get().withStyle(disableEffect(config)));

        }

    }

    private static UnaryOperator<Style> disableEffect(BooleanOption configOption)
    {
        ConfigFile file = configOption.getFile();
        if(file == null)
            return UnaryOperator.identity();
        return style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,LCText.MESSAGE_TAX_COLLECTOR_PLACEMENT_DISABLE.get()))
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/lcconfig edit " + configOption.getFile().getFileID() + " " + configOption.getFullName() + " false"));
    }


}