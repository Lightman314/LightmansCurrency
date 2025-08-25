package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CouponItem extends Item {

    public CouponItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
        int uses = TicketItem.getUseCount(stack);
        if(uses > 0)
            tooltip.add(LCText.TOOLTIP_TICKET_USES.get(uses).withStyle(ChatFormatting.GRAY));
    }

    public static int GetCouponCode(ItemStack coupon)
    {
        CompoundTag tag = coupon.getTag();
        if(tag != null && tag.contains("CouponCode"))
            return tag.getInt("CouponCode");
        return 0;
    }

    public static int GetCouponColor(ItemStack coupon)
    {
        if(coupon.isEmpty() || !(coupon.getItem() instanceof CouponItem))
            return 0xFFFFFF;
        CompoundTag tag = coupon.getTag();
        if(tag == null || !tag.contains("CouponColor"))
            return 0xFFFFFF;
        return tag.getInt("CouponColor");
    }

    public static ItemStack CreateCoupon(Item coupon, String code, int durability) { return CreateCoupon(coupon,code,durability, 0xFFFFFF); }
    public static ItemStack CreateCoupon(Item coupon, String code, int durability, int color) {
        ItemStack stack = new ItemStack(coupon);
        if(code.length() > 16)
            code = code.substring(0,16);
        //Store the code as its hash so that it can't be easily read through item NBT displaying mods and be duplicated
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("CouponCode",code.hashCode());
        tag.putInt("CouponColor",color);
        LightmansCurrency.LogDebug("Crafted Coupon with code '" + code + "' and durability '" + durability + "'!");
        //Set durability
        if(durability > 0)
            TicketItem.setUseCount(stack,durability);
        return stack;
    }

}