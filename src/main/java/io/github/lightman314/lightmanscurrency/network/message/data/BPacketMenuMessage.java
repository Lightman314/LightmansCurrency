package io.github.lightman314.lightmanscurrency.network.message.data;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.world.menu.MessageMenu;
import io.github.lightman314.lightmanscurrency.network.packet.BiDirectionalPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class BPacketMenuMessage extends BiDirectionalPacket {

    private static final Type<BPacketMenuMessage> TYPE = bType("menu_message");
    private static final StreamCodec<RegistryFriendlyByteBuf,BPacketMenuMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,p -> p.menuID,
            FancyPacketMap.STREAM_CODEC,p -> p.message,
            BPacketMenuMessage::new);
    public static final Handler<BPacketMenuMessage> HANDLER = new H();

    private final int menuID;
    private final FancyPacketMap message;

    public BPacketMenuMessage(int menuID,FancyPacketMap message) {
        super(TYPE);
        this.menuID = menuID;
        this.message = message.immutable();
    }

    private static class H extends Handler<BPacketMenuMessage>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(BPacketMenuMessage message, IPayloadContext context, Player player) {
            if(player.containerMenu instanceof MessageMenu menu && menu.containerId == message.menuID)
                menu.receiveMessage(message.message);
        }
    }

}