package io.github.lightman314.lightmanscurrency.network.message.menu;

import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketLazyMenu extends ServerToClientPacket {

    public static final Handler<SPacketLazyMenu> HANDLER = new H();

    private final LazyPacketData data;
    public SPacketLazyMenu(LazyPacketData data) { this.data = data; }
    public SPacketLazyMenu(LazyPacketData.Builder data) { this(data.build()); }

    public void encode(@Nonnull FriendlyByteBuf buffer) { this.data.encode(buffer); }

    private static class H extends Handler<SPacketLazyMenu>
    {
        @Nonnull
        @Override
        public SPacketLazyMenu decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketLazyMenu(LazyPacketData.decode(buffer)); }
        @Override
        protected void handle(@Nonnull SPacketLazyMenu message, @Nullable ServerPlayer sender) {
            Minecraft mc = Minecraft.getInstance();
            if(mc.player != null && mc.player.containerMenu instanceof LazyMessageMenu menu)
                menu.HandleMessage(message.data);
        }
    }

}
