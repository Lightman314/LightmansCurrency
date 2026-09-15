package io.github.lightman314.lightmanscurrency.api.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.util.GsonHelper;

public final class JsonHelper {

    private JsonHelper() {}

    public static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Gson SINGLE_LINE_GSON = new GsonBuilder().disableHtmlEscaping().create();

    public static JsonElement parse(String string) { return GsonHelper.parse(string);
    }

}