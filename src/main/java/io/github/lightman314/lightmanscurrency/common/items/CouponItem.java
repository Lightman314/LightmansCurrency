package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.data.CouponData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CouponItem extends Item {

    public CouponItem(Properties properties) { super(properties); }

    public static int GetCouponColor(ItemStack coupon)
    {
        if(coupon.isEmpty() || !(coupon.getItem() instanceof CouponItem) || !coupon.has(ModDataComponents.COUPON_DATA))
            return 0xFFFFFF;
        return coupon.get(ModDataComponents.COUPON_DATA).color();
    }

    public static ItemStack CreateCoupon(Item coupon, String code) { return CreateCoupon(coupon,code,0xFFFFFF); }
    public static ItemStack CreateCoupon(Item coupon, String code, int color) {
        ItemStack stack = new ItemStack(coupon);
        if(code.length() > 16)
            code = code.substring(0,16);
        //Store the code as its hash so that it can't be easily read through item NBT displaying mods and be duplicated
        stack.set(ModDataComponents.COUPON_DATA,new CouponData(code.hashCode(),color));
        return stack;
    }

}
