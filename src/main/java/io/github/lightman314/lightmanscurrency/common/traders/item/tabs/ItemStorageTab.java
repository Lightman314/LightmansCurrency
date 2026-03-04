package io.github.lightman314.lightmanscurrency.common.traders.item.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasyItemHandlerSlot;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.UpgradesNode;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeStackHandler;
import io.github.lightman314.lightmanscurrency.common.traders.item.client.tabs.ItemStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ItemStorageTab extends TraderStorageNodeTab<ItemStorageNode> {

    public static final ResourceLocation KEY = LightmansCurrency.id("item_storage");

    public ItemStorageTab(ITraderStorageMenu menu) { super(ItemStorageNode.TYPE,menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }

    @Override
    public Object createClientTab(Object screen) { return new ItemStorageClientTab(screen, this); }

    List<EasySlot> slots = new ArrayList<>();
    public List<? extends Slot> getSlots() { return this.slots; }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
        UpgradesNode node = this.getNode(UpgradesNode.TYPE);
        //Upgrade Slots
        if(node != null && !this.isPersistent())
        {
            UpgradeStackHandler upgrades = node.getContainer();
            for(int i = 0; i < upgrades.getSlots(); ++i)
            {
                EasySlot upgradeSlot = new EasyItemHandlerSlot(upgrades, i, 176, 18 + 18 * i);
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
        ItemStorageNode node = this.getNode();
        if(node != null) {
            if(isPersistent())
                return false;
            TraderItemStorage storage = node.getStorage();
            if(storage.getFittableAmount(stack) > 0)
            {
                storage.tryAddItem(stack);
                return true;
            }
            else
            {
                //Always mark as dirty just in case the client thought it succeeded even though it failed
                node.setStorageChanged();
                UpgradesNode upgradesNode = this.getNode(UpgradesNode.TYPE);
                if(upgradesNode != null)
                    return upgradesNode.quickInsertUpgrade(stack);
                return false;
            }
        }
        return super.quickMoveStack(stack);
    }

    public void clickedOnSlot(int storageSlot, boolean isShiftHeld, boolean leftClick) {
        ItemStorageNode node = this.getNode();
        if(node != null)
        {
            if(this.isPersistent())
                return;
            TraderItemStorage storage = node.getStorage();
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
                        storage.removeItemLimited(removeStack);
                    }
                }
            }
            else
            {
                //Move from hand to storage
                if(leftClick)
                {
                    storage.tryAddItem(heldItem);
                }
                else
                {
                    //Right click, only attempt to add 1 from the hand
                    ItemStack addItem = heldItem.copy();
                    addItem.setCount(1);
                    if(storage.addItem(addItem))
                    {
                        heldItem.shrink(1);
                        if(heldItem.isEmpty())
                            this.menu.setHeldItem(ItemStack.EMPTY);
                    }
                }
            }
            if(this.menu.isClient())
                this.sendStorageClickMessage(storageSlot, isShiftHeld, leftClick);
            else //Always mark as dirty, just in case the client thought it succeeded even though it failed
                node.setStorageChanged();
        }
    }

    private void sendStorageClickMessage(int storageSlot, boolean isShiftHeld, boolean leftClick) {
        this.menu.SendMessage(this.builder()
                .setInt("ClickedSlot", storageSlot)
                .setBoolean("HeldShift", isShiftHeld)
                .setBoolean("LeftClick", leftClick));
    }

    public void quickTransfer(int type) {
        ItemStorageNode node = this.getNode();
        if(node != null) {
            if(this.isPersistent())
                return;
            TraderItemStorage storage = node.getStorage();
            Inventory inv = this.menu.getPlayer().getInventory();
            boolean changed = false;
            if(type == 0)
            {
                //Quick Deposit
                for(int i = 0; i < 36; ++i)
                {
                    ItemStack stack = inv.getItem(i);
                    int fillAmount = storage.getFittableAmount(stack);
                    if(fillAmount > 0)
                    {
                        //Remove the item from the players inventory
                        ItemStack fillStack = inv.removeItem(i, fillAmount);
                        //Put the item into storage
                        storage.forceAddItem(fillStack);
                    }
                }
            }
            else if(type == 1)
            {
                //Quick Extract
                List<ItemStack> itemList = InventoryUtil.copyList(storage.getContents());
                for(ItemStack stack : itemList)
                {
                    boolean keepTrying = true;
                    while(storage.getItemCount(stack) > 0 && keepTrying)
                    {
                        ItemStack transferStack = stack.copy();
                        int transferCount = Math.min(storage.getItemCount(stack), stack.getMaxStackSize());
                        transferStack.setCount(transferCount);
                        //Attempt to move the stack into the players inventory
                        int removedCount = InventoryUtil.safeGiveToPlayer(inv, transferStack);
                        if(removedCount > 0)
                        {
                            //Remove the transferred amount from storage
                            ItemStack removeStack = stack.copy();
                            removeStack.setCount(removedCount);
                            storage.removeItemLimited(removeStack);
                        }
                        else
                            keepTrying = false;
                    }
                }
            }

            if(this.menu.isClient())
                this.menu.SendMessage(this.builder().setInt("QuickTransfer", type));

        }
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
        if(message.contains("QuickTransfer"))
        {
            this.quickTransfer(message.getInt("QuickTransfer"));
        }
    }

}