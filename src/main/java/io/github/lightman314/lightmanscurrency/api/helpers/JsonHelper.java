package io.github.lightman314.lightmanscurrency.api.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonHelper {

    private JsonHelper() {}

    public static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

}