package io.github.lightman314.lightmanscurrency.api.client.events;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab.TabRegistration;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab.TabBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.TabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.MenuTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabbedMenu;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class RegisterClientMenuTabEvent extends Event implements IModBusEvent {

    @Nullable
    Identifier section;

    private final Map<Identifier,Map<Identifier,TabBuilder<?,?,?,?>>> registry;
    @ApiStatus.Internal
    public RegisterClientMenuTabEvent(Map<Identifier,Map<Identifier,TabBuilder<?,?,?,?>>> registry) {
        this.registry = registry;
    }

    public <X extends TabbedMenu<X,T>,T extends MenuTab<X>,S extends TabbedMenuScreen<X,T,?>> TabRegistration<X,T,S> forMenu(DeferredHolder<MenuType<?>,MenuType<X>> menuType,Class<S> screen) {
        return this.forMenu(menuType.getKey().identifier(),null,screen);
    }
    public <X extends TabbedMenu<X,T>,T extends MenuTab<X>,S extends TabbedMenuScreen<X,T,?>> TabRegistration<X,T,S> forMenu(Identifier menuKey,Class<X> menu,Class<S> screen) {
        return new SubBuilder<>(menuKey,this.registry.computeIfAbsent(menuKey,t -> new HashMap<>()));
    }

    private record SubBuilder<X extends TabbedMenu<X,T>,T extends MenuTab<X>,S extends TabbedMenuScreen<X,T,?>>(Identifier menuKey,Map<Identifier,TabBuilder<?,?,?,?>> submap) implements TabRegistration<X,T,S> {
        @Override
        public <C extends T> TabRegistration<X,T,S> register(Identifier key, TabBuilder<X,C,T,S> builder) {
            if(this.submap.containsKey(key)) {
                LightmansCurrency.LogError("Attempted to register duplicate client tab for " + this.menuKey + " - " + key);
                return this;
            }
            this.submap.put(key,builder);
            return this;
        }
    }

}
