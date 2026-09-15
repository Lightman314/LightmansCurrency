package io.github.lightman314.lightmanscurrency.api.world.menu.tabbed;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

public interface TabBuilder<X extends TabbedMenu<X,T>,T extends MenuTab<X>> {

    void setTab(int slot,T tab);
    void removeTab(int slot);
    void addTab(T tab);
    default void safeAddTab(T tab) {
        if(!this.hasTab(tab.getClass()))
            this.addTab(tab);
    }
    Map<Integer,T> previewTabs();
    default boolean hasTab(int slot) { return this.previewTabs().containsKey(slot); }
    default boolean hasTab(Predicate<T> filter) { return this.previewTabs().values().stream().anyMatch(filter); }
    default boolean hasTab(Class<?> clazz) { return this.hasTab(clazz::isInstance); }

    static <X extends TabbedMenu<X,T>,T extends MenuTab<X>> TabBuilder<X,T> forMap(Map<Integer,T> tabs) { return new Impl<>(tabs); }

    final class Impl<X extends TabbedMenu<X,T>,T extends MenuTab<X>> implements TabBuilder<X,T>
    {
        private final Map<Integer,T> tabs;
        private Impl(Map<Integer,T> tabs) { this.tabs = tabs; }
        @Override
        public void setTab(int slot,T tab) { this.tabs.put(slot,tab); }
        @Override
        public void removeTab(int slot) { this.tabs.remove(slot);}
        @Override
        public void addTab(T tab) {
            int index = 0;
            while(true)
            {
                if(!this.tabs.containsKey(index))
                {
                    this.setTab(index,tab);
                    return;
                }
                index++;
            }
        }

        @Override
        public Map<Integer, T> previewTabs() { return Collections.unmodifiableMap(this.tabs); }
    }

}