package io.github.lightman314.lightmanscurrency.network.message.persistentdata;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketCreatePersistentTrader extends ClientToServerPacket {

	public static final Handler<CPacketCreatePersistentTrader> HANDLER = new H();

	private static final String GENERATE_ID_FORMAT = "trader_";
	
	final long traderID;
	final String id;
	final String owner;
	
	public CPacketCreatePersistentTrader(long traderID, String id, String owner) {
		this.traderID = traderID;
		this.id = id;
		this.owner = owner.isBlank() ? "Minecraft" : owner;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeLong(this.traderID);
		buffer.writeUtf(this.id);
		buffer.writeUtf(this.owner);
	}

	private static class H extends Handler<CPacketCreatePersistentTrader>
	{
		@Nonnull
		@Override
		public CPacketCreatePersistentTrader decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketCreatePersistentTrader(buffer.readLong(), buffer.readUtf(), buffer.readUtf()); }
		@Override
		protected void handle(@Nonnull CPacketCreatePersistentTrader message, @Nullable ServerPlayer sender) {
			if(LCAdminMode.isAdminPlayer(sender))
			{
				TraderData trader = TraderSaveData.GetTrader(false, message.traderID);
				if(trader != null && trader.canMakePersistent())
				{

					boolean generateID = message.id.isBlank();

					if(!generateID)
					{
						try {
							JsonObject traderJson = trader.saveToJson(message.id, message.owner);

							JsonArray persistentTraders = TraderSaveData.getPersistentTraderJson(TraderSaveData.PERSISTENT_TRADER_SECTION);
							//Check for traders with the same id, and replace any entries that match
							for(int i = 0; i < persistentTraders.size(); ++i)
							{
								JsonObject traderData = persistentTraders.get(i).getAsJsonObject();
								if(traderData.has("ID") && traderData.get("ID").getAsString().equals(message.id) || traderData.has("id") && traderData.get("id").getAsString().equals(message.id))
								{
									//Overwrite the existing entry with the same id.
									persistentTraders.set(i, traderJson);
									TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders);
									sender.sendSystemMessage(EasyText.translatable("lightmanscurrency.message.persistent.trader.overwrite", message.id));
									return;
								}
							}

							//If no trader found with the id, add to list
							persistentTraders.add(traderJson);
							TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders);
							sender.sendSystemMessage(EasyText.translatable("lightmanscurrency.message.persistent.trader.add", message.id));
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
								if(traderData.has("ID"))
									knownIDs.add(traderData.get("ID").getAsString());
							}

							//Check trader_1 -> trader_2147483646 to find an available id
							for(int i = 1; i < Integer.MAX_VALUE; ++i)
							{
								String genID = GENERATE_ID_FORMAT + i;
								if(knownIDs.stream().noneMatch(id -> id.equals(genID)))
								{
									persistentTraders.add(trader.saveToJson(genID, message.owner));
									TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders);
									sender.sendSystemMessage(EasyText.translatable("lightmanscurrency.message.persistent.trader.add", genID));
									return;
								}
							}
							LightmansCurrency.LogError("Could not generate ID, as all trader_# ID's are somehow spoken for.");
						} catch(Throwable t) { t.printStackTrace(); }
					}
				}
			}
			else if(sender != null)
				sender.sendSystemMessage(EasyText.translatable("lightmanscurrency.message.persistent.trader.fail"));
		}
	}
	
	
}
