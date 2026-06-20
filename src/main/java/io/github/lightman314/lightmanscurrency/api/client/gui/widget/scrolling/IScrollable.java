package io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IScrollListener;

public interface IScrollable {

    int getScroll();
    void setScroll(int scroll);
    //Easy runnables for scroll buttons
    default void incrementScroll() {
        int newScroll = this.getScroll() + 1;
        if(newScroll <= this.getMaxScroll())
            this.setScroll(newScroll);
    }
    //Easy runnables for scroll buttons
    default void decrementScroll() {
        int newScroll = this.getScroll() - 1;
        if(newScroll >= this.getMinScroll())
            this.setScroll(newScroll);
    }

    default int getMinScroll() { return 0; }
    int getMaxScroll();

    default IScrollListener buildListener() {
        return (x,y,dx,dy) -> {
            int scroll = this.getScroll();
            int newScroll = scroll;
            if(dy < 0 && scroll < this.getMaxScroll())
                newScroll++;
            else if(dy > 0 && scroll > this.getMinScroll())
                newScroll --;
            if(scroll != newScroll)
            {
                this.setScroll(scroll);
                return true;
            }
            return false;
        };
    }

    default void validateScroll() {
        int scroll = this.getScroll();
        int max = this.getMaxScroll();
        if(scroll > max)
            this.setScroll(max);
        else
        {
            int min = this.getMinScroll();
            if(scroll < min)
                this.setScroll(min);
        }
    }

    static int calculateMaxScroll(int actualSize,int visibleEntries) { return Math.max(actualSize - visibleEntries,0); }
    static int calculateMaxScroll(int actualSize,int entriesPerScroll,int visibleEntries) { return Math.max(0,Math.ceilDiv(actualSize,entriesPerScroll) - (visibleEntries/entriesPerScroll)); }

}