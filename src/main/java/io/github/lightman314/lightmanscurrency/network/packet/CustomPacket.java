package io.github.lightman314.lightmanscurrency.network.packet;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public abstract class CustomPacket implements CustomPacketPayload {

    public static <T extends CustomPacket> Type<T> bType(String id) { return bType(LCApi.MODID,id); }
    public static <T extends CustomPacket> Type<T> bType(String modid,String id) { return new Type<>(Identifier.fromNamespaceAndPath(modid,"b_" + id)); }
    public static <T extends CustomPacket> Type<T> cType(String id) { return cType(LCApi.MODID,id); }
    public static <T extends CustomPacket> Type<T> cType(String modid,String id) { return new Type<>(Identifier.fromNamespaceAndPath(modid,"c_" + id)); }
    public static <T extends CustomPacket> Type<T> sType(String id) { return sType(LCApi.MODID,id); }
    public static <T extends CustomPacket> Type<T> sType(String modid,String id) { return new Type<>(Identifier.fromNamespaceAndPath(modid,"s_" + id)); }

    private final Type<?> type;
    protected CustomPacket(Type<?> type) { this.type = type;}

    @Override
    public Type<? extends CustomPacketPayload> type() { return this.type; }

    public final void handle(IPayloadContext context) { this.handle(context,context.player()); }
    protected abstract void handle(IPayloadContext context,Player player);

}