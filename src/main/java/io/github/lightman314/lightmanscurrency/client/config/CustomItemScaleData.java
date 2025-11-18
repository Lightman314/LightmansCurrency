package io.github.lightman314.lightmanscurrency.client.config;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CustomItemScaleData {

    private final List<Pair<ItemTest,Float>> data;
    public List<Pair<ItemTest,Float>> getRawData() { return new ArrayList<>(this.data); }

    public static final CustomItemScaleData EMPTY = new CustomItemScaleData(ImmutableList.of());

    public CustomItemScaleData(List<Pair<ItemTest,Float>> data) { this.data = ImmutableList.copyOf(data); }

    public float getCustomScale(ItemStack item)
    {
        for(var pair : this.data)
        {
            if(pair.getFirst().test(item))
                return pair.getSecond();
        }
        return 1f;
    }

    public static Pair<ItemTest,Float> parse(String string) throws ConfigParsingException {
        String[] split = string.split(";");
        if(split.length < 2)
            throw new ConfigParsingException("Missing ';' in '" + string + "'");
        else if(split.length > 2)
            throw new ConfigParsingException("Unexpected ';' in '" + string + "'");
        ItemTest test = ItemTest.parse(split[0]);
        try {
            float scale = Float.parseFloat(split[1]);
            if(scale <= 0f)
                throw new ConfigParsingException(split[1] + " cannot be less than or equal to 0");
            return Pair.of(test,scale);
        }catch (NumberFormatException e) { throw new ConfigParsingException(split[1] + " is not a valid scale"); }
    }

    public static String write(Pair<ItemTest,Float> value) { return value.getFirst().toString() + ";" + value.getSecond(); }

}
