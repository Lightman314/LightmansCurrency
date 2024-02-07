package io.github.lightman314.lightmanscurrency.api.taxes;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.misc.world.WorldPosition;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

/**
 * Interface that should be applied to all machines that can be taxed.<br>
 */
public interface ITaxable extends IClientTracker {

    /**
     * The name of the taxable machine.
     */
    @Nonnull
    MutableComponent getName();

    /**
     * A {@link TaxableReference} that will point to this machine.<br>
     * Said TaxableReference should always point to this machine unless/until it has been removed, destroyed,
     * or otherwise modified in such a way that it cannnot be said to be the same machine.
     */
    @Nonnull
    TaxableReference getReference();

    /**
     * The {@link WorldPosition} of this machine.<br>
     * Used by the {@link ITaxCollector} to determine if this machine is within its tax collection area.
     */
    @Nonnull
    WorldPosition getWorldPosition();

    /**
     * A method with which to manually post a notification to this machines logger (if one is present).
     */
    void pushNotification(@Nonnull NonNullSupplier<Notification> notification);

}
