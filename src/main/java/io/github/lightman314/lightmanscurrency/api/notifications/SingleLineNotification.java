package io.github.lightman314.lightmanscurrency.api.notifications;

import com.google.common.collect.Lists;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import java.util.List;

@MethodsReturnNonnullByDefault
public abstract class SingleLineNotification extends Notification {

    @Override
    public final List<Component> getMessageLines() { return Lists.newArrayList(this.getMessage()); }

    protected abstract Component getMessage();

}