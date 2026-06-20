package io.github.lightman314.lightmanscurrency.api.world.menu.tabbed;

public interface ISortedTab {

    int getTabSortPriority();

    static int getTabSortPriority(Object... obj) {
        for(Object o : obj)
        {
            if(o instanceof ISortedTab t)
                return t.getTabSortPriority();
        }
        return 0;
    }

}