package io.github.lightman314.lightmanscurrency.network.message.persistentdata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageAddPersistentTrader {

	private static final String GENERATE_ID_FORMAT = "trader_";
	
	final UUID traderID;
	final String id;
	final String owner;
	
	public MessageAddPersistentTrader(UUID traderID, String id, String owner) {
		this.traderID = traderID;
		this.id = id;
		this.owner = owner.isBlank() ? "Minecraft" : owner;
	}
	
	private JsonObject getTraderJson(UniversalTraderData trader, String id) {
		JsonObject traderJson = new JsonObject();
		traderJson.addProperty("id", this.id);
		traderJson.addProperty("OwnerName", this.owner);
		traderJson = trader.saveToJson(traderJson);
		return traderJson;
	}
	
	public static void encode(MessageAddPersistentTrader message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeUtf(message.id);
		buffer.writeUtf(message.owner);
	}
	
	public static MessageAddPersistentTrader decode(FriendlyByteBuf buffer) {
		return new MessageAddPersistentTrader(buffer.readUUID(), buffer.readUtf(), buffer.readUtf());
	}
	
	public static void handle(MessageAddPersistentTrader message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			//Only allow if player is in admin mode
			ServerPlayer player = supplier.get().getSender();
			if(TradingOffice.isAdminPlayer(player))
			{
				UniversalTraderData trader = TradingOffice.getData(message.traderID);
				if(trader != null)
				{
					
					boolean generateID = message.id.isBlank();
					
					if(!generateID)
					{
						JsonObject traderJson = message.getTraderJson(trader, message.id);
						
						JsonArray persistentTraders = TradingOffice.getPersistentTraderJson(TradingOffice.PERSISTENT_TRADER_SECTION);
						//Check for traders with the same id, and replace any entries that match
						for(int i = 0; i < persistentTraders.size(); ++i)
						{
							JsonObject traderData = persistentTraders.get(i).getAsJsonObject();
							if(traderData.has("id") && traderData.get("id").getAsString().equals(message.id))
							{
								//Overwrite the existing entry with the same id.
								persistentTraders.set(i, traderJson);
								TradingOffice.setPersistentTraderSection(TradingOffice.PERSISTENT_TRADER_SECTION, persistentTraders);
								player.sendMessage(new TranslatableComponent("lightmanscurrency.message.persistent.trader.overwrite", message.id), new UUID(0,0));
								return;
							}
						}
						
						//If no trader found with the id, add to list
						persistentTraders.add(traderJson);
						TradingOffice.setPersistentTraderSection(TradingOffice.PERSISTENT_TRADER_SECTION, persistentTraders);
						player.sendMessage(new TranslatableComponent("lightmanscurrency.message.persistent.trader.add", message.id), new UUID(0,0));
						return;
					}
					else
					{
						//Get a list of all known trader IDs
						List<String> knownIDs = new ArrayList<>();
						JsonArray persistentTraders = TradingOffice.getPersistentTraderJson(TradingOffice.PERSISTENT_TRADER_SECTION);
						for(int i = 0; i < persistentTraders.size(); ++i)
						{
							JsonObject traderData = persistentTraders.get(i).getAsJsonObject();
							if(traderData.has("id"))
								knownIDs.add(traderData.get("id").getAsString());
						}
						
						//Check trader_1 -> trader_2147483646 to find an available id
						for(int i = 1; i < Integer.MAX_VALUE; ++i)
						{
							String genID = GENERATE_ID_FORMAT + String.valueOf(i);
							if(knownIDs.stream().noneMatch(id -> id.equals(genID)))
							{
								persistentTraders.add(message.getTraderJson(trader, genID));
								TradingOffice.setPersistentTraderSection(TradingOffice.PERSISTENT_TRADER_SECTION, persistentTraders);
								player.sendMessage(new TranslatableComponent("lightmanscurrency.message.persistent.trader.add", genID), new UUID(0,0));
								return;
							}
						}
						LightmansCurrency.LogError("Could not generate ID, as all trader_# ID's are somehow spoken for.");
					}
				}
			}
			player.sendMessage(new TranslatableComponent("lightmanscurrency.message.persistent.trader.fail"), new UUID(0,0));
		});
		supplier.get().setPacketHandled(true);
	}
	
	
}