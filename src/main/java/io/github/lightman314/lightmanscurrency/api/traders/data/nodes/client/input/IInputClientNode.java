package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.input;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.traders.client.TraderClientHooks;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public interface IInputClientNode {

    IconData getSettingsTabIcon(TraderData trader);
    Component getSettingsTabTooltip(TraderData trader);

    default List<? extends InputTabAddon> getInputSettingsAddons(TraderData trader) { return new ArrayList<>(); }

    static IconData lookupIcon(TraderData trader) {
        AtomicReference<IconData> result = new AtomicReference<>(ItemIcon.ofItem(Items.HOPPER));
        TraderClientHooks.forEach(trader,IInputClientNode.class,attachment -> result.set(attachment.getSettingsTabIcon(trader)));
        return result.get();
    }

    static Component lookupTooltip(TraderData trader) {
        AtomicReference<Component> result = new AtomicReference<>(LCText.TOOLTIP_TRADER_SETTINGS_INPUT_GENERIC.get());
        TraderClientHooks.forEach(trader,IInputClientNode.class,attachment -> result.set(attachment.getSettingsTabTooltip(trader)));
        return result.get();
    }

    static List<InputTabAddon> lookupAddons(TraderData trader) {
        List<InputTabAddon> addons = new ArrayList<>();
        TraderClientHooks.forEach(trader,IInputClientNode.class,attachment -> addons.addAll(attachment.getInputSettingsAddons(trader)));
        return addons;
    }

}
