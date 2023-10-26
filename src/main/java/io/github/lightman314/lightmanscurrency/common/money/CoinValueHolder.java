package io.github.lightman314.lightmanscurrency.common.money;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public class CoinValueHolder {

    private final Runnable onChange;
    private CoinValue value = CoinValue.EMPTY;
    public CoinValueHolder() { this.onChange = () -> {}; }
    public CoinValueHolder(@Nonnull Runnable onChange) { this.onChange = onChange; }

    @Nonnull
    public CoinValue getValue() { return this.value; }
    public void setValue(@Nonnull CoinValue value) { this.value = value; this.setChanged(); }

    public void clear() { this.setValue(CoinValue.EMPTY); }

    public void addValue(@Nonnull CoinValue value) { this.value = this.value.plusValue(value); this.setChanged(); }
    public void removeValue(@Nonnull CoinValue value) { this.value = this.value.minusValue(value); this.setChanged(); }

    protected void setChanged() { this.onChange.run(); }

    @Nonnull
    public CompoundTag save() { return this.value.save(); }
    public void load(@Nonnull CompoundTag tag) { this.value = CoinValue.load(tag); }
    public void safeLoad(@Nonnull CompoundTag tag, @Nonnull String key) { this.value = CoinValue.safeLoad(tag, key); }

}