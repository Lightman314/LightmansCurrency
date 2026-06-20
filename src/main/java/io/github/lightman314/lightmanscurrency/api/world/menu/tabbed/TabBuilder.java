package io.github.lightman314.lightmanscurrency.api.world.menu.tabbed;

import com.ibm.icu.impl.locale.XCldrStub;

import java.util.Map;

public interface TabBuilder<X extends TabbedMenu<X,T>,T extends MenuTab<X>> {

    void setTab(int slot,T tab);
    void removeTab(int slot);
    void addTab(T tab);
    Map<Integer,T> previewTabs();

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
        public Map<Integer, T> previewTabs() { return XCldrStub.ImmutableMap.copyOf(this.tabs); }
    }

}