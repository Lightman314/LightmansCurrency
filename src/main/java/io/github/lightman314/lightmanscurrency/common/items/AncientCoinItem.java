package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.coins.ICoinLike;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class AncientCoinItem extends Item implements ICoinLike {

    public static final ResourceLocation PROPERTY = VersionUtil.lcResource("ancient_coin_type");

    public AncientCoinItem(Properties properties) { super(properties); }

    @Nullable
    public static AncientCoinType getAncientCoinType(@Nonnull ItemStack item)
    {
        if(item.is(ModItems.COIN_ANCIENT.get()))
        {
            CompoundTag tag = item.getTag();
            if(tag == null || !tag.contains("CoinType"))
                return null;
            return EnumUtil.enumFromString(tag.getString("CoinType"),AncientCoinType.values(),null);
        }
        return null;
    }

    @Override
    public void verifyTagAfterLoad(@Nonnull CompoundTag tag) {
        if(!tag.contains("CoinType"))
            tag.putString("CoinType", AncientCoinType.COPPER.toString());
    }

    @Nonnull
    @Override
    public String getDescriptionId(@Nonnull ItemStack stack) {
        String original = super.getDescriptionId();
        AncientCoinType type = getAncientCoinType(stack);
        if(type == null)
            return original;
        else
            return original + "." + type.translationTag();
    }

    @Override
    public boolean canBeHurtBy(@Nonnull DamageSource source) {
        if(source.is(DamageTypeTags.IS_FIRE))
            return false;
        return super.canBeHurtBy(source);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if(flag.isAdvanced())
        {
            AncientCoinType type = getAncientCoinType(stack);
            if(type == null)
                return;
            tooltip.add(LCText.TOOLTIP_ANCIENT_COIN_ADVANCED_TYPE.get(LCText.ANCIENT_COIN_TYPE_LABEL.get(type).get()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}