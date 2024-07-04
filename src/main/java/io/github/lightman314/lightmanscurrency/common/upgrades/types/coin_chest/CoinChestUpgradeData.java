package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class CoinChestUpgradeData {

    public static final CoinChestUpgradeData NULL = new CoinChestUpgradeData(ItemStack.EMPTY, new CoinChestUpgrade() {
        @Override
        public void HandleMenuMessage(@Nonnull CoinChestMenu menu, @Nonnull CoinChestUpgradeData data, @Nonnull LazyPacketData message) {}

        @Override
        public void addClientTabs(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen, @Nonnull Consumer<Object> consumer) { }
    }, -1, () -> {});

    public final int slot;
    private final boolean ticks;
    private int tickTimer = 0;
    private final ItemStack stack;
    public Item getItem() { return this.stack.getItem(); }
    public final CoinChestUpgrade upgrade;
    private final Runnable onChange;
    private CoinChestUpgradeData(@Nonnull ItemStack stack, @Nonnull CoinChestUpgrade upgrade, int slot, @Nonnull Runnable onChange)
    {
        this.stack = stack;
        this.upgrade = upgrade;
        this.slot = slot;
        this.onChange = onChange;
        if(this.upgrade instanceof TickableCoinChestUpgrade tickable)
        {
            this.ticks = true;
            this.tickTimer = tickable.getTickFrequency();
        }
        else
            this.ticks = false;
    }

    public boolean isNull() { return this == NULL; }

    public boolean notNull() { return !this.isNull(); }

    public void copyRelevantData(@Nonnull CoinChestUpgradeData other)
    {
        if(other.upgrade == this.upgrade)
            this.tickTimer = other.tickTimer;
    }

    @Nonnull
    public <T> T getData(@Nonnull Supplier<DataComponentType<T>> type,@Nonnull T defaultValue) { return this.stack.getOrDefault(type,defaultValue); }
    public <T> void setData(@Nonnull Supplier<DataComponentType<T>> type, @Nonnull T data)
    {
        this.stack.set(type,data);
        this.onChange.run();
    }
    public <T> void editData(@Nonnull Supplier<DataComponentType<T>> type, @Nonnull T defaultValue, @Nonnull UnaryOperator<T> editor)
    {
        T data = this.getData(type,defaultValue);
        data = editor.apply(data);
        this.setData(type,data);
    }

    @Nonnull
    public UpgradeData getUpgradeData() { return UpgradeItem.getUpgradeData(this.stack); }

    @Nonnull
    public static CoinChestUpgradeData forItem(@Nonnull ItemStack stack, int slot, @Nonnull Runnable onChange)
    {
        if(stack.getItem() instanceof UpgradeItem item && item.getUpgradeType() instanceof CoinChestUpgrade upgrade)
            return new CoinChestUpgradeData(stack, upgrade, slot, onChange);
        return NULL;
    }

    public void tick(@Nonnull CoinChestBlockEntity be) {
        if(this.ticks && this.upgrade instanceof TickableCoinChestUpgrade tickable)
        {
            if(--this.tickTimer <= 0)
            {
                this.tickTimer = tickable.getTickFrequency();
                try{ tickable.OnServerTick(be, this);
                }catch(Throwable t) { LightmansCurrency.LogError("Error ticking a Tickable CoinChestUpgrade", t); }
            }
        }
    }

}
