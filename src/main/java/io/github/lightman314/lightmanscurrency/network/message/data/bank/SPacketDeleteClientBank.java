package io.github.lightman314.lightmanscurrency.network.message.data.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SPacketDeleteClientBank extends ServerToClientPacket {

    private static final Type<SPacketDeleteClientBank> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_data_delete_bank"));
    public static final Handler<SPacketDeleteClientBank> HANDLER = new H();

    private final UUID player;

    public SPacketDeleteClientBank(@Nonnull UUID player) { super(TYPE); this.player = player; }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketDeleteClientBank message) { buffer.writeUUID(message.player); }
    private static SPacketDeleteClientBank decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketDeleteClientBank(buffer.readUUID()); }

    private static class H extends Handler<SPacketDeleteClientBank>
    {
        protected H() { super(TYPE, easyCodec(SPacketDeleteClientBank::encode,SPacketDeleteClientBank::decode)); }
        @Override
        protected void handle(@Nonnull SPacketDeleteClientBank message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            LightmansCurrency.getProxy().removeBankAccount(message.player);
        }
    }

}
