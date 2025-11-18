package io.github.lightman314.lightmanscurrency.client.util.text_inputs;

import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Predicate;

public class ResourceParser implements Function<String,ResourceLocation> {

    public static final ResourceParser DEFAULT = new ResourceParser(false);
    public static final ResourceParser REQUIRE_NAMESPACE = new ResourceParser(true);

    public static boolean isResourceOrEmpty(String s)
    {
        if(s.isEmpty())
            return true;
        try {
            VersionUtil.parseResource(s);
            return true;
        } catch (ResourceLocationException ignored) { return false; }
    };

    private final boolean requireNamespace;
    public ResourceParser(boolean requireNamespace) { this.requireNamespace = requireNamespace; }

    @Override
    public ResourceLocation apply(String s) {
        if(this.requireNamespace && !s.contains(":"))
            return null;
        try { return VersionUtil.parseResource(s);
        } catch (ResourceLocationException ignored) { return null; }
    }

}
