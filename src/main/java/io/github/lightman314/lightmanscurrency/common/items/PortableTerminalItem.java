package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class PortableTerminalItem extends TooltipItem {

	public PortableTerminalItem(Properties properties)
	{
		super(properties.stacksTo(1), LCTooltips.TERMINAL);
	}
	
	@Override
	public @Nonnull ActionResult<ItemStack> use(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand)
	{
		LightmansCurrency.PROXY.openTerminalScreen();
		return ActionResult.success(player.getItemInHand(hand));
	}
	
}
