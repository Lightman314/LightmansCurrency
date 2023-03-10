package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.items.WalletItem.DataWriter;
import io.github.lightman314.lightmanscurrency.common.menus.providers.WalletBankMenuProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkHooks;

public class MessageOpenWalletBank {
	
	private final int walletStackIndex;
	
	public MessageOpenWalletBank(int walletStackIndex) { this.walletStackIndex = walletStackIndex;  }
	
	public static void encode(MessageOpenWalletBank message, PacketBuffer buffer) { buffer.writeInt(message.walletStackIndex); }

	public static MessageOpenWalletBank decode(PacketBuffer buffer) {
		return new MessageOpenWalletBank(buffer.readInt());
	}

	public static void handle(MessageOpenWalletBank message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
				NetworkHooks.openGui(player, new WalletBankMenuProvider(message.walletStackIndex), new DataWriter(message.walletStackIndex));
		});
		supplier.get().setPacketHandled(true);
	}

}
