package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.ISettingsStorageIO;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public interface ISettingsStorageIONode extends ISettingsStorageIO {

    default TraderNodeType<?> getType() { return ((TraderNode)this).getType(); }
    @Override
    default DualKey getSettingsKey() { return DualKey.create(this.getType()); }
    @Override
    default Component getSettingsName() { return TextEntry.traderNode(this.getType()).get(); }

    default List<ISettingsStorageIO> getEntries(Player player) { return List.of(this); }

    static List<ISettingsStorageIO> getSettingsIO(INodeAccess trader,Player player) {
        List<ISettingsStorageIO> result = new ArrayList<>();
        for(ISettingsStorageIONode node : trader.getNodes(ISettingsStorageIONode.class))
            result.addAll(node.getEntries(player));
        //Sort the results so that high-priority loaders load first
        result.sort(ISettingsStorageIO.SORTER);
        return result;
    }

    interface PriorityLoad extends ISettingsStorageIONode, ISettingsStorageIO.PriorityLoad {}

}