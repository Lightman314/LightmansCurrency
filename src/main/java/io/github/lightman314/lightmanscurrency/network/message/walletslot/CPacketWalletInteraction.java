package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class CPacketWalletInteraction {

	int clickedSlot;
	boolean heldShift;
	ItemStack heldStack;
	
	public CPacketWalletInteraction(int clickedSlot, boolean heldShift, ItemStack heldStack) {
		this.clickedSlot = clickedSlot;
		this.heldShift = heldShift;
		this.heldStack = heldStack;
	}
	
	public static void encode(CPacketWalletInteraction message, PacketBuffer buffer) {
		buffer.writeInt(message.clickedSlot);
		buffer.writeBoolean(message.heldShift);
		buffer.writeItemStack(message.heldStack, false);
	}
	
	public static CPacketWalletInteraction decode(PacketBuffer buffer) {
		return new CPacketWalletInteraction(buffer.readInt(), buffer.readBoolean(), buffer.readItem());
	}
	
	public static void handle(CPacketWalletInteraction message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
				WalletCapability.WalletSlotInteraction(player, message.clickedSlot, message.heldShift, message.heldStack);
		});
		supplier.get().setPacketHandled(true);
	}
}
