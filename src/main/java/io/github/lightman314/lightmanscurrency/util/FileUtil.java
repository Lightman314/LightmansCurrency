package io.github.lightman314.lightmanscurrency.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.google.gson.*;

import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStack;

public class FileUtil {
	
	public static Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	
	public static JsonElement convertItemStack(ItemStack item) {
		return ItemStack.CODEC.encodeStart(JsonOps.INSTANCE,item).getOrThrow();
	}
	
	public static ItemStack parseItemStack(JsonObject json) throws JsonSyntaxException {
		return ItemStack.CODEC.decode(JsonOps.INSTANCE,json).getOrThrow(JsonSyntaxException::new).getFirst();
	}
	
	public static void writeStringToFile(File file, String string) throws IOException {

		PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
		
		writer.print(string);
		
		writer.close();
		
	}
	
}
