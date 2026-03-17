package io.github.lightman314.lightmanscurrency.api.client.widgets.text_inputs;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class ResourceParser implements Function<String,ResourceLocation> {

    public static final ResourceParser DEFAULT = new ResourceParser(false);
    public static final ResourceParser REQUIRE_NAMESPACE = new ResourceParser(true);

    public static boolean isResourceOrEmpty(String s)
    {
        if(s.isEmpty())
            return true;
        try {
            ResourceLocation.parse(s);
            return true;
        } catch (ResourceLocationException ignored) { return false; }
    }

    private final boolean requireNamespace;
    public ResourceParser(boolean requireNamespace) { this.requireNamespace = requireNamespace; }

    @Override
    public ResourceLocation apply(String s) {
        if(this.requireNamespace && !s.contains(":"))
            return null;
        try { return ResourceLocation.parse(s);
        } catch (ResourceLocationException ignored) { return null; }
    }

}
