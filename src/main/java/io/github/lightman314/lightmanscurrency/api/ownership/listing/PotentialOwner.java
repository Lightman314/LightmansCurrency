package io.github.lightman314.lightmanscurrency.api.ownership.listing;

import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class PotentialOwner implements IClientTracker {

    private final Owner owner;

    private boolean isClient;

    @Override
    public final boolean isClient() { return this.isClient; }

    public final void flagAsClient() { this.isClient = true; }
    private int priority;
    private boolean currentOwner;
    protected final void setPriority(int priority) { this.priority = priority; }
    public final void addPriority(int priority) { this.priority += priority; }
    public final int getPriority() { return this.currentOwner ? Integer.MAX_VALUE - 1 : this.priority; }
    public final int sortingPriority() { return this.getPriority() * -1; }
    public final void flagAsHighPriority() { this.priority = Integer.MAX_VALUE - 2; }
    public final void setAsCurrentOwner(boolean isCurrentOwner) { this.currentOwner = isCurrentOwner; }

    protected PotentialOwner(Owner owner) { this(owner,0); }
    protected PotentialOwner(Owner owner, int priority) { this.owner = owner; this.owner.setParent(this); this.priority = priority; }

    public final Owner asOwner() { return this.owner; }

    public boolean failedFilter(String searchFilter) { return !this.getName().getString().toLowerCase().contains(searchFilter.toLowerCase()); }

    public MutableComponent getName() { return this.asOwner().getName(); }

    public abstract IconData getIcon();

    public abstract void appendTooltip(List<Component> tooltip);

}
