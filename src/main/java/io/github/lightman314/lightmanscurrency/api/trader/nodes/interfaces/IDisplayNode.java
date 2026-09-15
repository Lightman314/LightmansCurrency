package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface IDisplayNode {

    @Nullable
    default Component getCustomName() { return null; }
    @Nullable
    default Component getDefaultName() { return null; }
    @Nullable
    default IconData getCustomIcon() { return null; }
    @Nullable
    default IconData getDefaultIcon() { return null; }

    @Nullable
    static Component getCustomTraderName(TraderData trader) {
        for(IDisplayNode node : trader.getNodes(IDisplayNode.class)) {
            Component name = node.getCustomName();
            if(name != null)
                return name;
        }
        return null;
    }

    static Component getTraderName(TraderData trader) {
        List<IDisplayNode> nodes = trader.getNodes(IDisplayNode.class);
        for(IDisplayNode node : nodes)
        {
            Component name = node.getCustomName();
            if(name != null)
                return name;
        }
        for(IDisplayNode node : nodes)
        {
            Component name = node.getDefaultName();
            if(name != null)
                return name;
        }
        Identifier type = LCRegistries.Trader.TRADER_TYPES.getKey(trader.getType());
        return Component.translatable(toTranslationKey(type));
    }


    static Optional<IconData> getCustomTraderIcon(TraderData trader) {
        for(IDisplayNode node : trader.getNodes(IDisplayNode.class))
        {
            IconData icon = node.getCustomIcon();
            if(icon != null)
                return Optional.of(icon);
        }
        return Optional.empty();
    }

    static Optional<IconData> getDefaultTraderIcon(TraderData trader) {
        for(IDisplayNode node : trader.getNodes(IDisplayNode.class))
        {
            IconData icon = node.getDefaultIcon();
            if(icon != null)
                return Optional.of(icon);
        }
        return Optional.empty();
    }

    static IconData getTraderIcon(TraderData trader) {
        List<IDisplayNode> nodes = trader.getNodes(IDisplayNode.class);
        for(IDisplayNode node : nodes)
        {
            IconData icon = node.getCustomIcon();
            if(icon != null)
                return icon;
        }
        for(IDisplayNode node : nodes)
        {
            IconData icon = node.getDefaultIcon();
            if(icon != null)
                return icon;
        }
        return ItemIcon.of(LCItems.TRADING_CORE);
    }

    static String toTranslationKey(Identifier type) { return type.getNamespace() + ".gui.trader.default_name." + type.getPath(); }

}