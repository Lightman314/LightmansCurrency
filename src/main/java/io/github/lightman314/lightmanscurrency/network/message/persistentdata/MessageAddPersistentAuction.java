package io.github.lightman314.lightmanscurrency.network.message.persistentdata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageAddPersistentAuction {

	private static final String GENERATE_ID_FORMAT = "auction_";
	
	final CompoundNBT auctionData;
	final String id;
	
	public MessageAddPersistentAuction(CompoundNBT auctionData, String id) {
		this.auctionData = auctionData;
		this.id = id;
	}
	
	private JsonObject getAuctionJson(String id) {
		AuctionTradeData auction = new AuctionTradeData(this.auctionData);
		JsonObject json = new JsonObject();
		json.addProperty("id", id);
		json = auction.saveToJson(json);
		return json;
	}
	
	public static void encode(MessageAddPersistentAuction message, PacketBuffer buffer) {
		buffer.writeNbt(message.auctionData);
		buffer.writeUtf(message.id);
	}
	
	public static MessageAddPersistentAuction decode(PacketBuffer buffer) {
		return new MessageAddPersistentAuction(buffer.readAnySizeNbt(), buffer.readUtf());
	}
	
	public static void handle(MessageAddPersistentAuction message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			//Only allow if player is in admin mode
			ServerPlayerEntity player = supplier.get().getSender();
			if(CommandLCAdmin.isAdminPlayer(player))
			{
				boolean generateID = message.id.isEmpty();
				if(!generateID) {
					
					JsonObject auctionJson = message.getAuctionJson(message.id);
					
					JsonArray persistentAuctions = TraderSaveData.getPersistentTraderJson(TraderSaveData.PERSISTENT_AUCTION_SECTION);
					//Check for auctions with the same id, and replace any entries that match
					for(int i = 0; i < persistentAuctions.size(); ++i)
					{
						JsonObject auctionData = persistentAuctions.get(i).getAsJsonObject();
						if(auctionData.has("id") && auctionData.get("id").getAsString().equals(message.id))
						{
							//Overwrite the existing entry with the same id.
							persistentAuctions.set(i, auctionJson);
							TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_AUCTION_SECTION, persistentAuctions);
							player.sendMessage(EasyText.translatable("lightmanscurrency.message.persistent.auction.overwrite", message.id), new UUID(0,0));
							return;
						}
					}
					
					//If no trader found with the id, add to list
					persistentAuctions.add(auctionJson);
					TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_AUCTION_SECTION, persistentAuctions);
					player.sendMessage(EasyText.translatable("lightmanscurrency.message.persistent.auction.add", message.id), new UUID(0,0));
					return;
				}
				else
				{
					//Get a list of all known trader IDs
					List<String> knownIDs = new ArrayList<>();
					JsonArray persistentAuctions = TraderSaveData.getPersistentTraderJson(TraderSaveData.PERSISTENT_AUCTION_SECTION);
					for(int i = 0; i < persistentAuctions.size(); ++i)
					{
						JsonObject auctionData = persistentAuctions.get(i).getAsJsonObject();
						if(auctionData.has("id"))
							knownIDs.add(auctionData.get("id").getAsString());
					}
					
					//Check auction_1 -> auction_2147483646 to find an available id
					for(int i = 1; i < Integer.MAX_VALUE; ++i)
					{
						String genID = GENERATE_ID_FORMAT + i;
						if(knownIDs.stream().noneMatch(id -> id.equals(genID)))
						{
							persistentAuctions.add(message.getAuctionJson(genID));
							TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_AUCTION_SECTION, persistentAuctions);
							player.sendMessage(EasyText.translatable("lightmanscurrency.message.persistent.auction.add", genID), new UUID(0,0));
							return;
						}
					}
					LightmansCurrency.LogError("Could not generate ID, as all auction_# ID's are somehow spoken for.");
					
				}
			}
			player.sendMessage(EasyText.translatable("lightmanscurrency.message.persistent.auction.fail"), new UUID(0,0));
		});
		supplier.get().setPacketHandled(true);
	}
	
}