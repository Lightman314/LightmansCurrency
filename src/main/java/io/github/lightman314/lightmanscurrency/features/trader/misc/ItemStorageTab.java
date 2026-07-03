package io.github.lightman314.lightmanscurrency.features.trader.misc;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.UpgradeNode;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.world.UpgradeStorage;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.EasyResourceSlot;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.IEasySlot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemStorageTab extends TraderStorageTab {

    public static final Identifier KEY = LCApi.id("item_storage");

    public ItemStorageTab(TraderStorageMenu menu) { super(menu); }

    @Override
    public Identifier getKey() { return KEY; }
    @Override
    public boolean canOpen() { return true; }
    @Override
    public int getTabSortPriority() { return -100; }

    private final List<Slot> slots = new ArrayList<>();
    public List<Slot> getSlots() { return this.slots; }

    @Override
    public void addMenuSlots(Consumer<Slot> builder) {
        UpgradeNode node = this.getNode(UpgradeNode.TYPE);
        if(node != null)
        {
            UpgradeStorage upgrades = node.getStorage();
            for(int i = 0; i < upgrades.size(); ++i)
            {
                EasyResourceSlot slot = new EasyResourceSlot(upgrades,upgrades::setItem,i,176,18 + 18 * i);
                slot.setActive(false);
                slot.setBackground(UpgradeType.EMPTY_SLOT_SPRITE);
                builder.accept(slot);
                this.slots.add(slot);

            }
        }
    }

    @Override
    public boolean quickMoveStack(Player player,int slotIndex) {
        TraderStorageMenu menu = this.getMenu();
        if(slotIndex >= 0 && slotIndex < menu.slots.size())
        {
            Slot s = menu.getSlot(slotIndex);
            if(this.slots.contains(s))
            {
                //Move item from upgrades to the players inventory
                if(!menu.moveStackTo(s.getItem(),0,TraderStorageMenu.INVENTORY_SLOTS,true))
                    return true;
            }
        }
        return super.quickMoveStack(player,slotIndex);
    }

    public void onItemClick(int slot, int button) {
        ItemStorageNode node = this.getNode(ItemStorageNode.TYPE);
        if(node != null)
        {
            try(Transaction transaction = Transaction.openRoot())
            {
                FlexibleItemStorage storage = node.getStorage();
                ItemStack heldItem = this.getMenu().getCarried();
                if(heldItem.isEmpty())
                {
                    int takeCount = button == 1 ? 1 : Integer.MAX_VALUE;
                    ItemResource current = storage.getResource(slot);
                    if(!current.isEmpty())
                    {
                        takeCount = Math.min(takeCount,storage.getAmountAsInt(slot));
                        int taken = storage.extract(slot,current,takeCount,transaction);
                        if(taken > 0 && taken <= takeCount)
                        {
                            transaction.commit();
                            this.getMenu().setCarried(current.toStack(taken));
                        }
                    }
                }
                else
                {
                    int insertCount = button == 1 ? 1 : heldItem.getCount();
                    int inserted = storage.insert(ItemResource.of(heldItem),insertCount,transaction);
                    if(inserted > 0 && inserted <= insertCount)
                    {
                        transaction.commit();
                        heldItem.shrink(inserted);
                        this.getMenu().setCarried(heldItem);
                    }
                }
            }
        }
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.newMutable().setMap("storage_click",FancyPacketMap.newMutable()
                    .setInt("slot",slot)
                    .setInt("button",button)));
        }
    }

    @Override
    public void onTabOpened(FancyPacketMap additional) { IEasySlot.setActive(this.slots,true); }

    @Override
    public void onTabClosed() { IEasySlot.setActive(this.slots,false); }

    @Override
    public void handleMessage(FancyPacketMap packet) {
        if(packet.contains("storage_click"))
        {
            FancyPacketMap entry = packet.getMap("storage_click");
            this.onItemClick(entry.getInt("slot"),entry.getInt("button"));
        }
    }

}