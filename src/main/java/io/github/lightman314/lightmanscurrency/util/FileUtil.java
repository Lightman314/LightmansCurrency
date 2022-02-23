package io.github.lightman314.lightmanscurrency.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FileUtil {
	
	public static Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	
	public static JsonObject convertItemStack(ItemStack item) {
		JsonObject json = new JsonObject();
		json.addProperty("id", item.getItem().getRegistryName().toString());
		json.addProperty("Count", item.getCount());
		if(item.hasTag())
		{
			String tag = item.getTag().getAsString();
			json.addProperty("tag", tag);
		}
		return json;
	}
	
	public static ItemStack parseItemStack(JsonObject json) throws Exception{
		String id = json.get("id").getAsString();
		int count = json.get("Count").getAsInt();
		ItemStack result = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)), count);
		try {
			if(json.has("tag"))
			{
				JsonElement tag = json.get("tag");
				if(tag.isJsonPrimitive() && tag.getAsJsonPrimitive().isString())
				{
					//Parse the compound tag
					CompoundTag compound = TagParser.parseTag(tag.getAsString());
					result.setTag(compound);
				}
				else
				{
					CompoundTag compound = TagParser.parseTag(GSON.toJson(tag));
					result.setTag(compound);
				}
			}
		} catch(Exception e) { LightmansCurrency.LogError("Error parsing tag data.", e); }
		return result;
	}
	
	public static void writeStringToFile(File file, String string) throws IOException {
		
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter writer = new PrintWriter(bw);
		
		writer.print(string);
		
		writer.close();
		
	}
	
}
