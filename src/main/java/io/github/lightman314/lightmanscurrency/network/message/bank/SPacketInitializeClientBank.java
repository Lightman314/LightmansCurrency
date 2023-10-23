package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SPacketInitializeClientBank extends ServerToClientPacket {

	public static final Handler<SPacketInitializeClientBank> HANDLER = new H();

	final CompoundTag compound;
	
	public SPacketInitializeClientBank(CompoundTag compound) { this.compound = compound; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeNbt(this.compound); }

	private static class H extends Handler<SPacketInitializeClientBank>
	{
		@Nonnull
		@Override
		public SPacketInitializeClientBank decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketInitializeClientBank(buffer.readNbt()); }
		@Override
		protected void handle(@Nonnull SPacketInitializeClientBank message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.initializeBankAccounts(message.compound);
		}
	}

}
