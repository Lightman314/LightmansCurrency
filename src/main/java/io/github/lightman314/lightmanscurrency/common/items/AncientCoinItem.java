package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.coins.ICoinLike;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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
            return item.get(ModDataComponents.ANCIENT_COIN_TYPE);
        return null;
    }

    @Override
    public void verifyComponentsAfterLoad(@Nonnull ItemStack stack) {
        if(!stack.has(ModDataComponents.ANCIENT_COIN_TYPE))
            stack.set(ModDataComponents.ANCIENT_COIN_TYPE, AncientCoinType.COPPER);
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
    public boolean canBeHurtBy(@Nonnull ItemStack stack, @Nonnull DamageSource source) {
        AncientCoinType type = getAncientCoinType(stack);
        if(type != null && type.fireResistant && source.is(DamageTypeTags.IS_FIRE))
            return false;
        return super.canBeHurtBy(stack, source);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
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
