package io.github.lightman314.lightmanscurrency.common.taxes;

import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.taxes.data.WorldPosition;
import io.github.lightman314.lightmanscurrency.common.taxes.reference.TaxableReference;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.util.NonNullSupplier;

public interface ITaxable {

    MutableComponent getName();

    TaxableReference getReference();

    WorldPosition getWorldPosition();

    void pushNotification(NonNullSupplier<Notification> notification);

}
