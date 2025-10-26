package io.github.lightman314.lightmanscurrency.api.settings;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.NodeSelections;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public abstract class SettingsNode implements IClientTracker {

    public static final Comparator<SettingsNode> SORTER = Comparator
            .comparingInt(SettingsNode::invPriority)
            .thenComparing(n -> n.getName().getString(),String::compareToIgnoreCase);

    public final String key;
    private final int priority;
    protected final ISaveableSettingsHolder host;
    public SettingsNode(String key, ISaveableSettingsHolder host) { this(key,host,0); }
    public SettingsNode(String key, ISaveableSettingsHolder host, int priority) {
        this.key = key;
        this.host = host;
        this.priority = priority;
    }

    @Override
    public final boolean isClient() { return this.host.isClient(); }
    @Override
    public final boolean isServer() { return this.host.isServer(); }

    public abstract MutableComponent getName();

    public final int priority() { return this.priority; }
    public final int invPriority() { return -1 * this.priority; }

    public boolean allowSelecting(@Nullable Player player) { return true; }
    public boolean allowSaving(@Nullable Player player) { return true; }
    public abstract boolean allowLoading(LoadContext context);

    public void applyDefaultSelections(NodeSelections selections, @Nullable Player player)
    {
        if(!this.allowSelecting(player))
            return;
        selections.setNodeSelected(this.key,true);
        for(SettingsSubNode<?> subNode : this.getSubNodes())
            selections.addSubNode(this.key,subNode.getSubKey());
    }

    @Nullable
    public SettingsSubNode<?> getSubNode(String subNodeKey)
    {
        for(SettingsSubNode<?> node : this.getSubNodes())
        {
            if(node.getSubKey().equals(subNodeKey))
                return node;
        }
        return null;
    }

    public List<SettingsSubNode<?>> getSubNodes() { return new ArrayList<>(); }

    public abstract void saveSettings(SavedSettingData.MutableNodeAccess data);

    public abstract void loadSettings(SavedSettingData.NodeAccess data, LoadContext context);

    public void writeAsText(SavedSettingData data, Consumer<Component> lineWriter)
    {
        if(data.hasNode(this.key))
        {
            lineWriter.accept(formatTitle(this.getName()));
            this.writeLines(data.getNode(this.key),lineWriter);
        }
    }

    public static MutableComponent formatTitle(MutableComponent title) { return EasyText.empty().append(title.withStyle(ChatFormatting.BOLD)); }

    public static MutableComponent formatEntry(Component label, boolean value) { return formatEntry(label,LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.get(value).get()); }
    public static MutableComponent formatEntry(Component label, String value) { return formatEntry(label, EasyText.literal(value)); }
    public static MutableComponent formatEntry(Component label, int value) { return formatEntry(label, String.valueOf(value)); }
    public static MutableComponent formatEntry(Component label, float value) { return formatEntry(label, String.valueOf(value)); }
    public static MutableComponent formatEntry(Component label, Component value) { return LCText.DATA_ENTRY_LABEL.get(label,value); }

    protected abstract void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter);

}
