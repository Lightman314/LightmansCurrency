package io.github.lightman314.lightmanscurrency.network.message.tax;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketUpdateClientTax extends ServerToClientPacket {

    private static final Type<SPacketUpdateClientTax> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_taxdata_update_client"));
    public static final Handler<SPacketUpdateClientTax> HANDLER = new H();

    private final CompoundTag updateTag;
    public SPacketUpdateClientTax(CompoundTag updateTag) { super(TYPE); this.updateTag = updateTag; }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketUpdateClientTax message) { buffer.writeNbt(message.updateTag); }
    private static SPacketUpdateClientTax decode(@Nonnull FriendlyByteBuf buffer) { return  new SPacketUpdateClientTax(readNBT(buffer)); }

    private static class H extends Handler<SPacketUpdateClientTax>
    {
        protected H() { super(TYPE, easyCodec(SPacketUpdateClientTax::encode, SPacketUpdateClientTax::decode)); }
        @Override
        protected void handle(@Nonnull SPacketUpdateClientTax message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            LightmansCurrency.PROXY.updateTaxEntries(message.updateTag);
        }
    }

}
