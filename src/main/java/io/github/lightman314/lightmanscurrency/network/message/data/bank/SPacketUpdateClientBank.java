package io.github.lightman314.lightmanscurrency.network.message.data.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SPacketUpdateClientBank extends ServerToClientPacket {

	private static final Type<SPacketUpdateClientBank> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_data_update_bank"));
	public static final Handler<SPacketUpdateClientBank> HANDLER = new H();

	UUID player;
	CompoundTag bankData;
	
	public SPacketUpdateClientBank(@Nonnull UUID player, @Nonnull CompoundTag bankData) {
		super(TYPE);
		this.player = player;
		this.bankData = bankData;
	}
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketUpdateClientBank message) { buffer.writeUUID(message.player); buffer.writeNbt(message.bankData); }
	private static SPacketUpdateClientBank decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketUpdateClientBank(buffer.readUUID(), readNBT(buffer)); }


	private static class H extends Handler<SPacketUpdateClientBank>
	{
		protected H() { super(TYPE, easyCodec(SPacketUpdateClientBank::encode,SPacketUpdateClientBank::decode)); }

		@Override
		protected void handle(@Nonnull SPacketUpdateClientBank message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			context.enqueueWork(() -> LightmansCurrency.getProxy().updateBankAccount(message.player, message.bankData));
		}
	}

}
