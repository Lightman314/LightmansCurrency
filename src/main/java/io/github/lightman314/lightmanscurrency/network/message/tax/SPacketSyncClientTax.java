package io.github.lightman314.lightmanscurrency.network.message.tax;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketSyncClientTax extends ServerToClientPacket {

    public static final Handler<SPacketSyncClientTax> HANDLER = new H();

    private final CompoundTag updateTag;
    public SPacketSyncClientTax(CompoundTag updateTag) { this.updateTag = updateTag; }

    public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeNbt(this.updateTag); }

    private static class H extends Handler<SPacketSyncClientTax>
    {
        @Nonnull
        @Override
        public SPacketSyncClientTax decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncClientTax(buffer.readAnySizeNbt()); }
        @Override
        protected void handle(@Nonnull SPacketSyncClientTax message, @Nullable ServerPlayer sender) {
            LightmansCurrency.getProxy().updateTaxEntries(message.updateTag);
        }
    }

}
