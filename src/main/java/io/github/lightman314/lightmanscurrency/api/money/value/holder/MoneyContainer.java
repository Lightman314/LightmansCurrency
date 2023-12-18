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
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MoneyContainer extends SimpleContainer implements IMoneyHolder {

    private MoneyView cachedValue = null;
    private final List<Object> knowsLatest = new ArrayList<>();

    private Container cachedContents;

    private final Player player;

    public MoneyContainer(@Nonnull Player player, int size) { super(size); this.player = player; this.cachedContents = new SimpleContainer(size); }

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

}
