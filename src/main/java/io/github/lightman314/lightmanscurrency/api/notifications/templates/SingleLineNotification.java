package io.github.lightman314.lightmanscurrency.api.notifications.templates;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class SingleLineNotification extends Notification {

    @Override
    public final List<Component> getMessageLines() { return List.of(this.getMessage()); }

    protected abstract Component getMessage();

}
