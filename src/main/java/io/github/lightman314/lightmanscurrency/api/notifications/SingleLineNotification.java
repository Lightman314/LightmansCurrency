package io.github.lightman314.lightmanscurrency.api.notifications;

import com.google.common.collect.Lists;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

@MethodsReturnNonnullByDefault
public abstract class SingleLineNotification extends Notification {

    @Override
    public final List<MutableComponent> getMessageLines() { return Lists.newArrayList(this.getMessage()); }

    protected abstract MutableComponent getMessage();

}
