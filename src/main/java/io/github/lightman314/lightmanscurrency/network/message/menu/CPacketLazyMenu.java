package io.github.lightman314.lightmanscurrency.network.message.menu;

import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.network.packet.CustomPacket;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketLazyMenu extends ClientToServerPacket {

    public static final Handler<CPacketLazyMenu> HANDLER = new H();

    private final LazyPacketData data;
    public CPacketLazyMenu(LazyPacketData data) { this.data = data; }
    public CPacketLazyMenu(LazyPacketData.Builder data) { this(data.build()); }

    public void encode(@Nonnull FriendlyByteBuf buffer) { this.data.encode(buffer); }

    private static class H extends CustomPacket.Handler<CPacketLazyMenu>
    {
        @Nonnull
        @Override
        public CPacketLazyMenu decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketLazyMenu(LazyPacketData.decode(buffer)); }
        @Override
        protected void handle(@Nonnull CPacketLazyMenu message, @Nullable ServerPlayer sender) {
            if(sender != null && sender.containerMenu instanceof LazyMessageMenu menu)
                menu.HandleMessage(message.data);
        }
    }

}
