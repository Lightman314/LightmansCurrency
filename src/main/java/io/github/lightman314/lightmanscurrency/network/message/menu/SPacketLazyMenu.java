package io.github.lightman314.lightmanscurrency.network.message.menu;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketLazyMenu extends ServerToClientPacket {

    private static final Type<SPacketLazyMenu> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_lazy_menu"));
    public static final Handler<SPacketLazyMenu> HANDLER = new H();

    private final int menuID;
    private final LazyPacketData data;
    public SPacketLazyMenu(int menuID,LazyPacketData data) { super(TYPE); this.menuID = menuID; this.data = data; }
    public SPacketLazyMenu(int menuID, LazyPacketData.Builder data) { this(menuID,data.build()); }

    private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull SPacketLazyMenu message) { buffer.writeInt(message.menuID); message.data.encode(buffer); }
    private static SPacketLazyMenu decode(@Nonnull RegistryFriendlyByteBuf buffer) { return new SPacketLazyMenu(buffer.readInt(),LazyPacketData.decode(buffer)); }

    private static class H extends Handler<SPacketLazyMenu>
    {
        protected H() { super(TYPE, fancyCodec(SPacketLazyMenu::encode,SPacketLazyMenu::decode)); }
        @Override
        protected void handle(@Nonnull SPacketLazyMenu message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            if(player.containerMenu instanceof LazyMessageMenu menu && menu.containerId == menu.containerId)
                menu.HandleMessage(message.data);
        }
    }

}
