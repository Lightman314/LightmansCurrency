package io.github.lightman314.lightmanscurrency.client.config;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

    public static ItemTest create(ItemLike item) { return new MatchTest(item.asItem()); }
    public static ItemTest create(Supplier<? extends ItemLike> item) { return new MatchTest(item.get().asItem()); }
    public static ItemTest create(TagKey<Item> itemTag) { return new TagTest(itemTag); }

    @Nullable
    public static ItemTest tryParseTest(String string)
    {
        try {
            if(string.startsWith("#"))
                return new TagTest(TagKey.create(Registries.ITEM,VersionUtil.parseResource(string.substring(1))));
            else
                return new MatchTest(ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(string)));
        } catch (ResourceLocationException ignored) { return null; }
    }

    public static Pair<ItemTest,Float> parse(String string) throws ConfigParsingException {
        String[] split = string.split(";");
        if(split.length < 2)
            throw new ConfigParsingException("Missing ';' in '" + string + "'");
        else if(split.length > 2)
            throw new ConfigParsingException("Unexpected ';' in '" + string + "'");
        ItemTest test;
        try {
            if(split[0].startsWith("#"))
                test = new TagTest(TagKey.create(Registries.ITEM, VersionUtil.parseResource(split[0].substring(1))));
            else
                test = new MatchTest(ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(split[0])));
        } catch (ResourceLocationException e) { throw new ConfigParsingException(split[0] + " is not a valid Resource Location"); }
        try {
            float scale = Float.parseFloat(split[1]);
            if(scale <= 0f)
                throw new ConfigParsingException(split[1] + " cannot be less than or equal to 0");
            return Pair.of(test,scale);
        }catch (NumberFormatException e) { throw new ConfigParsingException(split[1] + " is not a valid scale"); }
    }

    public static String write(Pair<ItemTest,Float> value) { return value.getFirst().toString() + ";" + value.getSecond(); }

    public static abstract class ItemTest implements Predicate<ItemStack>
    {
        @Override
        public abstract String toString();
    }

    private static class MatchTest extends ItemTest
    {
        private final Item item;
        private MatchTest(Item item) { this.item = item; }
        @Override
        public String toString() { return ForgeRegistries.ITEMS.getKey(this.item).toString(); }
        @Override
        public boolean test(ItemStack stack) { return stack.getItem() == this.item; }
    }

    private static class TagTest extends ItemTest
    {
        private final TagKey<Item> tag;
        private TagTest(TagKey<Item> tag) { this.tag = tag; }
        @Override
        public String toString() { return "#" + this.tag.location(); }
        @Override
        public boolean test(ItemStack stack) { return InventoryUtil.ItemHasTag(stack,this.tag); }
    }

}