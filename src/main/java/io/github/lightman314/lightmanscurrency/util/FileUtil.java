package io.github.lightman314.lightmanscurrency.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.google.gson.*;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FileUtil {
	
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
	
	public static ItemStack parseItemStack(JsonObject json) throws JsonSyntaxException, ResourceLocationException {
		String id = GsonHelper.getAsString(json, "ID");
		int count = GsonHelper.getAsInt(json, "Count", 1);
		ItemStack result = new ItemStack(ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(id)), count);
		try {
			if(json.has("Tag"))
			{
				JsonElement tag = json.get("Tag");
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
		} catch(JsonSyntaxException | ResourceLocationException | CommandSyntaxException e) { LightmansCurrency.LogError("Error parsing tag data.", e); }
		return result;
	}
	
	public static void writeStringToFile(File file, String string) throws IOException {

		PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
		
		writer.print(string);
		
		writer.close();
		
	}
	
}
