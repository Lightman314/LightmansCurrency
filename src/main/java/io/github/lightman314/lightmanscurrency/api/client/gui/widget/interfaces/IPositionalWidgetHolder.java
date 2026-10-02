package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

import java.util.Collection;

public interface IPositionalWidgetHolder {

    <T extends FancyRenderable> T addChild(ScreenPosition position,T child);
    default <T extends FancyRenderable> T addChild(int x,int y,T child) { return this.addChild(ScreenPosition.of(x,y),child); }
    default int addChild(ScrollingWidgetBuilder builder) { return builder.buildWidgets(this,this.getWidth(),this.getLowestWidgetPoint() + 5); }
    default int addChild(ScrollingWidgetBuilder builder,int yPos) { return builder.buildWidgets(this,this.getWidth(),yPos); }

    <T> T addUnpositionedChild(T child);

    int getWidth();
    int getLowestWidgetPoint();

    default void addChildren(Collection<ScrollingWidgetBuilder> builders,int spacing) { this.addChildren(builders,0,0); }
    default int addChildren(Collection<ScrollingWidgetBuilder> builders,int spacing,int yPos) {
        int totalHeight = 0;
        for(ScrollingWidgetBuilder builder : builders) {
            int height = builder.buildWidgets(this,this.getWidth(),yPos) + spacing;
            yPos += height;
            totalHeight += height;
        }
        return totalHeight;
    }

    void removeChild(Object child);

}