package io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers;

import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import io.github.lightman314.lightmanscurrency.api.helpers.JsonHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.GsonHelper;

import java.util.function.Function;

public class ComponentParser implements Function<String,Component> {

    public static final ComponentParser INSTANCE = new ComponentParser();

    private ComponentParser() {}

    @Override
    public Component apply(String s) {
        try { return ComponentSerialization.CODEC.decode(JsonOps.INSTANCE,GsonHelper.parse(s)).getOrThrow().getFirst();
        } catch (IllegalStateException | JsonParseException e) { return s.isBlank() ? null : Component.literal(s); }
    }

    public static String write(Component component) {
        try { return JsonHelper.SINGLE_LINE_GSON.toJson(ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE,component).getOrThrow());
        } catch (IllegalStateException ignored) { return component.getString(); }
    }

}