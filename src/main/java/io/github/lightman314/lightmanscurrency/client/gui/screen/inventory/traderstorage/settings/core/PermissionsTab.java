package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PermissionsTab extends SettingsSubTab {

    public PermissionsTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    List<PermissionOption> options;

    protected int startHeight() { return 5; }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.BOOKSHELF); }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.allyperms"); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.EDIT_PERMISSIONS); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.options = new ArrayList<>();
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            this.options.addAll(trader.getPermissionOptions());
        int startHeight = screenArea.y + this.startHeight();
        for(int i = 0; i < this.options.size(); ++i)
        {
            int xPos = this.getXPos(i) + screenArea.x;
            int yPos = this.getYPosOffset(i) + startHeight;
            PermissionOption option = this.options.get(i);
            option.initWidgets(this, xPos, yPos, this::addChild);
        }

    }


    private int getYPosOffset(int index)
    {
        int yIndex = index / 2;
        //Trying 18 pixels per input instead of 20 to see if it'll let them all fit properly
        return 18 * yIndex;
    }

    private int getXPos(int index) { return index % 2 == 0 ? 5 : 105; }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        int startHeight = this.startHeight();
        for(int i = 0; i < this.options.size(); ++i)
        {
            PermissionOption option = this.options.get(i);
            int xPos = this.getXPos(i) + option.widgetWidth();
            int yPos = this.getYPosOffset(i) + startHeight;
            int textWidth = 90 - option.widgetWidth();
            int textHeight = gui.font.wordWrapHeight(option.widgetName().getString(), textWidth);
            int yStart = ((20 - textHeight) / 2) + yPos;
            gui.drawWordWrap(option.widgetName(), xPos, yStart, textWidth, 0xFFFFFF);
        }

    }

    @Override
    public void tick() {
        for (PermissionOption option : this.options)
            option.tick();
    }

    @Override
    public boolean shouldRenderInventoryText() { return this.options.size() < 15; }

}
