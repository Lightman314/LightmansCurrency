package io.github.lightman314.lightmanscurrency.features.trader.item;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.features.trader.item.requirements.ItemMatchRequirement;
import io.github.lightman314.lightmanscurrency.features.trader.item.requirements.ItemRequirement;
import io.github.lightman314.lightmanscurrency.features.trader.item.requirements.StackMatchRequirement;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class TradeItem {

    public static final Codec<TradeItem> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemResource.OPTIONAL_CODEC.fieldOf("item").forGetter(r -> r.item),
            Codec.intRange(0,Integer.MAX_VALUE).fieldOf("count").forGetter(TradeItem::getCount),
            Codec.STRING.optionalFieldOf("name").forGetter(TradeItem::getOptionalNameChange),
            Codec.BOOL.fieldOf("strict").forGetter(TradeItem::isStrict)
    ).apply(builder, TradeItem::new));
    public static final Codec<TradeItem> LENIENT_CODEC = Codec.withAlternative(CODEC,ItemStack.CODEC.xmap(stack -> {
        TradeItem ti = TradeItem.create();
        ti.setStack(stack);
        return ti;
    },TradeItem::getStack));
    public static final Codec<List<TradeItem>> LENIENT_LIST_CODEC = CodecHelper.lenientListCodec(LENIENT_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, TradeItem> STREAM_CODEC = StreamCodec.composite(
            ItemResource.STREAM_CODEC,r -> r.item,
            ByteBufCodecs.INT,TradeItem::getCount,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),TradeItem::getOptionalNameChange,
            ByteBufCodecs.BOOL,TradeItem::isStrict,
            TradeItem::new);

    public static final int MAX_NAME_LENGTH = 32;

    public static final TextEntry GUI_TRADE_ITEM_ENFORCE_DATA = TextEntry.gui(LCApi.MODID,"trade.item.enforce_data");
    public static final TextEntry TOOLTIP_TRADE_ITEM_EDIT_EMPTY = TextEntry.tooltip(LCApi.MODID,"trade.item.edit_item");
    public static final TextEntry TOOLTIP_TRADE_ITEM_EDIT_SHIFT = TextEntry.tooltip(LCApi.MODID,"trade.item.shift_edit_item");
    public static final TextEntry TOOLTIP_TRADE_ITEM_DATA_WARNING_OUTPUT = TextEntry.tooltip(LCApi.MODID,"trade.item.data_warning.output");
    public static final TextEntry TOOLTIP_TRADE_ITEM_DATA_WARNING_INPUT = TextEntry.tooltip(LCApi.MODID,"trade.item.data_warning.input");
    public static final TextEntry TOOLTIP_TRADE_INFO_ORIGINAL_NAME = TextEntry.tooltip(LCApi.MODID,"trade.info.original_name");
    public static final TextEntry GUI_ITEM_EDIT_SEARCH = TextEntry.gui(LCApi.MODID,"item_edit.search");
    public static final TextEntry TOOLTIP_ITEM_EDIT_SCROLL = TextEntry.tooltip(LCApi.MODID,"item_edit.scroll");

    private UnaryOperator<ItemResource> filter = UnaryOperator.identity();
    public final TradeItem withFilter(UnaryOperator<ItemResource> filter) { this.filter = filter; return this; }
    public static <T extends List<TradeItem>> T withFilter(T list,UnaryOperator<ItemResource> filter) { list.forEach(i -> i.withFilter(filter)); return list; }

    private ItemResource item;

    public ItemStack getStack() { return this.item.toStack(this.count); }
    public ItemStack getDisplayStack(TradeContext context) {
        if(this.isFilter()) {
            //TODO display the cycling filter item
        }
        return this.getStack();
    }

    public ResourceStack<ItemResource> getDummyStack() {
        if(this.isEmpty())
            return new ResourceStack<>(ItemResource.EMPTY,0);
        if(this.isFilter())
            throw new NotImplementedException("Filter Items Not Yet Implemented!");
        return new ResourceStack<>(this.item,this.count);
    }

    public void setResource(ItemResource resource) {
        this.item = this.filter.apply(resource);
        if(this.count > this.item.getMaxStackSize())
            this.count = this.item.getMaxStackSize();
    }
    public void setStack(ItemStack stack) { this.setResource(ItemResource.of(stack)); this.setCount(stack.getCount()); }

    private int count;
    public int getCount() { return this.count; }
    public void setCount(int count) { this.count = Math.clamp(count,0,this.item.getMaxStackSize()); }
    public boolean grow(int amount) {
        ItemStack stack = this.item.toStack();
        if(this.count < stack.getMaxStackSize())
        {
            this.count = Math.min(stack.getMaxStackSize(),this.count + amount);
            return true;
        }
        return false;
    }
    public boolean shrink(int amount) {
        ItemStack stack = this.item.toStack();
        if(this.count > 0)
        {
            this.count = Math.max(0,this.count - amount);
            if(this.count == 0)
                this.item = ItemResource.EMPTY;
            return true;
        }
        return false;
    }

    public boolean isValid() { return !this.isEmpty(); }
    public boolean isEmpty() { return this.item.isEmpty() || this.count <= 0; }

    private Optional<String> nameChange;
    public boolean hasNameChange() { return this.nameChange.isPresent(); }
    public Optional<String> getOptionalNameChange() { return this.nameChange; }
    @Nullable
    public String getNameChange() { return this.nameChange.orElse(null); }
    public void setNameChange(String name) {
        if(name.isBlank())
            this.nameChange = Optional.empty();
        else
            this.nameChange = Optional.of(name);
    }

    private boolean strict;
    public boolean isStrict() { return this.strict && !this.isFilter(); }
    public void setStrict(boolean strict) { this.strict = strict; }

    public boolean allowStrictToggle() { return !this.isFilter(); }

    public boolean isFilter() { return false; }

    public static TradeItem create() { return new TradeItem(ItemResource.EMPTY,0,Optional.empty(),true); }
    public static ImmutableList<TradeItem> createList(int size) {
        ImmutableList.Builder<TradeItem> builder = ImmutableList.builderWithExpectedSize(size);
        for(int i = 0; i < size; ++i)
            builder.add(create());
        return builder.build();
    }
    public static ImmutableList<TradeItem> createList(int size,UnaryOperator<ItemResource> filter) {
        return withFilter(createList(size),filter);
    }
    public static void loadList(List<TradeItem> list,List<TradeItem> data) {
        for(int i = 0; i < list.size() && i < data.size(); ++i)
            list.get(i).copyFrom(data.get(i));
    }

    private TradeItem(ItemResource item,int count,Optional<String> nameChange,boolean strict) {
        this.item = item;
        this.count = count;
        this.nameChange = nameChange;
        this.strict = strict;
    }

    public void copyFrom(@Nullable TradeItem other) {
        if(other == null)
            return;
        this.item = other.item;
        this.count = other.count;
        this.nameChange = other.nameChange;
        this.strict = other.strict;
    }

    public final TradeItem copy() {
        TradeItem copy = create();
        copy.copyFrom(this);
        return copy;
    }

    public ItemRequirement asRequirement() {
        if(this.isFilter()) {
            throw new NotImplementedException("Filters not yet implemented!");
        }
        if(this.isStrict())
            return new StackMatchRequirement(this.item,this.count);
        return new ItemMatchRequirement(this.item.getItem(),this.count);
    }

    @Override
    public int hashCode() { return Objects.hash(this.item,this.count,this.nameChange,this.strict); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TradeItem other)
            return this.item.equals(other.item) && this.count == other.count && this.nameChange.equals(other.nameChange) && this.strict == other.strict;
        return false;
    }

    public static List<ItemRequirement> asRequirements(List<TradeItem> items) {
        return ItemRequirement.combine(items.stream().map(TradeItem::asRequirement).toList());
    }

    public static List<TradeItem> combineMatching(List<TradeItem> items) {
        List<TradeItem> list = new ArrayList<>();
        for(TradeItem item : items)
        {
            //Ignore empty requirements
            if(item.isEmpty())
                continue;
            boolean add = true;
            for(TradeItem r : list)
            {
                if(r.equals(item))
                {
                    r.count += item.count;
                    add = false;
                    break;
                }
            }
            if(add)
                list.add(item.copy());
        }
        return list;
    }

    public static boolean allowedInStorage(List<TradeItem> tradeItems,ItemResource item) {
        return tradeItems.stream().map(TradeItem::asRequirement).anyMatch(ti -> ti.test(item));
    }

    @Override
    public String toString() {
        return "TradeItem[" + this.item.toString() + "," + this.count + ",\"" + this.nameChange.orElse("") + "\"," + this.strict + "]";
    }

}