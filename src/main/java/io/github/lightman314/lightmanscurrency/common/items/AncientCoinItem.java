package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.coins.ICoinLike;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AncientCoinItem extends Item implements ICoinLike {

    public static final ResourceLocation PROPERTY = VersionUtil.lcResource("ancient_coin_type");

    public AncientCoinItem(Properties properties) { super(properties); }

    @Nullable
    public static AncientCoinType getAncientCoinType(ItemStack item)
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
    public void verifyTagAfterLoad(CompoundTag tag) {
        //Don't verify if the coin is supposed to be random
        if(tag.getBoolean("RandomCoin"))
            return;
        if(!tag.contains("CoinType"))
            tag.putString("CoinType", AncientCoinType.COPPER.toString());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        CompoundTag tag = stack.getTag();
        if(tag != null && tag.getBoolean("RandomCoin") && entity instanceof Player player)
        {
            RandomSource random = player.getRandom();
            //Randomize now
            while(stack.getCount() > 1)
            {
                stack.shrink(1);
                ItemHandlerHelper.giveItemToPlayer(player,AncientCoinType.random(random).asItem());
            }
            tag.remove("RandomCoin");
            tag.putString("CoinType",AncientCoinType.random(random).toString());
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
    public boolean canBeHurtBy(DamageSource source) {
        if(source.is(DamageTypeTags.IS_FIRE))
            return false;
        return super.canBeHurtBy(source);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> tooltip, TooltipFlag flag) {
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