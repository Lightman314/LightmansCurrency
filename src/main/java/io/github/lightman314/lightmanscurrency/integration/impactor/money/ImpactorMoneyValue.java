package io.github.lightman314.lightmanscurrency.integration.impactor.money;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.integration.impactor.LCImpactorCompat;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.kyori.adventure.key.Key;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImpactorMoneyValue extends MoneyValue {

    private final Currency currency;
    public Currency getImpactorCurrency() { return this.currency; }
    private final BigDecimal value;
    public BigDecimal getValue() { return this.value; }

    private ImpactorMoneyValue(Currency currency, BigDecimal value) {
        this.currency = Objects.requireNonNull(currency,"Currency Type must not be null!");
        this.value = value;
    }

    @Nonnull
    @Override
    protected String generateUniqueName() { return this.generateCustomUniqueName(this.currency.key().toString()); }

    @Nonnull
    @Override
    protected ResourceLocation getType() { return ImpactorCurrencyType.TYPE; }

    @Override
    public boolean isEmpty() { return this.getCoreValue() <= 0; }

    private static BigDecimal getDecimalNullifier(Currency currency) {
        int decimals = currency.decimals();
        int result = 1;
        while(decimals-- > 0)
            result *= 10;
        return BigDecimal.valueOf(result);
    }

    @Override
    public long getCoreValue() { return Math.max(0,this.value.multiply(getDecimalNullifier(this.currency)).longValue()); }

    @Override
    public MutableComponent getText(@Nonnull MutableComponent emptyText) {
        if(this.isEmpty())
            return emptyText;
        return LCImpactorCompat.convertComponent(this.currency.format(this.value));
    }

    @Override
    public MoneyValue addValue(@Nonnull MoneyValue addedValue) {
        if(addedValue instanceof ImpactorMoneyValue other && other.getImpactorCurrency().key().equals(this.currency.key()))
            return of(this.currency,this.value.add(other.value));
        return null;
    }

    @Override
    public boolean containsValue(@Nonnull MoneyValue queryValue) {
        if(queryValue instanceof ImpactorMoneyValue other)
            return this.value.doubleValue() >= other.value.doubleValue();
        return false;
    }

    @Override
    public MoneyValue subtractValue(@Nonnull MoneyValue removedValue) {
        if(removedValue instanceof ImpactorMoneyValue other && other.getImpactorCurrency().key().equals(this.currency.key()))
            return of(this.currency,this.value.subtract(other.value));
        return null;
    }

    @Override
    public MoneyValue percentageOfValue(int percentage, boolean roundUp) {
        if(percentage == 100)
            return this;
        if(percentage == 0)
            return MoneyValue.free();
        BigDecimal mult = BigDecimal.valueOf(percentage).divide(BigDecimal.valueOf(100),MathContext.UNLIMITED);
        BigDecimal newValue = this.value.multiply(mult);
        return of(this.currency,newValue);
    }

    @Nonnull
    @Override
    public MoneyValue multiplyValue(double multiplier) {
        BigDecimal mult = BigDecimal.valueOf(multiplier);
        BigDecimal newValue = this.value.multiply(mult);
        return of(this.currency,newValue);
    }

    @Nonnull
    @Override
    public List<ItemStack> onBlockBroken(@Nonnull OwnerData owner) {
        PlayerReference player = owner.getPlayerForContext();
        Account account = LCImpactorCompat.getPlayerAccount(player.id,this.currency);
        if(account != null)
            account.deposit(this.value);
        return new ArrayList<>();
    }

    @Nonnull
    @Override
    public MoneyValue getSmallestValue() { return fromCoreValue(1); }

    @Nonnull
    @Override
    public MoneyValue fromCoreValue(long value) {
        BigDecimal result = BigDecimal.ONE.divide(getDecimalNullifier(this.currency),MathContext.UNLIMITED);
        return of(this.currency,result);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        tag.putString("Currency",this.currency.key().toString());
        tag.putString("Value",this.value.toString());
    }

    @Override
    protected void writeAdditionalToJson(@Nonnull JsonObject json) {
        json.addProperty("Currency",this.currency.key().toString());
        json.addProperty("Value",this.value);
    }

    @Nonnull
    @Override
    public DisplayEntry getDisplayEntry(@Nullable List<Component> additionalTooltips, boolean tooltipOverride) {
        return DisplayEntry.of(this.getText(EasyText.empty()),TextRenderUtil.TextFormatting.create().centered().middle());
    }

    public static MoneyValue load(CompoundTag tag) {
        Key currency = Key.key(tag.getString("Currency"),':');
        BigDecimal value = new BigDecimal(tag.getString("Value"));
        return of(currency,value);
    }

    public static MoneyValue load(JsonObject json) {
        Key currency = Key.key(GsonHelper.getAsString(json,"Currency"),';');
        BigDecimal value = GsonHelper.getAsBigDecimal(json,"Value");
        return of(currency,value);
    }

    public static MoneyValue of(Key currencyKey,BigDecimal value) { return of(EconomyService.instance().currencies().currency(currencyKey).orElse(null),value); }
    public static MoneyValue of(Currency currency, BigDecimal value) {
        if(currency == null)
            return MoneyValue.empty();
        if(value.doubleValue() <= 0)
            return empty();
        return new ImpactorMoneyValue(currency,value);
    }

}