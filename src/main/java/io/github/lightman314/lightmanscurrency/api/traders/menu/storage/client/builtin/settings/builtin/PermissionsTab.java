package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.traders.client.IClientPermissionProvider;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.client.PermissionOption;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class PermissionsTab extends SettingsSubTab implements IScrollable {

    public PermissionsTab(TraderSettingsClientTab parent) { super(parent); }

    public static final int PERMISSIONS_PER_PAGE = 8;
    private static final int START_HEIGHT = 20;
    public static final int ROW_HEIGHT = 14;

    List<PermissionOption> options;

    private int scroll = 0;

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(Items.BOOKSHELF); }

    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_ALLY_PERMS.get(); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.EDIT_PERMISSIONS); }

    public boolean isOptionVisible(PermissionOption option)
    {
        int index = this.options.indexOf(option);
        return index >= this.scroll && index < this.scroll + PERMISSIONS_PER_PAGE;
    }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.options = new ArrayList<>();
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            this.options.addAll(IClientPermissionProvider.getPermissionOptions(trader));
        int startHeight = screenArea.y + START_HEIGHT;
        int xPos = screenArea.x + 20;
        for(int i = 0; i < this.options.size(); ++i)
        {
            int yPos = startHeight + (ROW_HEIGHT * i);
            PermissionOption option = this.options.get(i);
            option.initWidgets(this, xPos, yPos, this::addChild);
        }

        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(screenArea.width - 20,START_HEIGHT))
                .height(ROW_HEIGHT * PERMISSIONS_PER_PAGE)
                .scrollable(this)
                .build());

        this.addChild(ScrollListener.builder()
                .position(screenArea.pos)
                .size(screenArea.width,150)
                .listener(this)
                .build());

        this.updateWidgetPositions();

    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        gui.drawString(LCText.TOOLTIP_TRADER_SETTINGS_ALLY_PERMS.get(),8,6,0x404040);

        for(int i = 0; i < PERMISSIONS_PER_PAGE; ++i)
        {
            int index = i + this.scroll;
            if(index >= this.options.size())
                break;
            PermissionOption option = this.options.get(index);
            int xPos = 20 + option.widgetWidth();
            int yPos = START_HEIGHT + (ROW_HEIGHT * i) + 3;

            gui.drawString(option.widgetName(),xPos,yPos,0x404040);
        }

    }

    private void updateWidgetPositions()
    {
        int xPos = this.screen.getGuiLeft() + 20;
        int startY = this.screen.getGuiTop() + START_HEIGHT;
        for(int i = 0; i < PERMISSIONS_PER_PAGE; ++i)
        {
            int index = i + this.scroll;
            if(index >= this.options.size())
                break;
            this.options.get(index).updateWidgetPosition(xPos,startY + (ROW_HEIGHT * i));
        }
    }

    @Override
    public void renderAfterWidgets(EasyGuiGraphics gui) {
        int startHeight = START_HEIGHT;
        for(int i = 0; i < PERMISSIONS_PER_PAGE; ++i)
        {
            int index = i + this.scroll;
            if(index >= this.options.size())
                break;
            PermissionOption option = this.options.get(index);
            int xPos = 20 + option.widgetWidth();
            int yPos = START_HEIGHT + (ROW_HEIGHT * i);
            int textWidth = 150 - option.widgetWidth();
            if(ScreenArea.of(xPos,yPos,textWidth,ROW_HEIGHT).offsetPosition(this.screen.getCorner()).isMouseInArea(gui.mousePos))
            {
                Component tooltip = option.widgetTooltip();
                if(tooltip != null)
                    gui.renderComponentTooltip(TooltipHelper.splitTooltips(tooltip));
                break;
            }
        }
    }

    @Override
    public void tick() {
        for (PermissionOption option : this.options)
            option.tick();
    }

    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) {
        this.scroll = newScroll;
        this.updateWidgetPositions();
    }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(PERMISSIONS_PER_PAGE,this.options.size()); }

}
