package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageChangeSettings2 {
	
	private static final int MAX_TYPE_LENGTH = 100;
	
	UUID traderID;
	ResourceLocation type;
	CompoundTag updateInfo;
	
	public MessageChangeSettings2(UUID traderID, ResourceLocation type, CompoundTag updateInfo)
	{
		this.traderID = traderID;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	public static void encode(MessageChangeSettings2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeUtf(message.type.toString(), MAX_TYPE_LENGTH);
		buffer.writeNbt(message.updateInfo);
	}

	public static MessageChangeSettings2 decode(FriendlyByteBuf buffer) {
		return new MessageChangeSettings2(buffer.readUUID(), new ResourceLocation(buffer.readUtf(MAX_TYPE_LENGTH)), buffer.readAnySizeNbt());
	}

	public static void handle(MessageChangeSettings2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				UniversalTraderData data = TradingOffice.getData(message.traderID);
				if(data != null)
				{
					data.changeSettings(message.type, player, message.updateInfo);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
