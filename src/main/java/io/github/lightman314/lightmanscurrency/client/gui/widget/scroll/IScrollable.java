package io.github.lightman314.lightmanscurrency.client.gui.widget.scroll;

import io.github.lightman314.lightmanscurrency.util.MathUtil;

public interface IScrollable {
    int currentScroll();
    void setScroll(int newScroll);
    default int getMinScroll() { return 0; }
    int getMaxScroll();
    default boolean handleScrollWheel(double delta) {
        int scroll = this.currentScroll();
        if(delta < 0)
        {
            if(scroll < this.getMaxScroll())
            {
                this.setScroll(scroll + 1);
                return true;
            }
        }
        else if(delta > 0)
        {
            if(scroll > 0)
            {
                this.setScroll(scroll - 1);
                return true;
            }
        }
        return false;
    }
    static int calculateMaxScroll(int visibleCount, int totalCount)
    {
        return Math.max(0, totalCount - visibleCount);
    }
    static int calculateMaxScroll(int visibleCount, int entriesPerScroll, int totalCount)
    {
        return Math.max(0, MathUtil.DivideByAndRoundUp(totalCount, entriesPerScroll) - (visibleCount/entriesPerScroll));
    }
}
