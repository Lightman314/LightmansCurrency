package io.github.lightman314.lightmanscurrency.api.taxes;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.misc.world.WorldPosition;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

/**
 * Interface that should be applied to all machines that can be taxed.<br>
 */
public interface ITaxable extends IClientTracker {

    /**
     * The name of the taxable machine.
     */
    MutableComponent getName();

    /**
     * A {@link TaxableReference} that will point to this machine.<br>
     * Said TaxableReference should always point to this machine unless/until it has been removed, destroyed,
     * or otherwise modified in such a way that it cannnot be said to be the same machine.
     */
    TaxableReference getReference();

    /**
     * The {@link WorldPosition} of this machine.<br>
     * Used by the {@link ITaxCollector} to determine if this machine is within its tax collection area.
     */
    WorldPosition getWorldPosition();

    /**
     * Whether this taxable should be targeted by the server tax if it's "Only Target Network Traders" toggle is enabled<br>
     * Defaults to false if not implemented
     */
    default boolean isNetworkAccessible() { return false; }

    /**
     * A method with which to manually post a notification to this machines logger (if one is present).
     */
    void pushNotification(Supplier<Notification> notification);

}