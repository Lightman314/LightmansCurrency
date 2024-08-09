package io.github.lightman314.lightmanscurrency.network.message.menu;

import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketLazyMenu extends ServerToClientPacket {

    public static final Handler<SPacketLazyMenu> HANDLER = new H();

    private final int menuID;
    private final LazyPacketData data;
    public SPacketLazyMenu(int menuID,LazyPacketData data) { this.menuID = menuID; this.data = data; }
    public SPacketLazyMenu(int menuID, LazyPacketData.Builder data) { this(menuID,data.build()); }

    public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeInt(this.menuID); this.data.encode(buffer); }

    private static class H extends Handler<SPacketLazyMenu>
    {
        @Nonnull
        @Override
        public SPacketLazyMenu decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketLazyMenu(buffer.readInt(),LazyPacketData.decode(buffer)); }
        @Override
        protected void handle(@Nonnull SPacketLazyMenu message, @Nullable ServerPlayer sender) {
            Minecraft mc = Minecraft.getInstance();
            if(mc.player != null && mc.player.containerMenu instanceof LazyMessageMenu menu && menu.containerId == message.menuID)
                menu.HandleMessage(message.data);
        }
    }

}
