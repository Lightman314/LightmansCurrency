package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler.ITradeRuleMessageHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageUpdateTradeRule2 {

	private UUID traderID;
	private int index;
	private ResourceLocation type;
	private CompoundNBT updateInfo;
	
	public MessageUpdateTradeRule2(UUID traderID, ResourceLocation type, CompoundNBT updateInfo) {
		this(traderID, -1, type, updateInfo);
	}
	
	public MessageUpdateTradeRule2(UUID traderID, int index, ResourceLocation type, CompoundNBT updateInfo) {
		this.traderID = traderID;
		this.index = index;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	public static void encode(MessageUpdateTradeRule2 message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
		buffer.writeInt(message.index);
		buffer.writeString(message.type.toString());
		buffer.writeCompoundTag(message.updateInfo);
	}
	
	public static MessageUpdateTradeRule2 decode(PacketBuffer buffer) {
		return new MessageUpdateTradeRule2(buffer.readUniqueId(), buffer.readInt(), new ResourceLocation(buffer.readString(1000)), buffer.readCompoundTag());
	}
	
	public static void handle(MessageUpdateTradeRule2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->{
			PlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				UniversalTraderData trader = TradingOffice.getData(message.traderID);
				if(trader instanceof ITradeRuleMessageHandler)
				{
					((ITradeRuleMessageHandler)trader).receiveTradeRuleMessage(player, message.index, message.type, message.updateInfo);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
