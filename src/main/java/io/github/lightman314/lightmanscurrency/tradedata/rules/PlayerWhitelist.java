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

public class PlayerWhitelist extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "whitelist");
	public static final ITradeRuleDeserializer<PlayerWhitelist> DESERIALIZER = new Deserializer();
	
	List<String> whitelistPlayerNames = new ArrayList<>();
	List<UUID> whitelistPlayerIDs = new ArrayList<>(); 
	
	public PlayerWhitelist() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(whitelistPlayerNames.contains(event.getPlayer().getDisplayName().getString()))
			return;
		else if(whitelistPlayerIDs.contains(event.getPlayer().getUniqueID()))
			return;
		event.setCanceled(true);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		//Save player names
		ListNBT playerNameList = new ListNBT();
		for(int i = 0; i < whitelistPlayerNames.size(); i++)
		{
			CompoundNBT thisCompound = new CompoundNBT();
			thisCompound.putString("name", whitelistPlayerNames.get(i));
		}
		compound.put("WhitelistedPlayersNames", playerNameList);
		
		//Save player UUUIDs
		ListNBT playerUUIDList = new ListNBT();
		for(int i = 0; i < whitelistPlayerIDs.size(); i++)
		{
			CompoundNBT thisCompound = new CompoundNBT();
			thisCompound.putUniqueId("UUID", whitelistPlayerIDs.get(i));
		}
		compound.put("WhitelistedPlayersIDs", playerUUIDList);
		
		return compound;
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		
		//Load player names
		if(compound.contains("WhitelistedPlayersNames", Constants.NBT.TAG_LIST))
		{
			this.whitelistPlayerNames.clear();
			ListNBT playerNameList = compound.getList("WhitelistedPlayersNames", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerNameList.size(); i++)
			{
				CompoundNBT thisCompound = playerNameList.getCompound(i);
				if(thisCompound.contains("name", Constants.NBT.TAG_STRING))
					this.whitelistPlayerNames.add(thisCompound.getString("name"));
			}
		}
		//Load player UUIDs
		if(compound.contains("WhitelistedPlayersIDs", Constants.NBT.TAG_LIST))
		{
			this.whitelistPlayerIDs.clear();
			ListNBT playerUUIDList = compound.getList("WhitelistedPlayersIDs", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerUUIDList.size(); i++)
			{
				CompoundNBT thisCompound = playerUUIDList.getCompound(i);
				if(thisCompound.contains("UUID"))
					this.whitelistPlayerIDs.add(thisCompound.getUniqueId("UUID"));
			}
		}
		
	}
	
	@Override
	public int getGUIX() { return 16; }
	
	public static class Deserializer implements ITradeRuleDeserializer<PlayerWhitelist>
	{
		@Override
		public PlayerWhitelist deserialize(CompoundNBT compound) {
			PlayerWhitelist value = new PlayerWhitelist();
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
