package io.github.lightman314.lightmanscurrency.items;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.tooltips.LCTooltips;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class PortableTerminalItem extends TooltipItem {

	public PortableTerminalItem(Properties properties)
	{
		super(properties.stacksTo(1), LCTooltips.TERMINAL);
	}
	
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand)
	{
		LightmansCurrency.PROXY.openTerminalScreen();
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}
	
}
