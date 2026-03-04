package io.github.lightman314.lightmanscurrency.api.settings;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.NodeSelections;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextData;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface ISaveableSettingsHolder extends IClientTracker {

    List<SettingsNode> getAllSettingNodes();
    @Nullable
    SettingsNode getSettingsNode(String nodeKey);

    @Nullable
    Component getName();

    void buildLoadContext(LoadContext.Builder builder);

    default NodeSelections defaultNodeSelections(@Nullable Player player) {
        NodeSelections selections = new NodeSelections();
        for(SettingsNode node : this.getAllSettingNodes())
            node.applyDefaultSelections(selections,player);
        return selections;
    }

    default SavedSettingData saveSettings() { return this.saveSettings(null,this.defaultNodeSelections(null)); }
    default SavedSettingData saveSettings(@Nullable Player player, NodeSelections selections)
    {
        HolderLookup.Provider lookup = player == null ? LookupHelper.getRegistryAccess() : player.registryAccess();
        SavedSettingData.Mutable builder = SavedSettingData.EMPTY.makeMutable(lookup);
        for(SettingsNode node : this.getAllSettingNodes())
        {
            if(selections.nodeSelected(node.key) && node.allowSaving(player))
            {
                node.saveSettings(builder.getNode(node.key));
                for(SettingsSubNode<?> subNode : node.getSubNodes())
                {
                    if(selections.subNodeSelected(node.key,subNode.getSubKey()) && subNode.allowSaving(player))
                        subNode.saveSettings(builder.getNode(subNode.getFullKey()));
                }
            }
        }
        return builder.makeImmutable();
    }

    default PrettyTextData writePrettySettings(SavedSettingData data, HolderLookup.Provider lookup) { return this.writePrettySettings(data,DataContext.createNBT(lookup)); }
    default PrettyTextData writePrettySettings(SavedSettingData data, DataContext<Tag> context) {
        List<Component> text = new ArrayList<>();
        List<SettingsNode> allNodes = this.getAllSettingNodes();
        allNodes.sort(SettingsNode.SORTER);
        data.withContext(context);
        for(SettingsNode node : allNodes)
        {
            if(data.hasNode(node.key))
            {
                node.writeAsText(data,text::add);
                for(SettingsSubNode<?> subNode : node.getSubNodes())
                {
                    if(data.hasNode(subNode.getFullKey()))
                        subNode.writeAsText(data,text::add);
                }
            }
        }
        return new PrettyTextData(LCText.DATA_NAME_FORMAT.get(this.getName()), ImmutableList.copyOf(text));
    }

    default void loadSettings(Player player, SavedSettingData data, NodeSelections selections)
    {
        //Create load context
        LoadContext.Builder contextBuilder = LoadContext.builder(player,this);
        this.buildLoadContext(contextBuilder);
        LoadContext context = contextBuilder.build();
        data.withContext(context.dataContext());
        List<SettingsNode> allNodes = new ArrayList<>(this.getAllSettingNodes());
        //Sort nodes by priority
        allNodes.sort(SettingsNode.SORTER);
        //Load nodes in order
        for(SettingsNode node : allNodes)
        {
            if(selections.nodeSelected(node.key) && data.hasNode(node.key) && node.allowLoading(context))
            {
                node.loadSettings(data.getNode(node.key),context);
                for(SettingsSubNode<?> subNode : node.getSubNodes())
                {
                    if(selections.subNodeSelected(node.key,subNode.getSubKey()) && data.hasNode(subNode.getFullKey()) && subNode.allowLoading(context))
                        subNode.loadSettings(data.getNode(subNode.getFullKey()),context);
                }
            }
        }
    }

}
