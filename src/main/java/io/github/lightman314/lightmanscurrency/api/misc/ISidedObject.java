package io.github.lightman314.lightmanscurrency.api.misc;

import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An extension of {@link IClientTracker} that makes available methods to flag the object as exising on the logical server or client<br>
 * Normally not necessary, as most objects have their own ways of tracking which side they're on, but is useful for data that can be saved to an item stack via the {@link io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyHandler} capabilty<br>
 * and any objects that attempt to get a {@link io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler IMoneyHandler} capability from an item should check for this interface
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface ISidedObject extends IClientTracker {

    Object flagAsClient();
    Object flagAsClient(boolean isClient);
    Object flagAsClient(IClientTracker tracker);

}