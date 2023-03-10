package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class CPacketSetVisible {

	int entityID;
	boolean visible;
	
	public CPacketSetVisible(int entityID, boolean visible) {
		this.entityID = entityID;
		this.visible = visible;
	}
	
	public static void encode(CPacketSetVisible message, PacketBuffer buffer) {
		buffer.writeInt(message.entityID);
		buffer.writeBoolean(message.visible);
	}
	
	public static CPacketSetVisible decode(PacketBuffer buffer) {
		return new CPacketSetVisible(buffer.readInt(), buffer.readBoolean());
	}
	
	public static void handle(CPacketSetVisible message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
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
