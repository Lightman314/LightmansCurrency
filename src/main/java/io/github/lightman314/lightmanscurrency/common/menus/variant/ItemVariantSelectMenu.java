package io.github.lightman314.lightmanscurrency.common.menus.variant;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasySlot;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.api.variants.item.data.VariantData;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ItemVariantSelectMenu extends VariantSelectMenu {

    private final VariantHolder variantHolder = new VariantHolder();

    public ItemVariantSelectMenu(int id, Inventory inventory) {
        super(ModMenus.VARIANT_SELECT_ITEM.get(), id, inventory);

        this.addSlot(new EasySlot(this.variantHolder,0,155,72));

        //Add Inventory Slots
        //Player inventory
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 11 + x * 18, 154 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(inventory, x, 11 + x * 18, 212));
        }

    }

    public ItemStack getVariantStack() { return this.variantHolder.getItem().copy(); }

    private boolean validSlotItem() { return VariantProvider.getVariantItem(this.variantHolder.getItem().getItem()) != null; }

    @Override
    protected void changeVariant(@Nullable ResourceLocation newVariant) {
        ItemStack variantItem = this.variantHolder.getItem();
        if(variantItem.isEmpty())
            return;
        if(variantItem.has(ModDataComponents.VARIANT_LOCK) && !this.player.isCreative())
        {
            LightmansCurrency.LogDebug(this.player.getName().getString() + " attempted to change the variant of a locked item!");
            return;
        }
        if(newVariant == null)
            variantItem.remove(ModDataComponents.MODEL_VARIANT);
        else if(VariantProvider.getVariantItem(variantItem) != null)
            variantItem.set(ModDataComponents.MODEL_VARIANT,new VariantData(newVariant));
    }
    @Nullable
    @Override
    public ResourceLocation getSelectedVariant() {
        if(this.validSlotItem())
        {
            ItemStack variantItem = this.variantHolder.getItem();
            return variantItem.getOrDefault(ModDataComponents.MODEL_VARIANT,VariantData.NULL).variant();
        }
        return null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack clickedStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            clickedStack = slotStack.copy();
            if(index < 1)
            {
                //Move from variant slot back into inventory
                if(!this.moveItemStackTo(slotStack, 1, this.slots.size(), true))
                    return ItemStack.EMPTY;
            }
            else
            {
                //Move from inventory to item variant slot
                if(!this.moveItemStackTo(slotStack, 0, 1, false))
                {
                    return ItemStack.EMPTY;
                }
            }


            if(slotStack.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
        }

        return clickedStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player,this.variantHolder);
    }

    public static MenuProvider providerFor() { return Provider.INSTANCE; }

    private static class Provider implements EasyMenuProvider
    {
        private static final Provider INSTANCE = new Provider();
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) { return new ItemVariantSelectMenu(containerId,playerInventory); }
    }

    private static class VariantHolder extends LCItemStackHandler {
        private VariantHolder() { super(1); }
        public final ItemStack getItem() { return this.getStackInSlot(0); }
        @Override
        public boolean isItemValid(int slot, ItemStack stack) { return VariantProvider.getVariantItem(stack.getItem()) != null; }
    }

}
