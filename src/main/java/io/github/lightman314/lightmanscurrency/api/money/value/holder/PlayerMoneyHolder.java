package io.github.lightman314.lightmanscurrency.api.money.value.holder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated(since = "2.2.0.4")
public class PlayerMoneyHolder extends MoneyHolder.Slave {

    private final IMoneyHolder parent;

    public PlayerMoneyHolder(@Nonnull IMoneyHolder parent) { this.parent = parent; }
    @Nullable
    @Override
    protected IMoneyHolder getParent() { return this.parent; }

}
