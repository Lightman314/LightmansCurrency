package io.github.lightman314.lightmanscurrency.network.message.persistentdata;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketCreatePersistentAuction extends ClientToServerPacket {

	public static final Handler<CPacketCreatePersistentAuction> HANDLER = new H();

	private static final String GENERATE_ID_FORMAT = "auction_";
	
	final CompoundTag auctionData;
	final String id;
	
	public CPacketCreatePersistentAuction(CompoundTag auctionData, String id) {
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
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeNbt(this.auctionData);
		buffer.writeUtf(this.id);
	}

	private static class H extends Handler<CPacketCreatePersistentAuction>
	{
		@Nonnull
		@Override
		public CPacketCreatePersistentAuction decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketCreatePersistentAuction(buffer.readAnySizeNbt(), buffer.readUtf()); }
		@Override
		protected void handle(@Nonnull CPacketCreatePersistentAuction message, @Nullable ServerPlayer sender) {
			if(LCAdminMode.isAdminPlayer(sender))
			{
				boolean generateID = message.id.isBlank();
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
							sender.sendSystemMessage(EasyText.translatable("lightmanscurrency.message.persistent.auction.overwrite", message.id));
							return;
						}
					}

					//If no trader found with the id, add to list
					persistentAuctions.add(auctionJson);
					TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_AUCTION_SECTION, persistentAuctions);
					sender.sendSystemMessage(EasyText.translatable("lightmanscurrency.message.persistent.auction.add", message.id));
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
							sender.sendSystemMessage(EasyText.translatable("lightmanscurrency.message.persistent.auction.add", genID));
							return;
						}
					}
					LightmansCurrency.LogError("Could not generate ID, as all auction_# ID's are somehow spoken for.");

				}
			}
			else if(sender != null)
				sender.sendSystemMessage(EasyText.translatable("lightmanscurrency.message.persistent.auction.fail"));
		}
	}
	
}
