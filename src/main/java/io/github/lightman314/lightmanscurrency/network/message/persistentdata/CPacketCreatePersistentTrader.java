package io.github.lightman314.lightmanscurrency.network.message.persistentdata;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketCreatePersistentTrader extends ClientToServerPacket {

	private static final Type<CPacketCreatePersistentTrader> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_persistent_make_trader"));
	public static final Handler<CPacketCreatePersistentTrader> HANDLER = new H();

	private static final String GENERATE_ID_FORMAT = "trader_";
	
	final long traderID;
	final String id;
	final String owner;
	
	public CPacketCreatePersistentTrader(long traderID, String id, String owner) {
		super(TYPE);
		this.traderID = traderID;
		this.id = id;
		this.owner = owner.isBlank() ? "Minecraft" : owner;
	}

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketCreatePersistentTrader message) {
		buffer.writeLong(message.traderID);
		buffer.writeUtf(message.id);
		buffer.writeUtf(message.owner);
	}
	private static CPacketCreatePersistentTrader decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketCreatePersistentTrader(buffer.readLong(), buffer.readUtf(),buffer.readUtf()); }

	private static class H extends Handler<CPacketCreatePersistentTrader>
	{
		protected H() { super(TYPE, easyCodec(CPacketCreatePersistentTrader::encode,CPacketCreatePersistentTrader::decode)); }
		@Override
		protected void handle(@Nonnull CPacketCreatePersistentTrader message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(LCAdminMode.isAdminPlayer(player))
			{
				TraderData trader = TraderSaveData.GetTrader(false, message.traderID);
				if(trader != null && trader.canMakePersistent())
				{

					RegistryAccess lookup = player.registryAccess();

					boolean generateID = message.id.isBlank();

					if(!generateID)
					{
						try {
							JsonObject traderJson = trader.saveToJson(message.id, message.owner,lookup);

							JsonArray persistentTraders = TraderSaveData.getPersistentTraderJson(TraderSaveData.PERSISTENT_TRADER_SECTION);
							//Check for traders with the same id, and replace any entries that match
							for(int i = 0; i < persistentTraders.size(); ++i)
							{
								JsonObject traderData = persistentTraders.get(i).getAsJsonObject();
								if(traderData.has("ID") && traderData.get("ID").getAsString().equals(message.id) || traderData.has("id") && traderData.get("id").getAsString().equals(message.id))
								{
									//Overwrite the existing entry with the same id.
									persistentTraders.set(i, traderJson);
									TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders,lookup);
									player.sendSystemMessage(LCText.MESSAGE_PERSISTENT_TRADER_OVERWRITE.get(message.id));
									return;
								}
							}

							//If no trader found with the id, add to list
							persistentTraders.add(traderJson);
							TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders,lookup);
							player.sendSystemMessage(LCText.MESSAGE_PERSISTENT_TRADER_ADD.get(message.id));
						} catch (Throwable t) { LightmansCurrency.LogError("Error occurred while creating a persistent trader!", t); }
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
								if(traderData.has("ID"))
									knownIDs.add(traderData.get("ID").getAsString());
							}

							//Check trader_1 -> trader_2147483646 to find an available id
							for(int i = 1; i < Integer.MAX_VALUE; ++i)
							{
								String genID = GENERATE_ID_FORMAT + i;
								if(knownIDs.stream().noneMatch(id -> id.equals(genID)))
								{
									persistentTraders.add(trader.saveToJson(genID, message.owner, lookup));
									TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders, lookup);
									player.sendSystemMessage(LCText.MESSAGE_PERSISTENT_TRADER_ADD.get(genID));
									return;
								}
							}
							LightmansCurrency.LogError("Could not generate ID, as all trader_# ID's are somehow spoken for.");
						} catch(Throwable t) { LightmansCurrency.LogError("Error occurred while creating a persistent trader!", t); }
					}
				}
			}
			else
				player.sendSystemMessage(LCText.MESSAGE_PERSISTENT_TRADER_FAIL.get());
		}
	}
	
	
}
