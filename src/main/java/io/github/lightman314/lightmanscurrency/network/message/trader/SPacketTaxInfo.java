package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.BooleanOption;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.UnaryOperator;

public class SPacketTaxInfo extends ServerToClientPacket {

    public static final Type<SPacketTaxInfo> TYPE = new Type<>(VersionUtil.lcResource("s_trader_tax_warning"));
    public static final Handler<SPacketTaxInfo> HANDLER = new H();

    private final boolean serverTax;
    private final boolean playerTax;

    public SPacketTaxInfo(boolean server, boolean player) { super(TYPE); this.serverTax = server; this.playerTax = player; }

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

    private static void encode(FriendlyByteBuf buffer,SPacketTaxInfo message) { buffer.writeBoolean(message.serverTax); buffer.writeBoolean(message.playerTax); }

    private static SPacketTaxInfo decode(FriendlyByteBuf buffer) { return new SPacketTaxInfo(buffer.readBoolean(),buffer.readBoolean()); }

    private static class H extends Handler<SPacketTaxInfo>
    {

        private H() { super(TYPE, StreamCodec.of(SPacketTaxInfo::encode,SPacketTaxInfo::decode)); }

        @Override
        protected void handle(@Nonnull SPacketTaxInfo message, @Nonnull IPayloadContext context, @Nonnull Player player) {


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
