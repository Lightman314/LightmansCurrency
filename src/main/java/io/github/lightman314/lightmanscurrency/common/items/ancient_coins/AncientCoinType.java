package io.github.lightman314.lightmanscurrency.common.items.ancient_coins;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplayData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
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

    public static List<String> tags() {
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
    AncientCoinType(String tag) { this(tag,false,0); }
    AncientCoinType(String tag, boolean fireResistant, int ignoreChars) { this.tag = tag; this.fireResistant = fireResistant; this.ignoreChars = ignoreChars; }

    public ItemStack asItem() {
        ItemStack stack = new ItemStack(ModItems.COIN_ANCIENT.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("CoinType",this.toString());
        return stack;
    }

    public ItemStack asItem(long count) { return this.asItem((int)Math.min(count,Integer.MAX_VALUE)); }
    public ItemStack asItem(int count)
    {
        ItemStack item = this.asItem();
        item.setCount(count);
        return item;
    }

    public ResourceLocation texture() { return VersionUtil.lcResource("item/ancient_coin/" + this.resourceSafeName()); }

    public String translationTag() {
        if(this.ignoreChars <= 0)
            return this.resourceSafeName();
        String safeName = this.resourceSafeName();
        return safeName.substring(0,safeName.length() - this.ignoreChars);
    }

    public String initialKey() { return "lightmanscurrency.money.ancient_coins.initial." + this.resourceSafeName(); }
    public String iconKey() { return "lightmanscurrency.money.ancient_coins.icon." + this.resourceSafeName(); }
    
    public Component initial() { return EasyText.translatable(this.initialKey()); }
    public Component icon() { return EasyText.translatableWithFallback(this.iconKey(),ValueDisplayData.ICON_FALLBACK_KEY).withStyle(ChatFormatting.WHITE); }

    public String resourceSafeName() { return this.toString().toLowerCase(Locale.ENGLISH); }

    public AncientCoinType previous() { return fromOrdinal(this.ordinal() - 1, AncientCoinType.ENDER_PEARL); }

    public AncientCoinType next() { return fromOrdinal(this.ordinal() + 1, AncientCoinType.COPPER); }
    
    public static AncientCoinType fromOrdinal(int ordinal, AncientCoinType defaultValue)
    {
        for(AncientCoinType type : values())
        {
            if(type.ordinal() == ordinal)
                return type;
        }
        return defaultValue;
    }

    public static ItemStack randomizingItem() { return randomizingItem(1); }
    public static ItemStack randomizingItem(int count) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("RandomCoin",true);
        ItemStack item = new ItemStack(ModItems.COIN_ANCIENT.get(),count);
        item.setTag(tag);
        return item;
    }
    public static AncientCoinType random(RandomSource random)
    {
        return randomWithTags(tags().get(random.nextInt(tags().size())),random);
    }
    private static AncientCoinType randomWithTags(String tag, RandomSource random)
    {
        List<AncientCoinType> coinsWithTag = new ArrayList<>();
        for(AncientCoinType type : AncientCoinType.values())
        {
            if(type.tag.equals(tag))
                coinsWithTag.add(type);
        }
        if(coinsWithTag.isEmpty())
            return AncientCoinType.COPPER;
        return coinsWithTag.get(random.nextInt(coinsWithTag.size()));
    }

}