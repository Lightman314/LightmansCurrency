package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class PortableATMItem extends TooltipItem{

	public PortableATMItem(Properties properties)
	{
		super(properties.stacksTo(1), LCTooltips.ATM);
	}
	
	@Nonnull
	@Override
	public ActionResult<ItemStack> use(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand)
	{
		player.openMenu(this.getMenuProvider());
		return ActionResult.success(player.getItemInHand(hand));
	}
	
	public INamedContainerProvider getMenuProvider()
	{
		return new SimpleNamedContainerProvider((windowId, playerInventory, playerEntity) -> new ATMMenu(windowId, playerInventory), EasyText.empty());
	}
	
}
