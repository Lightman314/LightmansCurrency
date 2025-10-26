package io.github.lightman314.lightmanscurrency.api.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ConfigComments {


    public static final ConfigComments EMPTY = new ConfigComments();
    private final List<Object> comments;
    private ConfigComments() { this(new ArrayList<>()); }
    private ConfigComments(List<?> comments) { this.comments = ImmutableList.copyOf(comments); }

    public List<String> getComments()
    {
        List<String> result = new ArrayList<>();
        for(Object entry : this.comments)
            result.addAll(castObject(entry));
        return result;
    }

    private static List<String> castObject(Object object)
    {
        switch (object) {
            case String string -> {
                return ImmutableList.of(string);
            }
            case Supplier<?> supplier -> {
                return castObject(supplier.get());
            }
            case Collection<?> collection -> {
                List<String> result = new ArrayList<>();
                for (Object entry : collection)
                    result.addAll(castObject(entry));
                return result;
            }
            default -> {}
        }
        LightmansCurrency.LogWarning("Could not cast " + object.getClass().getName() + " to a comment list!");
        return ImmutableList.of();
    }

    private static Supplier<List<String>> castSupplier(Supplier<?> supplier) { return () -> castObject(supplier.get()); }

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        private Builder() {}

        private final List<Object> comments = new ArrayList<>();

        public void add(Object... comments) { this.comments.addAll(Lists.newArrayList(comments)); }
        public void clear() { this.comments.clear(); }

        public ConfigComments build() { return new ConfigComments(this.comments); }

    }

}
