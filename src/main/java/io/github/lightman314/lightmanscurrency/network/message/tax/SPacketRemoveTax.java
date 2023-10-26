package io.github.lightman314.lightmanscurrency.network.message.tax;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketRemoveTax extends ServerToClientPacket {

    public static final Handler<SPacketRemoveTax> HANDLER = new H();

    private final long id;
    public SPacketRemoveTax(long id) { this.id = id; }

    public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeLong(this.id); }

    private static class H extends Handler<SPacketRemoveTax>
    {
        @Nonnull
        @Override
        public SPacketRemoveTax decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketRemoveTax(buffer.readLong()); }
        @Override
        protected void handle(@Nonnull SPacketRemoveTax message, @Nullable ServerPlayer sender) {
            LightmansCurrency.PROXY.removeTaxEntry(message.id);
        }
    }

}
