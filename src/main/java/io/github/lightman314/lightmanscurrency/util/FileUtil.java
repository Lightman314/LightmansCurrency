package io.github.lightman314.lightmanscurrency.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.google.gson.*;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class FileUtil {
	
	public static Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    /**
     * @deprecated Use Codecs instead!
     */
    @Deprecated(forRemoval = true)
	public static JsonElement convertItemStack(ItemStack item, HolderLookup.Provider lookup) {
		JsonElement json = ItemStack.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,lookup),item.copyWithCount(1)).getOrThrow();
		json.getAsJsonObject().addProperty("count",item.getCount());
		return json;
	}

    @Deprecated(forRemoval = true)
	public static ItemStack parseItemStack(JsonObject json, HolderLookup.Provider lookup) throws JsonSyntaxException {
		int count = GsonHelper.getAsInt(json,"count",1);
		if(count > 99)
		{
			json.addProperty("count",1);
			ItemStack result = ItemStack.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE,lookup),json).getOrThrow(JsonSyntaxException::new).getFirst();
			result.setCount(count);
			return result;
		}
		return ItemStack.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE,lookup),json).getOrThrow(JsonSyntaxException::new).getFirst();
	}
	
	public static void writeStringToFile(File file, String string) throws IOException {

		PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);

		writer.print(string);

		writer.close();
		
	}

}
