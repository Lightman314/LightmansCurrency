package io.github.lightman314.lightmanscurrency.api.settings;

import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class SettingsSubNode<T extends SettingsNode> implements IClientTracker {

    protected final T parent;
    public SettingsSubNode(T parent) { this.parent = parent; }

    @Override
    public final boolean isClient() { return this.parent.isClient(); }
    @Override
    public final boolean isServer() { return this.parent.isServer(); }

    public final String getFullKey() { return this.parent.key + "." + this.getSubKey(); }
    public abstract String getSubKey();
    public abstract MutableComponent getName();

    public boolean allowSelecting(@Nullable Player player) { return true; }
    public boolean allowSaving(@Nullable Player player) { return true; }
    public abstract boolean allowLoading(LoadContext context);

    public abstract void saveSettings(SavedSettingData.MutableNodeAccess data);

    public abstract void loadSettings(SavedSettingData.NodeAccess data, LoadContext context);

    public void writeAsText(SavedSettingData data, Consumer<Component> lineWriter)
    {
        if(data.hasNode(this.getFullKey()))
        {
            lineWriter.accept(SettingsNode.formatTitle(this.getName()));
            this.writeLines(data.getNode(this.getFullKey()),lineWriter);
        }
    }

    protected abstract void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter);

}
