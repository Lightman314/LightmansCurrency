package io.github.lightman314.lightmanscurrency.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

import com.google.gson.*;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.FileUtils;

public class FileUtil {

	public static final JsonParser JSON_PARSER = new JsonParser();
	public static Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	
	public static JsonObject convertItemStack(ItemStack item) {
		JsonObject json = new JsonObject();
		json.addProperty("ID", ForgeRegistries.ITEMS.getKey(item.getItem()).toString());
		json.addProperty("Count", item.getCount());
		if(item.hasTag())
		{
			String tag = item.getTag().getAsString();
			json.addProperty("Tag", tag);
		}
		return json;
	}
	
	public static ItemStack parseItemStack(JsonObject json) {
		String id = json.get("ID").getAsString();
		int count = json.get("Count").getAsInt();
		ItemStack result = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)), count);
		try {
			if(json.has("Tag"))
			{
				JsonElement tag = json.get("Tag");
				if(tag.isJsonPrimitive() && tag.getAsJsonPrimitive().isString())
				{
					//Parse the compound tag
					CompoundNBT compound = JsonToNBT.parseTag(tag.getAsString());
					result.setTag(compound);
				}
				else
				{
					CompoundNBT compound = JsonToNBT.parseTag(GSON.toJson(tag));
					result.setTag(compound);
				}
			}
		} catch(Exception e) { LightmansCurrency.LogError("Error parsing tag data.", e); }
		return result;
	}

	public static String readString(File file) throws IOException {
		return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	}

	public static void writeStringToFile(File file, String string) throws IOException {

		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		PrintWriter writer = new PrintWriter(osw);
		
		writer.print(string);
		
		writer.close();
		
	}
	
}