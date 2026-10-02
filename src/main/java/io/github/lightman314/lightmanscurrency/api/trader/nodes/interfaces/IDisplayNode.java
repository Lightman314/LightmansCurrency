package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface IDisplayNode {

    @Nullable
    default Component getCustomName() { return null; }
    @Nullable
    default Component getDefaultName() { return null; }
    @Nullable
    default IconData getCustomIcon() { return null; }
    @Nullable
    default IconData getDefaultIcon() { return null; }
    default Optional<Integer> getNameColor() { return Optional.empty(); }
    default void appendTerminalText(@Nullable Player player,Consumer<Component> builder) { }

    @Nullable
    static Component getCustomTraderName(INodeAccess trader) {
        for(IDisplayNode node : trader.getNodes(IDisplayNode.class)) {
            Component name = node.getCustomName();
            if(name != null)
                return name;
        }
        return null;
    }

    static Component getTraderName(INodeAccess trader) {
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
        TraderData t = trader.getTrader();
        Identifier type = LCApi.id("null");
        if(t != null)
            type = t.getType().getKey();
        return Component.translatable(toTranslationKey(type));
    }

    static Optional<Integer> getTraderNameColor(INodeAccess trader) {
        for(IDisplayNode node : trader.getNodes(IDisplayNode.class)) {
            Optional<Integer> color = node.getNameColor();
            if(color.isPresent())
                return color;
        }
        return Optional.empty();
    }

    static Optional<IconData> getCustomTraderIcon(INodeAccess trader) {
        for(IDisplayNode node : trader.getNodes(IDisplayNode.class))
        {
            IconData icon = node.getCustomIcon();
            if(icon != null && !icon.isEmpty())
                return Optional.of(icon);
        }
        return Optional.empty();
    }

    static Optional<IconData> getDefaultTraderIcon(INodeAccess trader) {
        for(IDisplayNode node : trader.getNodes(IDisplayNode.class))
        {
            IconData icon = node.getDefaultIcon();
            if(icon != null && !icon.isEmpty())
                return Optional.of(icon);
        }
        return Optional.empty();
    }

    static IconData getTraderIcon(INodeAccess trader) {
        List<IDisplayNode> nodes = trader.getNodes(IDisplayNode.class);
        for(IDisplayNode node : nodes)
        {
            IconData icon = node.getCustomIcon();
            if(icon != null && !icon.isEmpty())
                return icon;
        }
        for(IDisplayNode node : nodes)
        {
            IconData icon = node.getDefaultIcon();
            if(icon != null && !icon.isEmpty())
                return icon;
        }
        return ItemIcon.of(LCItems.TRADING_CORE);
    }

    static List<Component> getTerminalInfo(@Nullable Player player,INodeAccess trader) {
        List<Component> tooltip = new ArrayList<>();
        for(IDisplayNode node : trader.getNodes(IDisplayNode.class))
            node.appendTerminalText(player,tooltip::add);
        return tooltip;
    }

    static String toTranslationKey(Identifier type) { return type.getNamespace() + ".gui.trader.default_name." + type.getPath(); }

}