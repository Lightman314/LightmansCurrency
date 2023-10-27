package io.github.lightman314.lightmanscurrency.common.menus;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CoinChestMenu extends LazyMessageMenu {

    public final CoinChestBlockEntity be;


    private final List<CoinSlot> coinSlots;
    private final List<UpgradeInputSlot> upgradeSlots;
    private final List<SimpleSlot> inventorySlots;

    public CoinChestMenu(int id, Inventory inventory, CoinChestBlockEntity be) {
        super(ModMenus.COIN_CHEST.get(), id, inventory);
        this.be = be;
        this.be.startOpen(this.player);

        this.addValidator(BlockEntityValidator.of(be));
        this.addValidator(this.be::allowAccess);

        //Chest Slots
        List<CoinSlot> cSlots = new ArrayList<>();
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                CoinSlot s = new CoinSlot(this.be.getStorage(), x + 9 * y, 8 + x * 18, 93 + y * 18, true);
                this.addSlot(s);
                cSlots.add(s);
            }
        }
        this.coinSlots = ImmutableList.copyOf(cSlots);

        //Upgrade Slots
        List<UpgradeInputSlot> uSlots = new ArrayList<>();
        for(int y = 0; y < CoinChestBlockEntity.UPGRADE_SIZE; ++y)
        {
            final int index = y;
            UpgradeInputSlot s = new UpgradeInputSlot(this.be.getUpgrades(), y, 152, 21 + y * 18, this.be);
            s.setListener(() -> this.be.checkUpgradeEquipped(index));
            this.addSlot(s);
            uSlots.add(s);
        }
        this.upgradeSlots = ImmutableList.copyOf(uSlots);

        //Player inventory
        List<SimpleSlot> iSlots = new ArrayList<>();
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                SimpleSlot s = new SimpleSlot(inventory, x + y * 9 + 9, 8 + x * 18, 161 + y * 18);
                iSlots.add(s);
                this.addSlot(s);
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++)
        {
            SimpleSlot s = new SimpleSlot(inventory, x, 8 + x * 18, 219);
            iSlots.add(s);
            this.addSlot(s);
        }
        this.inventorySlots = ImmutableList.copyOf(iSlots);

    }

    public void SetUpgradeSlotVisibility(boolean visible)
    {
        for(UpgradeInputSlot slot : this.upgradeSlots)
            slot.active = visible;
    }

    public void SetCoinSlotVisibility(boolean visible)
    {
        for(CoinSlot slot : this.coinSlots)
            slot.active = visible;
    }

    public void SetInventoryVisibility(boolean visible)
    {
        for(SimpleSlot slot : this.inventorySlots)
            slot.active = visible;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index)
    {

        ItemStack clickedStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            clickedStack = slotStack.copy();
            //Move items from coin/upgrade slots into the inventory
            if(index < CoinChestBlockEntity.STORAGE_SIZE + CoinChestBlockEntity.UPGRADE_SIZE)
            {
                if(!this.moveItemStackTo(slotStack, CoinChestBlockEntity.STORAGE_SIZE + CoinChestBlockEntity.UPGRADE_SIZE, this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            //Move items from the inventory into the coin/upgrade slots
            else if(!this.moveItemStackTo(slotStack, 0, CoinChestBlockEntity.STORAGE_SIZE + CoinChestBlockEntity.UPGRADE_SIZE, false))
            {
                return ItemStack.EMPTY;
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
    public void removed(@Nonnull Player player) { super.removed(player); this.be.stopOpen(player); }

    private Consumer<LazyPacketData> extraHandler = d -> {};
    public final void AddExtraHandler(@Nonnull Consumer<LazyPacketData> extraHandler) { this.extraHandler = extraHandler; }

    @Override
    public void HandleMessage(LazyPacketData message) {
        this.extraHandler.accept(message);
        for(CoinChestUpgradeData data : this.be.getChestUpgrades())
            data.upgrade.HandleMenuMessage(this, data, message);
    }

}
