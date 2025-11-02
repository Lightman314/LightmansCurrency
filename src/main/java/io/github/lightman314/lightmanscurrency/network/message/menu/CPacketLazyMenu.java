package io.github.lightman314.lightmanscurrency.network.message.menu;

import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.network.packet.CustomPacket;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketLazyMenu extends ClientToServerPacket {

    public static final Handler<CPacketLazyMenu> HANDLER = new H();

    private final int menuID;
    private final LazyPacketData data;
    public CPacketLazyMenu(int menuID, LazyPacketData data) { this.menuID = menuID; this.data = data; }
    public CPacketLazyMenu(int menuID, LazyPacketData.Builder data) { this(menuID,data.build()); }

    public void encode(FriendlyByteBuf buffer) { buffer.writeInt(this.menuID); this.data.encode(buffer); }

    private static class H extends CustomPacket.Handler<CPacketLazyMenu>
    {
        @Override
        public CPacketLazyMenu decode(FriendlyByteBuf buffer) { return new CPacketLazyMenu(buffer.readInt(),LazyPacketData.decode(buffer)); }
        @Override
        protected void handle(CPacketLazyMenu message, Player player) {
            if(player.containerMenu instanceof LazyMessageMenu menu && menu.containerId == menu.containerId)
                menu.handleMessage(message.data);
        }
    }

}
