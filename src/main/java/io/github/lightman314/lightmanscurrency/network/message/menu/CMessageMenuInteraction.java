package io.github.lightman314.lightmanscurrency.network.message.menu;

import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CMessageMenuInteraction {

    private final LazyPacketData data;
    public CMessageMenuInteraction(LazyPacketData data) { this.data = data; }
    public CMessageMenuInteraction(LazyPacketData.Builder data) { this(data.build()); }

    public static void encode(CMessageMenuInteraction message, FriendlyByteBuf buffer) { message.data.encode(buffer); }

    public static CMessageMenuInteraction decode(FriendlyByteBuf buffer) { return new CMessageMenuInteraction(LazyPacketData.decode(buffer)); }

    public static void handle(CMessageMenuInteraction message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() -> {
            Player sender = supplier.get().getSender();
            if(sender != null && sender.containerMenu instanceof LazyMessageMenu menu)
                menu.HandleMessage(message.data);
        });
        supplier.get().setPacketHandled(true);
    }

}
