package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs;

import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tabs.EasyTabButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class TabOverflowHandler {

    public static TabOverflowHandler CreateBasic() { return new None(); }
    public static TabOverflowHandler CreateScrolling(int scrollX, int scrollY, int scrollWidth, int scrollHeight) { return new ScrollableWheel(scrollX, scrollY, scrollWidth, scrollHeight); }
    public static TabOverflowHandler CreateVerticalButtonScrolling(int topButtonX, int topButtonY, int bottomButtonX, int bottomButtonY) { return new ScrollableButton(bottomButtonX, bottomButtonY, IconAndButtonUtil.ICON_DOWN, topButtonX, topButtonY, IconAndButtonUtil.ICON_UP); }
    public static TabOverflowHandler CreateHorizontalButtonScrolling(int leftButtonX, int leftButtonY, int rightButtonX, int rightButtonY) { return new ScrollableButton(leftButtonX, leftButtonY, IconAndButtonUtil.ICON_LEFT, rightButtonX, rightButtonY, IconAndButtonUtil.ICON_RIGHT); }

    public abstract void addWidgets(IEasyScreen screen);
    public abstract void handleTabs(List<AbstractWidget> tabButtons, List<EasyTab<?>> tabs, Player player, int buttonLimit, Function<Integer, ScreenPosition> positionSource, Function<Integer,EasyTabRotation> rotationSource);

    private static class None extends TabOverflowHandler {
        @Override
        public void addWidgets(IEasyScreen screen) {}
        @Override
        public void handleTabs(List<AbstractWidget> tabButtons, List<EasyTab<?>> tabs, Player player, int buttonLimit, Function<Integer, ScreenPosition> positionSource, Function<Integer,EasyTabRotation> rotationSource) {
            int displayIndex = 0;
            for(int i = 0; i < tabButtons.size(); ++i)
            {
                AbstractWidget button = tabButtons.get(i);
                if(i < tabs.size())
                {
                    EasyTab<?> tab = tabs.get(i);
                    button.visible = tab.isTabVisible(player) && tab.canOpenTab(player);
                    if(button.visible)
                    {
                        ScreenPosition buttonPos = positionSource.apply(displayIndex);
                        button.x = buttonPos.x;
                        button.y = buttonPos.y;
                        if(button instanceof EasyTabButton b)
                            b.setRotation(rotationSource.apply(displayIndex));
                        displayIndex++;
                    }
                }
                else
                    button.visible = false;
            }
        }
    }

    private static abstract class Scrollable extends TabOverflowHandler
    {

        private int scroll = 0;
        protected int getScroll() { return this.scroll; }
        protected void setScroll(int newScroll) { this.scroll = newScroll; this.validateScroll(); }
        private int cachedButtonCount = 0;
        protected int getButtonCount() { return this.cachedButtonCount; }
        private int cachedButtonLimit = 0;
        protected int getButtonLimit() { return this.cachedButtonLimit; }

        protected int getMaxScroll() { return this.getMaxScroll(this.cachedButtonCount, this.cachedButtonLimit); }

        private int getMaxScroll(int buttonCount, int buttonLimit) { return Math.max(0, buttonCount - buttonLimit); }

        @Override
        public final void handleTabs(List<AbstractWidget> tabButtons, List<EasyTab<?>> tabs, Player player, int buttonLimit, Function<Integer, ScreenPosition> positionSource, Function<Integer,EasyTabRotation> rotationSource) {

            this.cachedButtonLimit = buttonLimit;

            List<AbstractWidget> visibleButtons = new ArrayList<>();
            //Check buttons for visibility status
            for(int i = 0; i < tabButtons.size(); ++i)
            {
                AbstractWidget button = tabButtons.get(i);
                if(i < tabs.size())
                {
                    EasyTab<?> tab = tabs.get(i);
                    button.visible = tab.isTabVisible(player) && tab.canOpenTab(player);
                    if(button.visible)
                        visibleButtons.add(button);
                }
                else
                    button.visible = false;
            }
            this.cachedButtonCount = visibleButtons.size();
            this.validateScroll(visibleButtons.size(), buttonLimit);

            int displayIndex = 0;
            for(int i = this.scroll; i < visibleButtons.size() && i < this.scroll + buttonLimit; ++i)
            {
                AbstractWidget button = visibleButtons.get(i);
                ScreenPosition buttonPos = positionSource.apply(displayIndex);
                button.x = buttonPos.x;
                button.y = buttonPos.y;
                if(button instanceof EasyTabButton b)
                    b.setRotation(rotationSource.apply(displayIndex));
                displayIndex++;
            }
            //Hide buttons not in the visible scroll area
            for(int i = 0; i < this.scroll; ++i)
                visibleButtons.get(i).visible = false;
            for(int i = this.scroll + buttonLimit; i < visibleButtons.size(); ++i)
                visibleButtons.get(i).visible = false;

            this.afterTabsHandled();

        }

        protected void afterTabsHandled() {}

        protected void validateScroll() { this.validateScroll(this.cachedButtonCount, this.cachedButtonLimit); }
        protected void validateScroll(int buttonCount, int buttonLimit) { this.scroll = MathUtil.clamp(this.scroll, 0, this.getMaxScroll(buttonCount, buttonLimit)); }

    }

    private static class ScrollableWheel extends Scrollable implements IScrollListener
    {
        private final int scrollX;
        private final int scrollY;
        private final int scrollWidth;
        private final int scrollHeight;
        private ScrollableWheel(int scrollX, int scrollY, int scrollWidth, int scrollHeight) { this.scrollX = scrollX; this.scrollY = scrollY; this.scrollWidth = scrollWidth; this.scrollHeight = scrollHeight; }

        @Override
        public void addWidgets(IEasyScreen screen) {}

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            int scroll = this.getScroll();
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
    }

    private static class ScrollableButton extends Scrollable {

        private final int buttonDownX;
        private final int buttonDownY;
        private final IconData buttonDownIcon;
        private final int buttonUpX;
        private final int buttonUpY;
        private final IconData buttonUpIcon;

        private IconButton buttonUp;
        private IconButton buttonDown;

        private ScrollableButton(int buttonDownX, int buttonDownY, IconData buttonDownIcon, int buttonUpX, int buttonUpY, IconData buttonUpIcon) {
            this.buttonDownX = buttonDownX;
            this.buttonDownY = buttonDownY;
            this.buttonDownIcon = buttonDownIcon;
            this.buttonUpX = buttonUpX;
            this.buttonUpY = buttonUpY;
            this.buttonUpIcon = buttonUpIcon;
        }


        @Override
        public void addWidgets(IEasyScreen screen) {
            this.buttonDown = screen.addChild(new IconButton(screen.guiLeft() + this.buttonDownX, screen.guiTop() + this.buttonDownY, b -> this.scrollDown(), this.buttonDownIcon));
            this.buttonUp = screen.addChild(new IconButton(screen.guiLeft() + this.buttonUpX, screen.guiTop() + this.buttonUpY, b -> this.scrollUp(), this.buttonUpIcon));
        }

        @Override
        protected void afterTabsHandled() {
            if(this.buttonUp != null && this.buttonDown != null)
            {
                if(this.getMaxScroll() == 0)
                {
                    this.buttonUp.visible = this.buttonDown.visible = false;
                }
                else
                {
                    this.buttonUp.visible = this.buttonDown.visible = true;
                    this.buttonUp.active = this.getScroll() < this.getMaxScroll();
                    this.buttonDown.active = this.getScroll() > 0;
                }
            }
        }

        private void scrollDown() {
            if(this.getScroll() > 0)
                this.setScroll(this.getScroll() - 1);
        }

        private void scrollUp() {
            if(this.getScroll() < this.getMaxScroll())
                this.setScroll(this.getScroll() + 1);
        }

    }

}
