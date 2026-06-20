package io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.MenuTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabbedMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ClientMenuTab<X extends TabbedMenu<X,T>,C extends T,T extends MenuTab<X>,S extends TabbedMenuScreen<X,T,?>> implements IWidgetHolder {

    private static final Map<Identifier,TabBuilder<?,?,?,?>> registry = new HashMap<>();

    public static void register(Identifier id,TabBuilder<?,?,?,?> builder) { registry.put(id,builder); }

    @Nullable
    public static ClientMenuTab<?,?,?,?> create(TabbedMenu<?,?> menu, MenuTab<?> tab, TabbedMenuScreen<?,?,?> screen)
    {
        TabBuilder<?,?,?,?> builder = registry.get(tab.getClientTabKey());
        if(builder == null)
            return null;
        return builder.forceBuild(menu,tab,screen);
    }

    private final List<Object> children = new ArrayList<>();
    private final S screen;
    public S getScreen() { return this.screen; }
    private final X menu;
    public X getMenu() { return this.menu; }
    private final C commonTab;
    public C getCommonTab() { return this.commonTab; }

    protected ClientMenuTab(X menu,C commonTab,S screen)
    {
        this.menu = menu;
        this.commonTab = commonTab;
        this.screen = screen;
    }

    @Override
    public final <A> A addChild(A child) {
        this.screen.addChild(child);
        this.children.add(child);
        return child;
    }

    @Override
    public final void removeChild(Object child) {
        this.screen.removeChild(child);
        this.children.remove(child);
    }

    public abstract IconData getIcon();
    public abstract Component getName();

    public boolean isVisible() { return true; }

    public abstract void extractBackground(FancyGuiExtractor gui, ScreenArea area);


    public final void onTabOpened(FancyPacketMap message) {
        this.children.clear();
        this.initialize(this.screen.getArea(),message);
    }
    protected abstract void initialize(ScreenArea area,FancyPacketMap message);
    public final void onTabClosed()
    {
        for(Object child : this.children)
            this.screen.removeChild(child);
        this.children.clear();
        this.afterTabClosed();
    }
    protected void afterTabClosed() {}

    public final void sendMessage(FancyPacketMap message) { this.screen.sendMessage(message); }

    public void handleMessage(FancyPacketMap message) {}

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

}