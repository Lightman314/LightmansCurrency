package io.github.lightman314.lightmanscurrency.network.message.lazy;

import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.packet.BiDirectionalPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class BPacketLazyMenu extends BiDirectionalPacket {

    private static final Type<BPacketLazyMenu> TYPE = bType("lazy_menu");
    private static final StreamCodec<RegistryFriendlyByteBuf, BPacketLazyMenu> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,p -> p.menuID,
            LazyPacketData.STREAM_CODEC,p -> p.data,
            BPacketLazyMenu::new);
    public static final Handler<BPacketLazyMenu> HANDLER = new H();

    private final int menuID;
    private final LazyPacketData data;
    public BPacketLazyMenu(int menuID, LazyPacketData data) { super(TYPE); this.menuID = menuID; this.data = data; }
    public BPacketLazyMenu(int menuID, LazyPacketData.Builder data) { this(menuID,data.build()); }

    private static class H extends Handler<BPacketLazyMenu>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(BPacketLazyMenu message, IPayloadContext context, Player player) {
            if(player.containerMenu instanceof LazyMessageMenu menu && menu.containerId == menu.containerId)
                menu.handleMessage(message.data);
        }
    }

}
