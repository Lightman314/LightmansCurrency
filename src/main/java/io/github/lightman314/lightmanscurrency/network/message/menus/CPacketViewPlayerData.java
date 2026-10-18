package io.github.lightman314.lightmanscurrency.network.message.menus;

import io.github.lightman314.lightmanscurrency.api.world.menu.provider.FancyMenuProvider;
import io.github.lightman314.lightmanscurrency.features.player_data.PlayerDataMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketViewPlayerData extends ClientToServerPacket {

    public static final Type<CPacketViewPlayerData> TYPE = cType("view_player_data");
    private static final CPacketViewPlayerData INSTANCE = new CPacketViewPlayerData();
    public static final StreamCodec<ByteBuf,CPacketViewPlayerData> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private CPacketViewPlayerData() { super(TYPE); }

    public static void sendToServer() { INSTANCE.send(); }

    @Override
    protected void handle(IPayloadContext context,Player player) {
        if(player.containerMenu == player.inventoryMenu)
            player.openMenu(new FancyMenuProvider((id,inv,p) -> new PlayerDataMenu(id,player)));
    }

}