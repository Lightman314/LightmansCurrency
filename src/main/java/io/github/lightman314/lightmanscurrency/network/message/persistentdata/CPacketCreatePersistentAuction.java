package io.github.lightman314.lightmanscurrency.network.message.persistentdata;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketCreatePersistentAuction extends ClientToServerPacket {

	private static final Type<CPacketCreatePersistentAuction> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_persistent_create_auction"));
	public static final Handler<CPacketCreatePersistentAuction> HANDLER = new H();

	private static final String GENERATE_ID_FORMAT = "auction_";
	
	final CompoundTag auctionData;
	final String id;
	
	public CPacketCreatePersistentAuction(CompoundTag auctionData, String id) {
		super(TYPE);
		this.auctionData = auctionData;
		this.id = id;
	}

	@Nonnull
	private JsonObject getAuctionJson(@Nonnull String id, @Nonnull HolderLookup.Provider lookup) {
		AuctionTradeData auction = new AuctionTradeData(this.auctionData,lookup);
		JsonObject json = new JsonObject();
		json.addProperty("id", id);
		json = auction.saveToJson(json,lookup);
		return json;
	}

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketCreatePersistentAuction message) {
		buffer.writeNbt(message.auctionData);
		buffer.writeUtf(message.id);
	}
	private static CPacketCreatePersistentAuction decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketCreatePersistentAuction(readNBT(buffer), buffer.readUtf()); }

	private static class H extends Handler<CPacketCreatePersistentAuction>
	{
		protected H() { super(TYPE, easyCodec(CPacketCreatePersistentAuction::encode,CPacketCreatePersistentAuction::decode)); }
		@Override
		protected void handle(@Nonnull CPacketCreatePersistentAuction message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(LCAdminMode.isAdminPlayer(player))
			{
				RegistryAccess lookup = player.registryAccess();
				boolean generateID = message.id.isBlank();
				if(!generateID) {

					JsonObject auctionJson = message.getAuctionJson(message.id,lookup);

					JsonArray persistentAuctions = TraderSaveData.getPersistentTraderJson(TraderSaveData.PERSISTENT_AUCTION_SECTION);
					//Check for auctions with the same id, and replace any entries that match
					for(int i = 0; i < persistentAuctions.size(); ++i)
					{
						JsonObject auctionData = persistentAuctions.get(i).getAsJsonObject();
						if(auctionData.has("id") && auctionData.get("id").getAsString().equals(message.id))
						{
							//Overwrite the existing entry with the same id.
							persistentAuctions.set(i, auctionJson);
							TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_AUCTION_SECTION, persistentAuctions, lookup);
							player.sendSystemMessage(LCText.MESSAGE_PERSISTENT_AUCTION_OVERWRITE.get(message.id));
							return;
						}
					}

					//If no trader found with the id, add to list
					persistentAuctions.add(auctionJson);
					TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_AUCTION_SECTION, persistentAuctions, lookup);
					player.sendSystemMessage(LCText.MESSAGE_PERSISTENT_AUCTION_ADD.get(message.id));
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
							persistentAuctions.add(message.getAuctionJson(genID,lookup));
							TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_AUCTION_SECTION, persistentAuctions, lookup);
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
