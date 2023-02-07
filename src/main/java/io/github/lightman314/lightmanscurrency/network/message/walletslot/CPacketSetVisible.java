package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent.Context;

public class CPacketSetVisible {

	int entityID;
	boolean visible;
	
	public CPacketSetVisible(int entityID, boolean visible) {
		this.entityID = entityID;
		this.visible = visible;
	}
	
	public static void encode(CPacketSetVisible message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.entityID);
		buffer.writeBoolean(message.visible);
	}
	
	public static CPacketSetVisible decode(FriendlyByteBuf buffer) {
		return new CPacketSetVisible(buffer.readInt(), buffer.readBoolean());
	}
	
	public static void handle(CPacketSetVisible message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			Entity entity = player.level.getEntity(message.entityID);
			if(entity != null)
			{
				IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(entity);
				if(walletHandler != null)
					walletHandler.setVisible(message.visible);
			}
		});
		supplier.get().setPacketHandled(true);
	}
}
