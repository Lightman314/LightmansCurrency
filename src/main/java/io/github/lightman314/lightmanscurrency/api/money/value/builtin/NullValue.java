package io.github.lightman314.lightmanscurrency.api.money.value.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.NullCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

public class NullValue extends MoneyValue {

    public static final NullValue EMPTY = new NullValue(false);
    public static final NullValue FREE = new NullValue(true);

    private final boolean free;
    private NullValue(boolean free) { this.free = free; }

    @Override
    public CurrencyType<?> getType() { return NullCurrencyType.INSTANCE; }

    @Override
    protected String generateUniqueName() { return this.free ? "null!free" : "null!empty"; }
    @Override
    public boolean isFree() { return this.free; }
    @Override
    public boolean isValidPrice() { return this.free; }
    @Override
    public boolean isEmpty() { return true; }
    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCoreValue() { return 0; }

    @Override
    public Component getText(Component emptyText) { return this.free ? LCText.GUI_MONEY_VALUE_FREE.get() : emptyText; }
    @Override
    public MoneyValue addValue(MoneyValue addedValue) { return addedValue; }
    @Override
    public MoneyValue multiplyValue(double multiplier) { return this; }
    @Override
    public boolean containsValue(MoneyValue queryValue) { return queryValue.isFree() || queryValue.isEmpty(); }
    @Override
    public MoneyValue subtractValue(MoneyValue removedValue) { return removedValue.isFree() || removedValue.isEmpty() ? this : null; }
    @Override
    public MoneyValue percentageOfValue(int percentage, boolean roundUp) { return FREE; }
    @Override
    public List<ItemStack> onBlockBroken(OwnerData owner) { return new ArrayList<>(); }
    @Override
    public MoneyValue getSmallestValue() { return this; }
    @Override
    public MoneyValue fromCoreValue(long value) { return this; }
    @Override
    public String toString() { return "NullMoneyValue:"+ (this.free ? "Free" : "Empty"); }
}
