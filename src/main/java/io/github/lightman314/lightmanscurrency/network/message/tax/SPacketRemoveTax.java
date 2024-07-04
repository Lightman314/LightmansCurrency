package io.github.lightman314.lightmanscurrency.network.message.tax;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketRemoveTax extends ServerToClientPacket {

    private static final Type<SPacketRemoveTax> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_taxdata_remove_client"));
    public static final Handler<SPacketRemoveTax> HANDLER = new H();

    private final long id;
    public SPacketRemoveTax(long id) { super(TYPE); this.id = id; }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketRemoveTax message) { buffer.writeLong(message.id); }
    private static SPacketRemoveTax decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketRemoveTax(buffer.readLong()); }

    private static class H extends Handler<SPacketRemoveTax>
    {
        protected H() { super(TYPE, easyCodec(SPacketRemoveTax::encode,SPacketRemoveTax::decode)); }
        @Override
        protected void handle(@Nonnull SPacketRemoveTax message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            LightmansCurrency.PROXY.removeTaxEntry(message.id);
        }
    }

}
