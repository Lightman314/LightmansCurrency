package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.data.CouponData;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CouponItem extends Item implements IVariantItem {

    public CouponItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if(stack.has(ModDataComponents.TICKET_USES))
            tooltip.add(LCText.TOOLTIP_TICKET_USES.get(stack.get(ModDataComponents.TICKET_USES)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void verifyComponentsAfterLoad(ItemStack stack) {
        if(stack.has(ModDataComponents.COUPON_DATA))
        {
            CouponData oldData = stack.get(ModDataComponents.COUPON_DATA);
            stack.remove(ModDataComponents.COUPON_DATA);
            stack.set(ModDataComponents.COUPON_CODE,oldData.code());
            stack.set(DataComponents.DYED_COLOR,new DyedItemColor(oldData.color(),false));
        }
        if(stack.has(ModDataComponents.TICKET_USES) && stack.get(ModDataComponents.TICKET_USES) <= 0)
            stack.remove(ModDataComponents.TICKET_USES);
        super.verifyComponentsAfterLoad(stack);
    }

    public static int GetCouponCode(ItemStack coupon) { return coupon.getOrDefault(ModDataComponents.COUPON_CODE,0); }

    public static int GetCouponColor(ItemStack coupon)
    {
        if(coupon.isEmpty() || !(coupon.getItem() instanceof CouponItem) || !coupon.has(DataComponents.DYED_COLOR))
            return 0xFFFFFF;
        return coupon.get(DataComponents.DYED_COLOR).rgb();
    }

    public static ItemStack CreateCoupon(Item coupon, String code, int durability) { return CreateCoupon(coupon,code,durability,0xFFFFFF); }
    public static ItemStack CreateCoupon(Item coupon, String code, int durability, int color) {
        ItemStack stack = new ItemStack(coupon);
        if(code.length() > 16)
            code = code.substring(0,16);
        //Store the code as its hash so that it can't be easily read through item NBT displaying mods and be duplicated
        stack.set(ModDataComponents.COUPON_CODE,code.hashCode());
        stack.set(DataComponents.DYED_COLOR,new DyedItemColor(color,false));
        //Set durability
        if(durability > 0)
            stack.set(ModDataComponents.TICKET_USES,durability);
        return stack;
    }

}
