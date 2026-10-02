package io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.events.RegisterClientMenuTabEvent;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.MenuTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabbedMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModLoader;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ClientMenuTab<X extends TabbedMenu<X,T>,C extends T,T extends MenuTab<X>,S extends TabbedMenuScreen<X,T,?>> implements IWidgetHolder, IRegistryAccess {

    private static final Map<Identifier,Map<Identifier,TabBuilder<?,?,?,?>>> registry = new HashMap<>();

    public final ScreenArea getArea() { return this.screen.getArea(); }
    public final ScreenPosition getCorner() { return this.screen.getCorner(); }

    public static void initialize() {
        if(registry.isEmpty())
            ModLoader.postEvent(new RegisterClientMenuTabEvent(registry));
    }

    @Nullable
    public static ClientMenuTab<?,?,?,?> create(TabbedMenu<?,?> menu,MenuTab<?> tab,TabbedMenuScreen<?,?,?> screen)
    {
        TabBuilder<?,?,?,?> builder = registry.getOrDefault(menu.getMenuKey(),Map.of()).get(tab.getClientTabKey());
        if(builder == null)
            return null;
        return builder.forceBuild(menu,tab,screen);
    }

    public final Player getPlayer() { return this.getMenu().getPlayer(); }
    @Override
    public final HolderLookup.Provider registryAccess() { return this.menu.registryAccess(); }

    private final List<Object> children = new ArrayList<>();
    private final S screen;
    public final S getScreen() { return this.screen; }
    protected final Font getFont() { return this.screen.getFont(); }
    private final X menu;
    public final X getMenu() { return this.menu; }
    private final C commonTab;
    public final C getCommonTab() { return this.commonTab; }

    protected ClientMenuTab(X menu,C commonTab,S screen)
    {
        this.menu = menu;
        this.commonTab = commonTab;
        this.screen = screen;
    }

    @Override
    public final <A> A addChild(A child) {
        if(child == this)
            throw new IllegalArgumentException("Cannot add yourself as a child!");
        this.screen.addChild(child);
        this.children.add(child);
        return child;
    }

    @Override
    public final void removeChild(Object child) {
        this.screen.removeChild(child);
        this.children.remove(child);
    }

    @Override
    public final void removeAllChildren() {
        for(Object child : new ArrayList<>(this.children))
            this.screen.removeChild(child);
        this.children.clear();
    }

    public abstract IconData getIcon();
    public abstract Component getName();

    public boolean isVisible() { return true; }

    public abstract void extractBackground(FancyGuiExtractor gui, ScreenArea area);

    @OverridingMethodsMustInvokeSuper
    public void onTabOpened(FancyPacketMap message) {
        this.children.clear();
        this.initialize(this.screen.getArea(),message);
    }
    protected abstract void initialize(ScreenArea area,FancyPacketMap message);
    @OverridingMethodsMustInvokeSuper
    public void onTabClosed() { this.afterTabClosed(); }
    protected void afterTabClosed() {}

    public void sendMessage(FancyPacketMap message) { this.screen.sendMessage(message); }

    public void handleMessage(FancyPacketMap message) {}

    public boolean blockInventoryButtonClosing() { return false; }

    public interface TabBuilder<X extends TabbedMenu<X,T>,C extends T,T extends MenuTab<X>,S extends TabbedMenuScreen<X,T,?>>
    {
        ClientMenuTab<?,?,?,?> build(X menu,C commonTab,S screen);
        @Nullable
        default ClientMenuTab<?,?,?,?> forceBuild(TabbedMenu<?,?> menu, MenuTab<?> commonTab, TabbedMenuScreen<?,?,?> screen)
        {
            try {
                return this.build((X)menu,(C)commonTab,(S)screen);
            } catch (ClassCastException exception) {
                LightmansCurrency.LogError("Error buildinc client menu tab:",exception);
                return null;
            }
        }
    }

    public interface TabRegistration<X extends TabbedMenu<X,T>,T extends MenuTab<X>,S extends TabbedMenuScreen<X,T,?>> {

        <C extends T> TabRegistration<X,T,S> register(Identifier key,TabBuilder<X,C,T,S> builder);

    }

}