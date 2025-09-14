package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public record SortTypeKey(ResourceLocation id, boolean inverted) {

    public static Supplier<String> getExampleListSupplier(boolean quotes) { return () -> getExampleList(quotes); }
    public static String getExampleList(boolean quotes)
    {
        StringBuilder builder = new StringBuilder();
        for(SortTypeKey key : TraderAPI.API.GetAllSortTypeKeys())
        {
            if(!builder.isEmpty())
                builder.append(",");
            if(quotes)
                builder.append('"');
            builder.append(key);
            if(quotes)
                builder.append('"');
        }
        return builder.toString();
    }

    @Nonnull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if(this.inverted)
            builder.append('!');
        return builder.append(this.id).toString();
    }

    public static SortTypeKey parse(String input)
    {
        boolean inverted = false;
        if(input.startsWith("!"))
        {
            inverted = true;
            input = input.substring(1);
        }
        return new SortTypeKey(VersionUtil.parseResource(input),inverted);
    }

}