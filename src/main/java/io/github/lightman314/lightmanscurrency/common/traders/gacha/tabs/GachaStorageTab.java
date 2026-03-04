package io.github.lightman314.lightmanscurrency.common.traders.gacha.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasyItemHandlerSlot;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.UpgradesNode;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeStackHandler;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.client.tabs.GachaStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasySlot;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaStorage;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaStorageNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GachaStorageTab extends TraderStorageNodeTab<GachaStorageNode> {

    public static final ResourceLocation KEY = LightmansCurrency.id("gacha_storage");

    public GachaStorageTab(ITraderStorageMenu menu) { super(GachaStorageNode.TYPE,menu); }

    @Override
    protected boolean isDefaultTab() { return true; }

    @Override
    public ResourceLocation tabKey() { return KEY; }

    @Override
    public Object createClientTab(Object screen) { return new GachaStorageClientTab(screen,this); }

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
        GachaStorageNode node = this.getNode();
        if(node != null && !this.isPersistent()) {
            GachaStorage storage = node.getStorage();
            if(storage.getSpace() > 0)
            {
                storage.insertItem(stack);
                return true;
            }
            else
            {
                UpgradesNode upgradesNode = this.getNode(UpgradesNode.TYPE);
                if(upgradesNode != null)
                    return upgradesNode.quickInsertUpgrade(stack);
                return false;
            }
        }
        return super.quickMoveStack(stack);
    }

    public void clickedOnSlot(int storageSlot, boolean isShiftHeld, boolean leftClick) {
        GachaStorageNode node = this.getNode();
        if(node != null && !this.isPersistent())
        {
            GachaStorage storage = node.getStorage();
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
                    }
                }
            }
            else
            {
                //Move from hand to storage
                if(leftClick)
                {
                    storage.insertItem(heldItem);
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