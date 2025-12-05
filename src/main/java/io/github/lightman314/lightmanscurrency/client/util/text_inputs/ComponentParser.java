package io.github.lightman314.lightmanscurrency.client.util.text_inputs;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class ComponentParser implements Function<String,Component> {

    public static final ComponentParser INSTANCE = new ComponentParser();

    private ComponentParser() {}

    @Override
    public Component apply(String s) {
        try { return Component.Serializer.fromJsonLenient(s);
        } catch (Exception e) { return s.isBlank() ? null : EasyText.literal(s); }
    }

    public static String write(Component component) { return Component.Serializer.toJson(component); }

}