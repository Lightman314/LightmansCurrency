package io.github.lightman314.lightmanscurrency.api.client.gui.tabbed;

import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class EasyTabbedMenuScreen<M extends EasyTabbedMenu<M,T>,T extends EasyMenuTab<M,T>,S extends AdvancedTabbedMenuScreen<M,M,T,S>> extends AdvancedTabbedMenuScreen<M,M,T,S> {

    public EasyTabbedMenuScreen(M menu, Inventory inventory) {
        super(menu, inventory);
    }

    public EasyTabbedMenuScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
