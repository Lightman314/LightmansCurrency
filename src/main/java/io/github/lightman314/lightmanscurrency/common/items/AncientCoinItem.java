package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.coins.ICoinLike;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AncientCoinItem extends Item implements ICoinLike {

    public static final ResourceLocation PROPERTY = VersionUtil.lcResource("ancient_coin_type");

    public AncientCoinItem(Properties properties) { super(properties); }

    @Override
    public boolean isCoin(ItemStack stack) { return stack.has(ModDataComponents.ANCIENT_COIN_TYPE); }

    @Nullable
    public static AncientCoinType getAncientCoinType(ItemStack item)
    {
        if(item.is(ModItems.COIN_ANCIENT.get()))
            return item.get(ModDataComponents.ANCIENT_COIN_TYPE);
        return null;
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        //Don't verify if the coin is supposed to be random
        if(stack.has(ModDataComponents.ANCIENT_COIN_RANDOM))
            return;
        if(!stack.has(ModDataComponents.ANCIENT_COIN_TYPE))
            stack.set(ModDataComponents.ANCIENT_COIN_TYPE,AncientCoinType.COPPER);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if(stack.has(ModDataComponents.ANCIENT_COIN_RANDOM) && entity instanceof Player player)
        {
            RandomSource random = player.getRandom();
            //Randomize now
            while(stack.getCount() > 1)
            {
                stack.shrink(1);
                ItemHandlerHelper.giveItemToPlayer(player,AncientCoinType.random(random).asItem());
            }
            stack.remove(ModDataComponents.ANCIENT_COIN_RANDOM);
            stack.set(ModDataComponents.ANCIENT_COIN_TYPE,AncientCoinType.random(random));
        }
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        String original = super.getDescriptionId();
        AncientCoinType type = getAncientCoinType(stack);
        if(type == null)
            return original;
        else
            return original + "." + type.translationTag();
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        AncientCoinType type = getAncientCoinType(stack);
        if(type != null && type.fireResistant && source.is(DamageTypeTags.IS_FIRE))
            return false;
        return super.canBeHurtBy(stack, source);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
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
