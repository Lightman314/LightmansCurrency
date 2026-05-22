package io.github.lightman314.lightmanscurrency.api.misc.icons.types;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconType;
import io.github.lightman314.lightmanscurrency.util.OldDataHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class ItemIcon extends IconData
{

    public static final IconType<ItemIcon> TYPE = new Type();

    private static final MapCodec<ItemIcon> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ItemStack.CODEC.fieldOf("item").forGetter(i -> i.iconStack),
            Codec.STRING.optionalFieldOf("override").forGetter(i -> i.countTextOverride)
    ).apply(builder,ItemIcon::new));

    private static final StreamCodec<RegistryFriendlyByteBuf,ItemIcon> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,i -> i.iconStack,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),i -> i.countTextOverride,
            ItemIcon::new);

    public final ItemStack iconStack;
    public final Optional<String> countTextOverride;
    private ItemIcon(ItemStack iconStack,Optional<String> countTextOverride) {
        this.iconStack = iconStack;
        this.countTextOverride = Objects.requireNonNull(countTextOverride);
    }

    public static IconData ofItem(Supplier<? extends ItemLike> item) { return ofItem(item.get()); }
    public static IconData ofItem(Supplier<? extends ItemLike> item, @Nullable String countTextOverride) { return ofItem(item.get(), countTextOverride); }
    public static IconData ofItem(ItemLike item) { return ofItem(new ItemStack(item)); }
    public static IconData ofItem(ItemLike item, @Nullable String countTextOverride) { return ofItem(new ItemStack(item),countTextOverride); }
    public static IconData ofItem(ItemStack item) { return ofItem(item,null); }
    public static IconData ofItem(ItemStack item, @Nullable String countTextOverride) { return new ItemIcon(item,Optional.ofNullable(countTextOverride)); }

    @Override
    public IconType<?> getType() { return TYPE; }

    @SuppressWarnings("deprecation")
    private static ItemIcon loadItem(CompoundTag tag, HolderLookup.Provider lookup)
    {
        ItemStack stack = OldDataHelper.loadItem(tag.getCompound("Item"),lookup);
        String countText = null;
        if(tag.contains("Text"))
            countText = tag.getString("Text");
        return new ItemIcon(stack,Optional.ofNullable(countText));
    }

    private static ItemIcon parseItem(JsonObject json, HolderLookup.Provider lookup)
    {
        ItemStack stack = DataContext.createJson(lookup).readOrThrow(GsonHelper.getAsJsonObject(json,"Item"),ItemStack.CODEC);
        String countText = GsonHelper.getAsString(json,"Text",null);
        return new ItemIcon(stack,Optional.ofNullable(countText));
    }

    private static class Type extends IconType<ItemIcon>
    {
        @Override
        public ItemIcon loadOld(CompoundTag tag, HolderLookup.Provider lookup) { return loadItem(tag,lookup); }
        @Override
        public ItemIcon parseOld(JsonObject json, HolderLookup.Provider lookup) { return parseItem(json,lookup); }
        @Override
        public MapCodec<ItemIcon> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ItemIcon> streamCodec() { return STREAM_CODEC; }
    }

}
