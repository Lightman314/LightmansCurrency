package io.github.lightman314.lightmanscurrency.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CouponItem extends Item {

    public CouponItem(Properties properties) { super(properties); }

    public static int GetCouponColor(ItemStack coupon)
    {
        if(coupon.isEmpty() || !(coupon.getItem() instanceof CouponItem))
            return 0xFFFFFF;

        CompoundTag tag = coupon.getTag();
        if(tag == null || !tag.contains("CouponColor"))
            return 0xFFFFFF;
        return tag.getInt("CouponColor");
    }

    public static ItemStack CreateCoupon(Item coupon, String code) { return CreateCoupon(coupon,code,0xFFFFFF); }
    public static ItemStack CreateCoupon(Item coupon, String code, int color) {
        ItemStack stack = new ItemStack(coupon);
        if(code.length() > 16)
            code = code.substring(0,16);
        //Store the code as its hash so that it can't be easily read through item NBT displaying mods and be duplicated
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("CouponCode",code.hashCode());
        tag.putInt("CouponColor",color);
        return stack;
    }

}