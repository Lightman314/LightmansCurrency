package io.github.lightman314.lightmanscurrency.common.notifications.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import io.github.lightman314.lightmanscurrency.util.OldDataHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ItemData
{

    public static final ItemData EMPTY = new ItemData(ItemStack.EMPTY);

    public static final Codec<ItemData> CODEC = Codec.withAlternative(
            //Desired Codec
            RecordCodecBuilder.create(builder -> builder.group(
                    ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(d -> d.stack),
                    ComponentSerialization.CODEC.optionalFieldOf("deprecatedName").forGetter(d -> d.deprecatedName),
                    Codec.STRING.fieldOf("customName").forGetter(d -> d.customName)
            ).apply(builder,ItemData::new)),
            //Fallback Codec for old data
            CodecHelper.oldValueLoader(ItemData::loadOldData,"Item Data"));

    public static final Codec<List<ItemData>> LIST_CODEC = CODEC.listOf();

    public static final StreamCodec<RegistryFriendlyByteBuf,ItemData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC,d -> d.stack,
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC),d -> d.deprecatedName,
            ByteBufCodecs.STRING_UTF8,d -> d.customName,
            ItemData::new);
    public static final StreamCodec<RegistryFriendlyByteBuf,List<ItemData>> STREAM_CODEC_LIST = STREAM_CODEC.apply(ByteBufCodecs.list());


    private final ItemStack stack;
    private final Optional<Component> deprecatedName;
    private final String customName;
    public ItemData(ItemStack stack) { this(stack, Optional.empty(), ""); }
    public ItemData(ItemStack stack, String customName) { this(stack, Optional.empty(), customName); }
    private ItemData(ItemStack stack, @Nullable Optional<Component> deprecatedName, String customName)
    {
        this.stack = stack;
        this.deprecatedName = deprecatedName;
        this.customName = customName;
    }
    private ItemData(Component deprecatedName, int count)
    {
        this.stack = new ItemStack(Items.BARRIER,count);
        this.deprecatedName = Optional.of(deprecatedName);
        this.customName = "";
    }

    public Component getName()
    {
        if(this.deprecatedName.isPresent())
            return this.deprecatedName.get();
        return this.customName.isEmpty() ? EasyText.empty().append(this.stack.getHoverName()) : EasyText.literal(this.customName);
    }

    public MutableComponent format() { return LCText.NOTIFICATION_ITEM_FORMAT.get(this.stack.getCount(), this.getName()); }
    public MutableComponent formatWith(ItemData other) { return LCText.GUI_AND.get(this.format(), other.format()); }
    public MutableComponent formatWith(MutableComponent other) { return LCText.GUI_AND.get(this.format(), other); }

    public static MutableComponent format(ItemData d1, ItemData d2)
    {
        if(d1.stack.isEmpty() && d2.stack.isEmpty())
            return EasyText.literal("ERROR");
        if(d1.stack.isEmpty())
            return d2.format();
        if(d2.stack.isEmpty())
            return d1.format();
        return d1.formatWith(d2);
    }

    public CompoundTag save(HolderLookup.Provider lookup) { return (CompoundTag)CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,lookup),this).getOrThrow(); }

    public static ItemData load(CompoundTag tag, HolderLookup.Provider lookup) { return CODEC.decode(RegistryOps.create(NbtOps.INSTANCE,lookup),tag).getOrThrow().getFirst(); }

    @Deprecated
    private static ItemData loadOldData(CompoundTag tag, HolderLookup.Provider lookup)
    {
        if(tag.contains("Empty"))
            return new ItemData(ItemStack.EMPTY,"");
        if(tag.contains("Name"))
        {
            MutableComponent deprecatedName = Component.Serializer.fromJson(tag.getString("Name"),lookup);
            int count = tag.getInt("Count");
            return new ItemData(deprecatedName,count);
        }
        ItemStack stack = OldDataHelper.loadItem(tag.getCompound("Stack"),lookup);
        String customName = tag.getString("CustomName");
        Component deprecatedName = null;
        if(tag.contains("DeprecatedName"))
            deprecatedName = Component.Serializer.fromJson(tag.getString("DeprecatedName"),lookup);
        return new ItemData(stack,Optional.ofNullable(deprecatedName),customName);
    }

    public boolean matches(ItemData other)
    {
        //Cannot compare text components server-side apparently...
        if(this.deprecatedName.isPresent() || other.deprecatedName.isPresent())
            return false;
        return this.customName.equals(other.customName) && ItemHandlerUtil.isExactMatch(this.stack,other.stack);
    }

    public static Component getItemNames(List<ItemData> items) {
        MutableComponent result = null;
        for (ItemData item : items) {
            if (result != null)
                result = item.formatWith(result);
            else
                result = item.format();
        }
        return result == null ? Component.literal("ERROR") : result;
    }

}
