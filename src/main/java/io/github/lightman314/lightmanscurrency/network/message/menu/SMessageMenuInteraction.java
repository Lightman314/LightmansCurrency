package io.github.lightman314.lightmanscurrency.network.message.menu;

import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SMessageMenuInteraction {

    private final LazyPacketData data;
    public SMessageMenuInteraction(LazyPacketData data) { this.data = data; }
    public SMessageMenuInteraction(LazyPacketData.Builder data) { this(data.build()); }

    public static void encode(SMessageMenuInteraction message, FriendlyByteBuf buffer) { message.data.encode(buffer); }

    public static SMessageMenuInteraction decode(FriendlyByteBuf buffer) { return new SMessageMenuInteraction(LazyPacketData.decode(buffer)); }

    public static void handle(SMessageMenuInteraction message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if(mc.player != null && mc.player.containerMenu instanceof LazyMessageMenu menu)
                menu.HandleMessage(message.data);
        });
        supplier.get().setPacketHandled(true);
    }

}
