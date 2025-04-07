package io.github.lightman314.lightmanscurrency.api.easy_data;

import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IEasyDataHost {

    @Nullable
    Consumer<Notification> dataChangeNotifier();

    void registerData(EasyData<?> data);
    void onDataChanged(EasyData<?> data);

    OwnerData getOwner();

}
