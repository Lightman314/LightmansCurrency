package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageUpdateTradeRule {

	private long traderID;
	private ResourceLocation type;
	private CompoundTag updateInfo;
	
	public MessageUpdateTradeRule(long traderID, ResourceLocation type, CompoundTag updateInfo) {
		this.traderID = traderID;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	public static void encode(MessageUpdateTradeRule message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.traderID);
		buffer.writeUtf(message.type.toString());
		buffer.writeNbt(message.updateInfo);
	}
	
	public static MessageUpdateTradeRule decode(FriendlyByteBuf buffer) {
		return new MessageUpdateTradeRule(buffer.readLong(), new ResourceLocation(buffer.readUtf()), buffer.readAnySizeNbt());
	}
	
	public static void handle(MessageUpdateTradeRule message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->{
			Player player = supplier.get().getSender();
			if(player != null)
			{
				TraderData trader = TraderSaveData.GetTrader(false, message.traderID);
				if(trader != null)
				{
					//TODO update trade rule data
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
