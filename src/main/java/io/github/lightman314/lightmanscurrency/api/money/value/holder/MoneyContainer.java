package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class MoneyContainer implements Container, IMoneyHolder {

    private MoneyView cachedValue = null;
    private final List<Object> knowsLatest = new ArrayList<>();

    private Container cachedContents;
    private final Container container;

    private final Player player;

    public MoneyContainer(@Nonnull Player player, int size) { this(player, new SimpleContainer(size)); }
    public MoneyContainer(@Nonnull Player player, @Nonnull Container container) {
        this.player = player;
        this.container = container;
        this.cachedContents = InventoryUtil.copyInventory(this.container);
    }

    //IMoneyHolder
    @Nonnull
    @Override
    public MoneyView getStoredMoney() {
        if(this.cachedValue == null || this.hasStoredMoneyChanged())
        {
            this.cachedContents = InventoryUtil.copyInventory(this);
            this.cachedValue = MoneyAPI.valueOfContainer(this);
        }
        return this.cachedValue;
    }

    @Override
    public boolean hasStoredMoneyChanged(@Nullable Object context) { return this.cachedValue == null || this.hasStoredMoneyChanged() || (context != null && !this.knowsLatest.contains(context)); }

    protected boolean hasStoredMoneyChanged() { return !InventoryUtil.ContainerMatches(this, this.cachedContents); }

    @Override
    public int priority() { return 100; }

    @Nonnull
    @Override
    public MoneyValue tryAddMoney(@Nonnull MoneyValue valueToAdd) {
        if(MoneyAPI.addMoneyToContainer(this, this.player, valueToAdd))
            return MoneyValue.empty();
        return valueToAdd;
    }

    @Nonnull
    @Override
    public MoneyValue tryRemoveMoney(@Nonnull MoneyValue valueToRemove) {
        MoneyView storedMoney = this.getStoredMoney();
        MoneyValue actualRemove = storedMoney.capValue(valueToRemove);
        if(MoneyAPI.takeMoneyFromContainer(this, this.player, actualRemove))
            return valueToRemove.subtractValue(actualRemove);
        return valueToRemove;
    }

    @Override
    public void flagAsKnown(@Nullable Object context) {
        if(context != null && !this.knowsLatest.contains(context))
            this.knowsLatest.add(context);
    }

    @Override
    public void forgetContext(@Nonnull Object context) { this.knowsLatest.remove(context); }

    @Override
    public Component getTooltipTitle() { return EasyText.translatable("tooltip.lightmanscurrency.trader.info.money.slots"); }

    //Container
    @Override
    public int getContainerSize() { return this.container.getContainerSize(); }
    @Override
    public boolean isEmpty() { return this.container.isEmpty(); }
    @Nonnull
    @Override
    public ItemStack getItem(int slot) { return this.container.getItem(slot); }
    @Nonnull
    @Override
    public ItemStack removeItem(int slot, int count) { return this.container.removeItem(slot,count); }
    @Nonnull
    @Override
    public ItemStack removeItemNoUpdate(int slot) { return this.container.removeItemNoUpdate(slot); }
    @Override
    public void setItem(int slot, @Nonnull ItemStack stack) { this.container.setItem(slot,stack); }
    @Override
    public int getMaxStackSize() { return this.container.getMaxStackSize(); }
    @Override
    public void setChanged() { this.container.setChanged(); }
    @Override
    public boolean stillValid(@Nonnull Player player) { return this.container.stillValid(player); }
    @Override
    public void startOpen(@Nonnull Player player) { this.container.startOpen(player); }
    @Override
    public void stopOpen(@Nonnull Player player) { this.container.stopOpen(player); }
    @Override
    public boolean canPlaceItem(int slot, @Nonnull ItemStack stack) { return this.container.canPlaceItem(slot,stack); }
    @Override
    public boolean canTakeItem(@Nonnull Container container, int slot, @Nonnull ItemStack stack) { return this.container.canTakeItem(container, slot, stack); }
    @Override
    public int countItem(@Nonnull Item item) { return this.container.countItem(item); }
    @Override
    public boolean hasAnyOf(@Nonnull Set<Item> set) { return this.container.hasAnyOf(set); }
    @Override
    public boolean hasAnyMatching(@Nonnull Predicate<ItemStack> predicate) { return this.container.hasAnyMatching(predicate); }
    @Override
    public void clearContent() { this.container.clearContent(); }
}
