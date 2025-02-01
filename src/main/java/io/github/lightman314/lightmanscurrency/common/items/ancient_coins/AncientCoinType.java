package io.github.lightman314.lightmanscurrency.common.items.ancient_coins;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public enum AncientCoinType {

    COPPER("copper"), IRON("iron"), GOLD("gold"), EMERALD("emerald"), DIAMOND("diamond"),
    NETHERITE_H("netherite",true,2),
    NETHERITE_E1("netherite",true,3),
    NETHERITE_R1("netherite",true,3),
    NETHERITE_O("netherite",true,2),
    NETHERITE_B("netherite",true,2),
    NETHERITE_R2("netherite",true,3),
    NETHERITE_I("netherite",true,2),
    NETHERITE_N("netherite",true,2),
    NETHERITE_E2("netherite",true,3),
    LAPIS("lapis"),
    ENDER_PEARL("ender_pearl");

    private static List<String> tags = null;

    public static Collection<String> tags() {
        if(tags == null)
        {
            List<String> t = new ArrayList<>();
            for(AncientCoinType type : values())
            {
                if(!t.contains(type.tag))
                    t.add(type.tag);
            }
            tags = ImmutableList.copyOf(t);
        }
        return tags;
    }

    public final boolean fireResistant;
    private final int ignoreChars;
    public final String tag;
    AncientCoinType(@Nonnull String tag) { this(tag,false,0); }
    AncientCoinType(@Nonnull String tag, boolean fireResistant, int ignoreChars) { this.tag = tag; this.fireResistant = fireResistant; this.ignoreChars = ignoreChars; }

    @Nonnull
    public ItemStack asItem() {
        ItemStack stack = new ItemStack(ModItems.COIN_ANCIENT.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("CoinType",this.toString());
        return stack;
    }
    @Nonnull
    public ItemStack asItem(long count) { return this.asItem((int)count); }
    @Nonnull
    public ItemStack asItem(int count)
    {
        ItemStack item = this.asItem();
        item.setCount(count);
        return item;
    }

    @Nonnull
    public ResourceLocation texture() { return VersionUtil.lcResource("item/ancient_coin/" + this.resourceSafeName()); }

    @Nonnull
    public String translationTag() {
        if(this.ignoreChars <= 0)
            return this.resourceSafeName();
        String safeName = this.resourceSafeName();
        return safeName.substring(0,safeName.length() - this.ignoreChars);
    }

    @Nonnull
    public String initialKey() { return "lightmanscurrency.money.ancient_coins.initial." + this.translationTag(); }

    @Nonnull
    public Component initial() { return EasyText.translatable(this.initialKey()); }

    @Nonnull
    public String resourceSafeName() { return this.toString().toLowerCase(Locale.ENGLISH); }

    @Nonnull
    public AncientCoinType previous() { return fromOrdinal(this.ordinal() - 1, AncientCoinType.ENDER_PEARL); }
    @Nonnull
    public AncientCoinType next() { return fromOrdinal(this.ordinal() + 1, AncientCoinType.COPPER); }
    @Nonnull
    public static AncientCoinType fromOrdinal(int ordinal, @Nonnull AncientCoinType defaultValue)
    {
        for(AncientCoinType type : values())
        {
            if(type.ordinal() == ordinal)
                return type;
        }
        return defaultValue;
    }

}