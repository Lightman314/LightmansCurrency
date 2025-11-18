package io.github.lightman314.lightmanscurrency.client.config;

import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class ItemTest implements Predicate<ItemStack> {

    @Override
    public abstract String toString();

    @Nullable
    public static ItemTest tryParseTest(String string)
    {
        try { return parse(string);
        } catch (ConfigParsingException ignored) { return null; }
    }

    public static ItemTest parse(String string) throws ConfigParsingException
    {
        try {
            if(string.startsWith("#"))
                return create(TagKey.create(Registries.ITEM, VersionUtil.parseResource(string.substring(1))));
            else
                return create(BuiltInRegistries.ITEM.get(VersionUtil.parseResource(string)));
        } catch (ResourceLocationException e) { throw new ConfigParsingException(string + " is not a valid Resource Location"); }
    }

    public static ItemTest create(ItemLike item) { return new MatchTest(item.asItem()); }
    public static ItemTest create(Supplier<? extends ItemLike> item) { return new MatchTest(item.get().asItem()); }
    public static ItemTest create(TagKey<Item> itemTag) { return new TagTest(itemTag); }

    private static class MatchTest extends ItemTest
    {
        private final Item item;
        private MatchTest(Item item) { this.item = item; }
        @Override
        public String toString() { return BuiltInRegistries.ITEM.getKey(this.item).toString(); }
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
