package io.github.lightman314.lightmanscurrency.api.ownership.holder;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.FakeOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import net.minecraft.network.chat.Component;

public final class LockedOwnerHolder implements IOwnerHolder {

    private final Owner owner;
    private final ISidedContext context;
    public LockedOwnerHolder(Owner owner, ISidedContext context) {
        this.owner = owner;
        this.context = context;
    }
    public LockedOwnerHolder(Component fakeOwnerName, ISidedContext context) {
        this(FakeOwner.of(fakeOwnerName),context);
    }

    @Override
    public Owner getValidOwner() { return this.owner; }

    @Override
    public boolean isClient() { return this.context.isClient(); }
}