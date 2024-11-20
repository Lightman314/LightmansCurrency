package io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed;

import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyTabbedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public abstract class EasyTabbedMenuScreen<M extends EasyTabbedMenu<M,T>,T extends EasyMenuTab<M,T>,S extends AdvancedTabbedMenuScreen<M,M,T,S>> extends AdvancedTabbedMenuScreen<M,M,T,S> {

    public EasyTabbedMenuScreen(@Nonnull M menu, @Nonnull Inventory inventory) {
        super(menu, inventory);
    }

    public EasyTabbedMenuScreen(@Nonnull M menu, @Nonnull Inventory inventory, @Nonnull Component title) {
        super(menu, inventory, title);
    }
}