package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import io.github.lightman314.lightmanscurrency.common.menus.providers.TerminalMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.ItemValidator;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class PortableTerminalItem extends TooltipItem implements IVariantItem {

	public PortableTerminalItem(Properties properties)
	{
		super(properties.stacksTo(1), LCText.TOOLTIP_TERMINAL.asTooltip());
	}
	
	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand)
	{
		if(!player.level().isClientSide)
			TerminalMenuProvider.OpenMenu(player, new ItemValidator(this));
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}
	
}
