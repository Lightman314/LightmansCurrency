package io.github.lightman314.lightmanscurrency.common.menus.slots;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.mojang.datafixers.util.Pair;

public class WalletSlot extends Slot {
	
	public static final ResourceLocation EMPTY_WALLET_SLOT = LightmansCurrency.id( "item/empty_wallet_slot");
	public static final Pair<ResourceLocation,ResourceLocation> BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_WALLET_SLOT);

    private final LivingEntity entity;
	public WalletSlot(LivingEntity entity, int x, int y)
	{
		super(new SimpleContainer(0), 0, x, y);
        this.entity = entity;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) { return isValidWallet(stack); }

    @Override
    public ItemStack getItem() {
        WalletHandler handler = WalletHandler.get(this.entity);
        return handler.getWallet();
    }

    @Override
    public void set(ItemStack stack) {
        WalletHandler handler = WalletHandler.get(this.entity);
        handler.setWallet(stack);
        this.setChanged();
    }

    public static boolean isValidWallet(ItemStack stack) { return stack.isEmpty() || stack.getItem() instanceof WalletItem; }
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() { return BACKGROUND; }

}
