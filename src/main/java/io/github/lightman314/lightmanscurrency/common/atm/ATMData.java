package io.github.lightman314.lightmanscurrency.common.atm;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent.Context;

@Mod.EventBusSubscriber
public class ATMData {
	
	public static final String ATM_FILE_LOCATION = "config/lightmanscurrency/ATMData.json";
	
	private final List<ATMConversionButtonData> conversionButtons;
	public final List<ATMConversionButtonData> getConversionButtons() { return ImmutableList.copyOf(this.conversionButtons); }
	
	private ATMData(JsonObject jsonData) throws Exception {
		
		//LightmansCurrency.LogInfo("Loading ATM Data from json:\n" + FileUtil.GSON.toJson(jsonData));
		
		this.conversionButtons = new ArrayList<>();
		if(jsonData.has("ConversionButtons"))
		{
			JsonArray conversionButtonDataList = jsonData.getAsJsonArray("ConversionButtons");
			for(int i = 0; i < conversionButtonDataList.size(); ++i)
			{
				try {
					this.conversionButtons.add(ATMConversionButtonData.parse(conversionButtonDataList.get(i).getAsJsonObject()));
				} catch(Exception e) { LightmansCurrency.LogError("Error parsing Conversion Button #" + String.valueOf(i + 1) + ".", e); }
			}
		}
		else
		{
			LightmansCurrency.LogWarning("ATM Data has no 'ConversionButtons' list entry. Conversion tab will have no buttons.");
		}
		
	}
	
	private ATMData(List<ATMConversionButtonData> conversionButtons) {
		this.conversionButtons = Lists.newArrayList(conversionButtons);
	}
	
	public JsonObject save() {
		JsonObject data = new JsonObject();
		
		JsonArray conversionButtonDataList = new JsonArray();
		for(int i = 0; i < this.conversionButtons.size(); ++i)
			conversionButtonDataList.add(this.conversionButtons.get(i).save());
		data.add("ConversionButtons", conversionButtonDataList);
		
		return data;
	}
	
	private static ATMData loadedData = null;
	public static ATMData get() {
		if(loadedData == null)
			reloadATMData();
		return loadedData;
	}
	
	public static void encode(ATMData data, FriendlyByteBuf buffer) {
		JsonObject json = data.save();
		String jsonString = FileUtil.GSON.toJson(json);
		int stringSize = jsonString.length();
		buffer.writeInt(stringSize);
		buffer.writeUtf(jsonString, stringSize);
	}
	
	public static ATMData decode(FriendlyByteBuf buffer) {
		try {
			LightmansCurrency.LogInfo("Decoding atm data packet:");
			int stringSize = buffer.readInt();
			String jsonString = buffer.readUtf(stringSize);
			JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
			return new ATMData(json);
		} catch(Throwable t) { LightmansCurrency.LogError("Error decoding ATMData.", t); return generateDefault(); }
	}
	
	public static void handle(ATMData data, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			LightmansCurrency.LogInfo("Received atm data packet from server.");
			loadedData = data;
		});
		source.get().setPacketHandled(true);
	}
	
	private static ATMData generateDefault() {
		return new ATMData(ATMConversionButtonData.generateDefault());
	}
	
	public static void reloadATMData() {
		LightmansCurrency.LogInfo("Reloading ATM Data");
		File file = new File(ATM_FILE_LOCATION);
		if(!file.exists())
		{
			createATMDataFile(file);
		}
		try { 
			JsonObject fileData = GsonHelper.parse(Files.readString(file.toPath()));
			loadedData = new ATMData(fileData);
		} catch(Throwable e) {
			LightmansCurrency.LogError("Error loading ATM Data. Using default values for now.", e);
			loadedData = generateDefault();
		}
		LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), loadedData);
	}
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		//Send the ATM data to the player
		LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(event.getEntity()), get());
	}
	
	private static void createATMDataFile(File file) {
		File dir = new File(file.getParent());
		if(!dir.exists())
			dir.mkdirs();
		if(dir.exists())
		{
			try {
				
				ATMData defaultData = generateDefault();
				
				file.createNewFile();
				
				FileUtil.writeStringToFile(file, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(defaultData.save()));
				
				LightmansCurrency.LogInfo("ATMData.json does not exist. Creating a fresh copy.");
				
			} catch(Throwable e) { LightmansCurrency.LogError("Error attempting to create 'ATMData.json' file.", e); }
		}
	}
	
	
}
