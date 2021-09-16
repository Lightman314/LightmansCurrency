package io.github.lightman314.lightmanscurrency.tradedata.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

public class PlayerBlacklist extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "blacklist");
	public static final ITradeRuleDeserializer<PlayerBlacklist> DESERIALIZER = new Deserializer();
	
	List<String> bannedPlayerNames = new ArrayList<>();
	List<UUID> bannedPlayerIDs = new ArrayList<>(); 
	
	public PlayerBlacklist() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(bannedPlayerNames.contains(event.getPlayer().getDisplayName().getString()))
			event.setCanceled(true);
		else if(bannedPlayerIDs.contains(event.getPlayer().getUniqueID()))
			event.setCanceled(true);
		
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		//Save player names
		ListNBT playerNameList = new ListNBT();
		for(int i = 0; i < bannedPlayerNames.size(); i++)
		{
			CompoundNBT thisCompound = new CompoundNBT();
			thisCompound.putString("name", bannedPlayerNames.get(i));
		}
		compound.put("BannedPlayersNames", playerNameList);
		
		//Save player UUUIDs
		ListNBT playerUUIDList = new ListNBT();
		for(int i = 0; i < bannedPlayerIDs.size(); i++)
		{
			CompoundNBT thisCompound = new CompoundNBT();
			thisCompound.putUniqueId("UUID", bannedPlayerIDs.get(i));
		}
		compound.put("BannedPlayersIDs", playerUUIDList);
		
		return compound;
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		
		//Load player names
		if(compound.contains("BannedPlayersNames", Constants.NBT.TAG_LIST))
		{
			this.bannedPlayerNames.clear();
			ListNBT playerNameList = compound.getList("BannedPlayersNames", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerNameList.size(); i++)
			{
				CompoundNBT thisCompound = playerNameList.getCompound(i);
				if(thisCompound.contains("name", Constants.NBT.TAG_STRING))
					this.bannedPlayerNames.add(thisCompound.getString("name"));
			}
		}
		//Load player UUIDs
		if(compound.contains("BannedPlayersIDs", Constants.NBT.TAG_LIST))
		{
			this.bannedPlayerIDs.clear();
			ListNBT playerUUIDList = compound.getList("BannedPlayersNames", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerUUIDList.size(); i++)
			{
				CompoundNBT thisCompound = playerUUIDList.getCompound(i);
				if(thisCompound.contains("UUID"))
					this.bannedPlayerIDs.add(thisCompound.getUniqueId("UUID"));
			}
		}
		
	}
	
	@Override
	public int getGUIX() { return 32; }
	
	public static class Deserializer implements ITradeRuleDeserializer<PlayerBlacklist>
	{
		@Override
		public PlayerBlacklist deserialize(CompoundNBT compound) {
			PlayerBlacklist value = new PlayerBlacklist();
			value.readNBT(compound);
			return value;
		}
	}

	@Override
	public void initTab(TradeRuleScreen screen) {
		
		
	}

	@Override
	public void renderTab(TradeRuleScreen screen, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		
		
	}

	@Override
	public void onTabClose(TradeRuleScreen screen) {
		
		
	}

}
