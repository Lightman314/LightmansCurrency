package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.gacha;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.gacha.GachaStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.api.upgrades.slot.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaStorage;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaStorageTab extends TraderStorageTab {

    public GachaStorageTab(ITraderStorageMenu menu) { super(menu); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(Object screen) { return new GachaStorageClientTab(screen,this); }

    @Override
    public boolean canOpen(Player player) { return true; }

    List<EasySlot> slots = new ArrayList<>();
    public List<? extends Slot> getSlots() { return this.slots; }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
        //Upgrade Slots
        if(this.menu.getTrader() instanceof GachaTrader trader && !trader.isPersistent())
        {
            Container upgrades = trader.getUpgrades();
            for(int i = 0; i < upgrades.getContainerSize(); ++i)
            {
                EasySlot upgradeSlot = new UpgradeInputSlot(upgrades, i, 176, 18 + 18 * i, trader);
                upgradeSlot.active = false;
                addSlot.apply(upgradeSlot);
                this.slots.add(upgradeSlot);
            }
        }
    }

    @Override
    public void onTabOpen() { EasySlot.SetActive(this.slots); }

    @Override
    public void onTabClose() { EasySlot.SetInactive(this.slots); }


    @Override
    public boolean quickMoveStack(ItemStack stack) {
        if(this.menu.getTrader() instanceof GachaTrader trader) {
            if(trader.isPersistent())
                return false;

            GachaStorage storage = trader.getStorage();
            if(storage.getSpace() > 0)
            {
                storage.insertItem(stack);
                trader.markUpgradesDirty();
                return true;
            }
            else
                return trader.quickInsertUpgrade(stack);
        }
        return super.quickMoveStack(stack);
    }

    public void clickedOnSlot(int storageSlot, boolean isShiftHeld, boolean leftClick) {
        if(this.menu.getTrader() instanceof GachaTrader trader)
        {
            if(trader.isPersistent())
                return;
            GachaStorage storage = trader.getStorage();
            ItemStack heldItem = this.menu.getHeldItem();
            if(heldItem.isEmpty())
            {
                //Move item out of storage
                List<ItemStack> storageContents = storage.getContents();
                if(storageSlot >= 0 && storageSlot < storageContents.size())
                {
                    ItemStack stackToRemove = storageContents.get(storageSlot).copy();
                    ItemStack removeStack = stackToRemove.copy();

                    //Assume we're moving a whole stack for now
                    int tempAmount = Math.min(stackToRemove.getMaxStackSize(), stackToRemove.getCount());
                    stackToRemove.setCount(tempAmount);
                    int removedAmount;

                    //Right-click, attempt to cut the stack in half
                    if(!leftClick)
                    {
                        if(tempAmount > 1)
                            tempAmount = tempAmount / 2;
                        stackToRemove.setCount(tempAmount);
                    }

                    if(isShiftHeld)
                    {
                        //Put the item in the players inventory. Will not throw overflow on the ground, so it will safely stop if the players inventory is full
                        this.menu.getPlayer().getInventory().add(stackToRemove);
                        //Determine the amount actually added to the players inventory
                        removedAmount = tempAmount - stackToRemove.getCount();
                    }
                    else
                    {
                        //Put the item into the players hand
                        this.menu.setHeldItem(stackToRemove);
                        removedAmount = tempAmount;
                    }
                    //Remove the correct amount from storage
                    if(removedAmount > 0)
                    {
                        removeStack.setCount(removedAmount);
                        storage.removeItem(storageSlot,removedAmount);
                        //Mark the storage dirty
                        trader.markStorageDirty();
                    }
                }
            }
            else
            {
                //Move from hand to storage
                if(leftClick)
                {
                    if(storage.insertItem(heldItem))
                        trader.markStorageDirty();
                }
                else
                {
                    //Right click, only attempt to add 1 from the hand
                    ItemStack addItem = heldItem.copy();
                    addItem.setCount(1);
                    if(storage.insertItem(addItem))
                    {
                        heldItem.shrink(1);
                        if(heldItem.isEmpty())
                            this.menu.setHeldItem(ItemStack.EMPTY);
                    }
                    //Mark the storage dirty
                    trader.markStorageDirty();
                }
            }
            if(this.menu.isClient())
                this.sendStorageClickMessage(storageSlot, isShiftHeld, leftClick);
        }
    }

    private void sendStorageClickMessage(int storageSlot, boolean isShiftHeld, boolean leftClick) {
        this.menu.SendMessage(this.builder()
                .setInt("ClickedSlot", storageSlot)
                .setBoolean("HeldShift", isShiftHeld)
                .setBoolean("LeftClick", leftClick));
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("ClickedSlot", LazyPacketData.TYPE_INT))
        {
            int storageSlot = message.getInt("ClickedSlot");
            boolean isShiftHeld = message.getBoolean("HeldShift");
            boolean leftClick = message.getBoolean("LeftClick");
            this.clickedOnSlot(storageSlot, isShiftHeld, leftClick);
        }
    }

}