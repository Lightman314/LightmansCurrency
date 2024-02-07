package io.github.lightman314.lightmanscurrency.network.message.data.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class SPacketUpdateClientBank extends ServerToClientPacket {

	public static final Handler<SPacketUpdateClientBank> HANDLER = new H();

	UUID player;
	CompoundTag bankData;
	
	public SPacketUpdateClientBank(@Nonnull UUID player, @Nonnull CompoundTag bankData) {
		this.player = player;
		this.bankData = bankData;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeUUID(this.player); buffer.writeNbt(this.bankData); }

	private static class H extends Handler<SPacketUpdateClientBank>
	{
		@Nonnull
		@Override
		public SPacketUpdateClientBank decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketUpdateClientBank(buffer.readUUID(), buffer.readAnySizeNbt()); }
		@Override
		protected void handle(@Nonnull SPacketUpdateClientBank message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.updateBankAccount(message.player, message.bankData);
		}
	}

}
