package io.github.lightman314.lightmanscurrency.common.menus.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType.IUpgradeable;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class UpgradeInputSlot extends SimpleSlot{

	public static final ResourceLocation EMPTY_UPGRADE_SLOT = new ResourceLocation(LightmansCurrency.MODID, "item/empty_upgrade_slot");
	
	private final IUpgradeable machine;
	private final IMessage onModified;
	
	public UpgradeInputSlot(IInventory inventory, int index, int x, int y, IUpgradeable machine) { this(inventory, index, x, y, machine, () -> {}); }
	
	public UpgradeInputSlot(IInventory inventory, int index, int x, int y, IUpgradeable machine, IMessage onModified)
	{
		super(inventory, index, x, y);
		this.machine = machine;
		this.onModified = onModified;
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack)
	{
		Item item = stack.getItem();
		if(item instanceof UpgradeItem)
			return machine.allowUpgrade((UpgradeItem)item);
		return false;
	}
	
	@Override
	public int getMaxStackSize() { return 1; }
	
	@Override
	public void setChanged() {
		super.setChanged();
		if(this.onModified != null)
			this.onModified.accept();
	}
	
	public interface IMessage
	{
		public void accept();
	}
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() {
		return Pair.of(PlayerContainer.BLOCK_ATLAS, EMPTY_UPGRADE_SLOT);
	}
	
}
