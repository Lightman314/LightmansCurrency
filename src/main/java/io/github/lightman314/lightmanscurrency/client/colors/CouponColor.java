package io.github.lightman314.lightmanscurrency.client.colors;

import io.github.lightman314.lightmanscurrency.common.items.CouponItem;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class CouponColor implements ItemColor{

	@Override
	public int getColor(@Nonnull ItemStack itemStack, int layer) {
		//Apparently these now require an alpha channel, so now adding 0xFF000000 to all colors to ensure alpha is always full
		//Get the Coupon's Color
		if(layer == 0)
			return 0xFF000000 + CouponItem.GetCouponColor(itemStack);

		//Get the Coupon's Inverted Color
		if(layer == 1)
			return 0xFFFFFFFF - CouponItem.GetCouponColor(itemStack);

		//N/A
		return 0xFFFFFFFF;
	}
	
}
