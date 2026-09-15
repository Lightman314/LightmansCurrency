package io.github.lightman314.lightmanscurrency.features.trader.item_common;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.debug.DebugHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ItemStorageTab extends TraderStorageTab {

    public static final Identifier KEY = LCApi.id("item_storage");

    public static final TextEntry TOOLTIP_ITEM_STORAGE = TextEntry.tooltip(LCApi.MODID,"trader.storage.item_storage");

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
                EasyResourceSlot slot = new EasyResourceSlot(upgrades,i,176,18 + 18 * i);
                slot.setActive(false);
                slot.setBackground(UpgradeType.EMPTY_SLOT_SPRITE);
                builder.accept(slot);
                this.slots.add(slot);

            }
        }
    }

    @Override
    public Optional<ItemStack> quickMoveStack(Player player,int slotIndex) {
        if(slotIndex >= 0 && slotIndex < TraderStorageMenu.INVENTORY_SLOTS)
        {
            Slot slot = this.getMenu().getSlot(slotIndex);
            ItemStorageNode node = this.getNode(ItemStorageNode.TYPE);
            if(slot == null || node == null)
                return Optional.empty();
            ItemStack stack = slot.getItem();
            if(stack.isEmpty())
                return Optional.of(stack);
            ItemStack clicked = stack.copy();
            int count = stack.getCount();
            //Move item from their inventory into the storage
            try(Transaction transaction = Transaction.openRoot()) {
                FlexibleItemStorage storage = node.getStorage();
                int inserted = storage.insert(ItemResource.of(stack),count,transaction);
                if(inserted > 0 && inserted <= count) {
                    ItemStack debugStack = stack.copyWithCount(inserted);
                    stack.shrink(inserted);
                    if(stack.isEmpty())
                        slot.setByPlayer(ItemStack.EMPTY);
                    else
                        slot.setChanged();
                    transaction.commit();
                    LightmansCurrency.LogDebug("Inserted " + debugStack + " via quick-move on the " + DebugHelper.sideName(this));
                }
                else {
                    LightmansCurrency.LogDebug("Could not insert any of " + stack + " on the " + DebugHelper.sideName(this));
                    return Optional.empty();
                }
            }
            return Optional.of(clicked);
        }
        return super.quickMoveStack(player,slotIndex);
    }

    public void onItemClick(int slot,int button,boolean heldShift) {
        ItemStorageNode node = this.getNode(ItemStorageNode.TYPE);
        if(node != null)
        {
            try(Transaction transaction = Transaction.openRoot())
            {
                FlexibleItemStorage storage = node.getStorage();
                ItemStack heldItem = this.getMenu().getCarried();
                //LightmansCurrency.LogDebug("Slot " + slot + " clicked on the " + DebugHelper.sideName(this) + " with " + heldItem + " in their hand!");
                if(heldShift) {
                    ItemResource current = storage.getResource(slot);
                    if(current.isEmpty())
                        return;
                    int takeCount = Math.min(button == 1 ? 1 : current.getMaxStackSize(),storage.getAmountAsInt(slot));
                    ResourceHandler<ItemResource> inventory = PlayerInventoryWrapper.of(this.getPlayer()).getMainSlots();
                    ResourceHandlerUtil.moveStacking(storage,inventory,r -> r.equals(current),takeCount,transaction);
                    transaction.commit();
                }
                else if(heldItem.isEmpty())
                {
                    ItemResource current = storage.getResource(slot);
                    int takeCount = button == 1 ? 1 : current.getMaxStackSize();
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
            this.sendToServer(FancyPacketMap.map().setMap("storage_click",FancyPacketMap.map()
                    .setInt("slot",slot)
                    .setInt("button",button)
                    .setBoolean("shift",heldShift)));
        }
    }

    public void quickInsert() {
        ItemStorageNode node = this.getNode(ItemStorageNode.TYPE);
        if(node == null)
            return;
        if(this.isClient()) {
            this.send(FancyPacketMap.flag("quickInsert"));
            return;
        }
        try(Transaction transaction = Transaction.openRoot()) {
            ResourceHandler<ItemResource> storage = node.getStorage();
            ResourceHandler<ItemResource> inventory = PlayerInventoryWrapper.of(this.getPlayer()).getMainSlots();
            for(int i = 0; i < inventory.size(); ++i) {
                ItemResource resource = inventory.getResource(i);
                if(!resource.isEmpty()) {
                    int available = inventory.getAmountAsInt(i);
                    try(Transaction tx = Transaction.open(transaction)) {
                        int inserted = storage.insert(resource,available,tx);
                        if(inserted > 0) {
                            int taken = inventory.extract(i,resource,inserted,tx);
                            if(inserted == taken)
                                tx.commit();
                        }
                    }
                }
            }
            transaction.commit();
        }
    }

    public void quickExtract() {
        ItemStorageNode node = this.getNode(ItemStorageNode.TYPE);
        if(node == null)
            return;
        if(this.isClient()) {
            this.send(FancyPacketMap.flag("quickExtract"));
            return;
        }
        try(Transaction transaction = Transaction.openRoot()) {
            ResourceHandler<ItemResource> storage = node.getStorage();
            ResourceHandler<ItemResource> inventory = PlayerInventoryWrapper.of(this.getPlayer()).getMainSlots();
            for(int i = 0; i < storage.size(); ++i) {
                ItemResource resource = storage.getResource(i);
                int count = storage.getAmountAsInt(i);
                try(Transaction tx = Transaction.open(transaction)) {
                    int inserted = inventory.insert(resource,count,tx);
                    if(inserted > 0) {
                        int extracted = storage.extract(i,resource,inserted,tx);
                        if(extracted == inserted)
                            tx.commit();
                    }
                }
                //If the resource in that slot changed then the storage slots shifted, and thus we should decrement the index
                ItemResource newResource = storage.getResource(i);
                if(!newResource.equals(resource))
                    i--;
            }
            transaction.commit();
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
            this.onItemClick(entry.getInt("slot"),entry.getInt("button"),entry.getBoolean("shift"));
        }
        if(packet.contains("quickInsert"))
            this.quickInsert();
        if(packet.contains("quickExtract"))
            this.quickExtract();
    }

}