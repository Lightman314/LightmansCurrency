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

    public static final Type<BPacketMenuMessage> TYPE = bType("menu_message");
    public static final StreamCodec<RegistryFriendlyByteBuf,BPacketMenuMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,p -> p.menuID,
            FancyPacketMap.STREAM_CODEC,p -> p.message,
            BPacketMenuMessage::new);

    private final int menuID;
    private final FancyPacketMap message;

    public BPacketMenuMessage(int menuID,FancyPacketMap message) {
        super(TYPE);
        this.menuID = menuID;
        this.message = message.immutable();
    }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        if(player.containerMenu instanceof MessageMenu menu && menu.containerId == this.menuID)
            menu.receiveMessage(this.message);
    }

}