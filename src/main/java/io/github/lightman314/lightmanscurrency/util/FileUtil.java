package io.github.lightman314.lightmanscurrency.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.google.gson.*;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class FileUtil {
	
	public static Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	@Nonnull
	public static JsonElement convertItemStack(@Nonnull ItemStack item, @Nonnull HolderLookup.Provider lookup) {
		return ItemStack.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,lookup),item).getOrThrow();
	}

	@Nonnull
	public static ItemStack parseItemStack(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException {
		return ItemStack.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE,lookup),json).getOrThrow(JsonSyntaxException::new).getFirst();
	}
	
	public static void writeStringToFile(File file, String string) throws IOException {

		PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
		
		writer.print(string);
		
		writer.close();
		
	}
	
}
