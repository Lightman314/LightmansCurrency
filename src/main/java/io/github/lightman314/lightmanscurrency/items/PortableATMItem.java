package io.github.lightman314.lightmanscurrency.items;

import io.github.lightman314.lightmanscurrency.containers.ATMContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class PortableATMItem extends Item{

	public PortableATMItem(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		player.openContainer(this.getContainer());
		return ActionResult.resultSuccess(player.getHeldItem(hand));
	}
	
	public INamedContainerProvider getContainer()
	{
		return new SimpleNamedContainerProvider((windowId, playerInventory, playerEntity) -> { return new ATMContainer(windowId, playerInventory);}, new StringTextComponent(""));
	}
	
}
