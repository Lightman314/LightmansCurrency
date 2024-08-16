package io.github.lightman314.lightmanscurrency.network.message.data.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class SPacketDeleteClientBank extends ServerToClientPacket {

    public static final Handler<SPacketDeleteClientBank> HANDLER = new H();

    private final UUID player;

    public SPacketDeleteClientBank(@Nonnull UUID player) { this.player = player; }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeUUID(this.player); }

    private static class H extends Handler<SPacketDeleteClientBank>
    {
        @Nonnull
        @Override
        public SPacketDeleteClientBank decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketDeleteClientBank(buffer.readUUID()); }
        @Override
        protected void handle(@Nonnull SPacketDeleteClientBank message, @Nullable ServerPlayer sender) {
            LightmansCurrency.PROXY.removeBankAccount(message.player);
        }
    }
}
