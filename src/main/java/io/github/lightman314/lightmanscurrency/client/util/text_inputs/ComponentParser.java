package io.github.lightman314.lightmanscurrency.client.util.text_inputs;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class ComponentParser implements Function<String,Component> {

    public static final ComponentParser INSTANCE = new ComponentParser();

    private ComponentParser() {}

    private static HolderLookup.Provider getLookup()
    {
        HolderLookup.Provider lookup = LookupHelper.getRegistryAccess();
        if(lookup == null)
            return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        return lookup;
    }

    @Override
    public Component apply(String s) {
        try { return Component.Serializer.fromJsonLenient(s,getLookup());
        } catch (Exception e) { return s.isBlank() ? null : EasyText.literal(s); }
    }

    public static String write(Component component) { return Component.Serializer.toJson(component,getLookup()); }

}
