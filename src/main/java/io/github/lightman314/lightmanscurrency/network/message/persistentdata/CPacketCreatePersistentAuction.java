package io.github.lightman314.lightmanscurrency.network.message.persistentdata;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketCreatePersistentAuction extends ClientToServerPacket {

	private static final Type<CPacketCreatePersistentAuction> TYPE = cType("persistent_create_auction");
	private static final StreamCodec<RegistryFriendlyByteBuf,CPacketCreatePersistentAuction> STREAM_CODEC = StreamCodec.composite(
            AuctionTradeData.STREAM_CODEC,p -> p.auction,
            ByteBufCodecs.STRING_UTF8,p -> p.id,
            CPacketCreatePersistentAuction::new);
    public static final Handler<CPacketCreatePersistentAuction> HANDLER = new H();

	private static final String GENERATE_ID_FORMAT = "auction_";
	
	final AuctionTradeData auction;
	final String id;
	
	public CPacketCreatePersistentAuction(AuctionTradeData auction, String id) {
		super(TYPE);
		this.auction = auction;
		this.id = id;
	}
    
	private JsonObject getAuctionJson(String id,DataContext<JsonElement> context) {
		JsonObject json = new JsonObject();
		json.addProperty("id", id);
		json = this.auction.saveToJson(json,context);
		return json;
	}

	private static class H extends Handler<CPacketCreatePersistentAuction>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(CPacketCreatePersistentAuction message, IPayloadContext context, Player player) {
			if(LCAdminMode.isAdminPlayer(player))
			{

				TraderDataCache data = TraderDataCache.TYPE.get(false);
				if(data == null)
					return;

				RegistryAccess lookup = player.registryAccess();
                DataContext<JsonElement> dataContext = DataContext.createJson(lookup);
				boolean generateID = message.id.isBlank();

				if(!generateID) {

					JsonObject auctionJson = message.getAuctionJson(message.id,dataContext);

					JsonArray persistentAuctions = data.getPersistentTraderJson(TraderDataCache.PERSISTENT_AUCTION_SECTION);
					//Check for auctions with the same id, and replace any entries that match
					for(int i = 0; i < persistentAuctions.size(); ++i)
					{
						JsonObject auctionData = persistentAuctions.get(i).getAsJsonObject();
						if(auctionData.has("id") && auctionData.get("id").getAsString().equals(message.id))
						{
							//Overwrite the existing entry with the same id.
							persistentAuctions.set(i, auctionJson);
							data.setPersistentTraderSection(TraderDataCache.PERSISTENT_AUCTION_SECTION, persistentAuctions,dataContext);
							player.sendSystemMessage(LCText.MESSAGE_PERSISTENT_AUCTION_OVERWRITE.get(message.id));
							return;
						}
					}

					//If no trader found with the id, add to list
					persistentAuctions.add(auctionJson);
					data.setPersistentTraderSection(TraderDataCache.PERSISTENT_AUCTION_SECTION,persistentAuctions,dataContext);
					player.sendSystemMessage(LCText.MESSAGE_PERSISTENT_AUCTION_ADD.get(message.id));
				}
				else
				{
					//Get a list of all known trader IDs
					List<String> knownIDs = new ArrayList<>();
					JsonArray persistentAuctions = data.getPersistentTraderJson(TraderDataCache.PERSISTENT_AUCTION_SECTION);
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
							persistentAuctions.add(message.getAuctionJson(genID,dataContext));
							data.setPersistentTraderSection(TraderDataCache.PERSISTENT_AUCTION_SECTION,persistentAuctions,dataContext);
							player.sendSystemMessage(LCText.MESSAGE_PERSISTENT_AUCTION_ADD.get(genID));
							return;
						}
					}
					LightmansCurrency.LogError("Could not generate ID, as all auction_# ID's are somehow spoken for.");

				}
			}
			else
				player.sendSystemMessage(LCText.MESSAGE_PERSISTENT_AUCTION_FAIL.get());
		}
	}
	
}
