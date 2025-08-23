package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.data.CouponData;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CouponItem extends Item {

    public CouponItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if(stack.has(ModDataComponents.TICKET_USES))
            tooltip.add(LCText.TOOLTIP_TICKET_USES.get(stack.get(ModDataComponents.TICKET_USES)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        if(stack.has(ModDataComponents.TICKET_USES) && stack.get(ModDataComponents.TICKET_USES) <= 0)
            stack.remove(ModDataComponents.TICKET_USES);
        super.verifyComponentsAfterLoad(stack);
    }

    public static int GetCouponColor(ItemStack coupon)
    {
        if(coupon.isEmpty() || !(coupon.getItem() instanceof CouponItem) || !coupon.has(ModDataComponents.COUPON_DATA))
            return 0xFFFFFF;
        return coupon.get(ModDataComponents.COUPON_DATA).color();
    }

    public static ItemStack CreateCoupon(Item coupon, String code, int durability) { return CreateCoupon(coupon,code,durability,0xFFFFFF); }
    public static ItemStack CreateCoupon(Item coupon, String code, int durability, int color) {
        ItemStack stack = new ItemStack(coupon);
        if(code.length() > 16)
            code = code.substring(0,16);
        //Store the code as its hash so that it can't be easily read through item NBT displaying mods and be duplicated
        stack.set(ModDataComponents.COUPON_DATA,new CouponData(code.hashCode(),color));
        //Set durability
        if(durability > 0)
            stack.set(ModDataComponents.TICKET_USES,durability);
        return stack;
    }

}
