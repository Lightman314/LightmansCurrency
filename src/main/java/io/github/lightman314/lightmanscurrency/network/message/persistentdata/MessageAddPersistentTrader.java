package io.github.lightman314.lightmanscurrency.network.message.persistentdata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageAddPersistentTrader {

	private static final String GENERATE_ID_FORMAT = "trader_";
	
	final long traderID;
	final String id;
	final String owner;
	
	public MessageAddPersistentTrader(long traderID, String id, String owner) {
		this.traderID = traderID;
		this.id = id;
		this.owner = owner.isBlank() ? "Minecraft" : owner;
	}
	
	private JsonObject getTraderJson(TraderData trader, String id) throws Exception {
		JsonObject traderJson = trader.saveToJson();
		traderJson.addProperty("ID", this.id);
		traderJson.addProperty("OwnerName", this.owner);
		return traderJson;
	}
	
	public static void encode(MessageAddPersistentTrader message, FriendlyByteBuf buffer) {
		buffer.writeLong(message.traderID);
		buffer.writeUtf(message.id);
		buffer.writeUtf(message.owner);
	}
	
	public static MessageAddPersistentTrader decode(FriendlyByteBuf buffer) {
		return new MessageAddPersistentTrader(buffer.readLong(), buffer.readUtf(), buffer.readUtf());
	}
	
	public static void handle(MessageAddPersistentTrader message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			//Only allow if player is in admin mode
			ServerPlayer player = supplier.get().getSender();
			if(CommandLCAdmin.isAdminPlayer(player))
			{
				TraderData trader = TraderSaveData.GetTrader(false, message.traderID);
				if(trader != null && trader.canMakePersistent())
				{
					
					boolean generateID = message.id.isBlank();
					
					if(!generateID)
					{
						try {
							JsonObject traderJson = message.getTraderJson(trader, message.id);
							
							JsonArray persistentTraders = TraderSaveData.getPersistentTraderJson(TraderSaveData.PERSISTENT_TRADER_SECTION);
							//Check for traders with the same id, and replace any entries that match
							for(int i = 0; i < persistentTraders.size(); ++i)
							{
								JsonObject traderData = persistentTraders.get(i).getAsJsonObject();
								if(traderData.has("id") && traderData.get("id").getAsString().equals(message.id))
								{
									//Overwrite the existing entry with the same id.
									persistentTraders.set(i, traderJson);
									TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders);
									player.sendMessage(new TranslatableComponent("lightmanscurrency.message.persistent.trader.overwrite", message.id), new UUID(0,0));
									return;
								}
							}
							
							//If no trader found with the id, add to list
							persistentTraders.add(traderJson);
							TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders);
							player.sendMessage(new TranslatableComponent("lightmanscurrency.message.persistent.trader.add", message.id), new UUID(0,0));
							return;
						} catch (Throwable t) { t.printStackTrace(); }
					}
					else
					{
						try {
							//Get a list of all known trader IDs
							List<String> knownIDs = new ArrayList<>();
							JsonArray persistentTraders = TraderSaveData.getPersistentTraderJson(TraderSaveData.PERSISTENT_TRADER_SECTION);
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
									TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders);
									player.sendMessage(new TranslatableComponent("lightmanscurrency.message.persistent.trader.add", genID), new UUID(0,0));
									return;
								}
							}
							LightmansCurrency.LogError("Could not generate ID, as all trader_# ID's are somehow spoken for.");
						} catch(Throwable t) { t.printStackTrace(); }
					}
				}
			}
			player.sendMessage(new TranslatableComponent("lightmanscurrency.message.persistent.trader.fail"), new UUID(0,0));
		});
		supplier.get().setPacketHandled(true);
	}
	
	
}