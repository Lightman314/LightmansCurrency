package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler.ITradeRuleMessageHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageUpdateTradeRule2 {

	private UUID traderID;
	private int index;
	private ResourceLocation type;
	private CompoundTag updateInfo;
	
	public MessageUpdateTradeRule2(UUID traderID, ResourceLocation type, CompoundTag updateInfo) {
		this(traderID, -1, type, updateInfo);
	}
	
	public MessageUpdateTradeRule2(UUID traderID, int index, ResourceLocation type, CompoundTag updateInfo) {
		this.traderID = traderID;
		this.index = index;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	public static void encode(MessageUpdateTradeRule2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeInt(message.index);
		buffer.writeUtf(message.type.toString());
		buffer.writeNbt(message.updateInfo);
	}
	
	public static MessageUpdateTradeRule2 decode(FriendlyByteBuf buffer) {
		return new MessageUpdateTradeRule2(buffer.readUUID(), buffer.readInt(), new ResourceLocation(buffer.readUtf()), buffer.readAnySizeNbt());
	}
	
	public static void handle(MessageUpdateTradeRule2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->{
			Player player = supplier.get().getSender();
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
