package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketWalletInteraction extends ClientToServerPacket {

	public static final Handler<CPacketWalletInteraction> HANDLER = new H();

	int clickedSlot;
	boolean heldShift;
	ItemStack heldStack;
	
	public CPacketWalletInteraction(int clickedSlot, boolean heldShift, ItemStack heldStack) {
		this.clickedSlot = clickedSlot;
		this.heldShift = heldShift;
		this.heldStack = heldStack;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeInt(this.clickedSlot);
		buffer.writeBoolean(this.heldShift);
		buffer.writeItemStack(this.heldStack, false);
	}

	private static class H extends Handler<CPacketWalletInteraction>
	{
		@Nonnull
		@Override
		public CPacketWalletInteraction decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketWalletInteraction(buffer.readInt(), buffer.readBoolean(), buffer.readItem()); }
		@Override
		protected void handle(@Nonnull CPacketWalletInteraction message, @Nullable ServerPlayer sender) {
			if(sender != null)
				WalletCapability.WalletSlotInteraction(sender, message.clickedSlot, message.heldShift, message.heldStack);
		}
	}
}
