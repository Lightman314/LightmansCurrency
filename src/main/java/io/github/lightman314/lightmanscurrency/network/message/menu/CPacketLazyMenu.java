package io.github.lightman314.lightmanscurrency.network.message.menu;

import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketLazyMenu extends ClientToServerPacket {

    private static final Type<CPacketLazyMenu> TYPE = new Type<>(VersionUtil.lcResource("c_lazy_menu"));
    public static final Handler<CPacketLazyMenu> HANDLER = new H();

    private final int menuID;
    private final LazyPacketData data;
    public CPacketLazyMenu(int menuID, LazyPacketData data) { super(TYPE); this.menuID = menuID; this.data = data; }
    public CPacketLazyMenu(int menuID, LazyPacketData.Builder data) { this(menuID,data.build()); }

    private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull CPacketLazyMenu message) { buffer.writeInt(message.menuID); message.data.encode(buffer); }
    private static CPacketLazyMenu decode(@Nonnull RegistryFriendlyByteBuf buffer) { return new CPacketLazyMenu(buffer.readInt(),LazyPacketData.decode(buffer)); }

    private static class H extends Handler<CPacketLazyMenu>
    {
        protected H() { super(TYPE, fancyCodec(CPacketLazyMenu::encode,CPacketLazyMenu::decode)); }
        @Override
        protected void handle(@Nonnull CPacketLazyMenu message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            if(player.containerMenu instanceof LazyMessageMenu menu && menu.containerId == menu.containerId)
                menu.handleMessage(message.data);
        }
    }

}
