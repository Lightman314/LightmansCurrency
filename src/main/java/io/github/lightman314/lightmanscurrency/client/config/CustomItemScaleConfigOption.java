package io.github.lightman314.lightmanscurrency.client.config;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.options.ListLikeOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CustomItemScaleConfigOption extends ListLikeOption<CustomItemScaleData> {

    public static final ConfigParser<CustomItemScaleData> PARSER = ListOption.makeParser(StringOption.PARSER).map(CustomItemScaleConfigOption::read,CustomItemScaleConfigOption::write);

    protected CustomItemScaleConfigOption(@Nonnull Supplier<CustomItemScaleData> defaultValue) {
        super(defaultValue);
    }

    public static CustomItemScaleConfigOption create() { return create(CustomItemScaleData.EMPTY); }
    public static CustomItemScaleConfigOption create(CustomItemScaleData defaultValue) { return create(() -> defaultValue); }
    public static CustomItemScaleConfigOption create(Supplier<CustomItemScaleData> defaultValue) { return new CustomItemScaleConfigOption(defaultValue); }

    @Nonnull
    @Override
    protected List<String> bonusComments() {
        return Lists.newArrayList(
                "Should be formatted as \"namespace:item_id;SCALE\" where SCALE is decimal number between 0.0 and 1.0 (exclusive)",
                "Scale of 1.0 will be drawn at full size, Scale of 0.0 will not draw at all",
                "Can define an item tag instead of an item by formatting as \"#namespace:item_tag;SCALE\" instead");
    }

    @Nonnull
    @Override
    public Pair<Boolean, ConfigParsingException> editList(String value, int index, boolean isEdit) {
        if(index < 0 && isEdit)
        {
            //Add value
            try {
                Pair<CustomItemScaleData.ItemTest,Float> newValue = CustomItemScaleData.parse(cleanWhitespace(value));
                List<Pair<CustomItemScaleData.ItemTest,Float>> currentValue = this.getCurrentValue().getRawData();
                currentValue.add(newValue);
                this.set(new CustomItemScaleData(currentValue));
                return Pair.of(true,null);
            } catch (ConfigParsingException e) { return Pair.of(false,e); }
        }
        if(index >= 0)
        {
            List<Pair<CustomItemScaleData.ItemTest,Float>> currentValue = this.getCurrentValue().getRawData();
            if(index >= currentValue.size())
                return Pair.of(false, new ConfigParsingException("Invalid index. Maximum is " + (currentValue.size() - 1) + "!"));
            if(isEdit)
            {
                //Replace action
                try {
                    Pair<CustomItemScaleData.ItemTest,Float> newValue = CustomItemScaleData.parse(cleanWhitespace(value));
                    currentValue.set(index, newValue);
                    this.set(new CustomItemScaleData(currentValue));
                    return Pair.of(true,null);
                } catch (ConfigParsingException e) { return Pair.of(false, e); }
            }
            else
            {
                //Remove action
                currentValue.remove(index);
                this.set(new CustomItemScaleData(currentValue));
                return Pair.of(true,null);
            }
        }
        return null;
    }

    @Override
    public int getSize() { return this.getCurrentValue().getRawData().size(); }

    @Nonnull
    @Override
    protected ConfigParser<CustomItemScaleData> getParser() { return PARSER; }

    private static CustomItemScaleData read(List<String> list)
    {
        List<Pair<CustomItemScaleData.ItemTest,Float>> results = new ArrayList<>();
        for(int s = 0; s < list.size(); ++s)
        {
            try { results.add(CustomItemScaleData.parse(list.get(s)));
            }catch (ConfigParsingException e) { LightmansCurrency.LogWarning("Failed to parse List Config entry #" + (s + 1), e); }
        }
        return new CustomItemScaleData(results);
    }

    private static List<String> write(CustomItemScaleData value)
    {
        List<String> list = new ArrayList<>();
        for(var v : value.getRawData())
            list.add(CustomItemScaleData.write(v));
        return list;
    }



}