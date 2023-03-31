package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
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

    List<PermissionOption.OptionWidgets> widgets = Lists.newArrayList();
    List<PermissionOption> options;

    protected int startHeight() { return 5; }
    private int calculateStartHeight() { return this.screen.getGuiTop() + this.startHeight(); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.BOOKSHELF); }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.allyperms"); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.EDIT_PERMISSIONS); }

    @Override
    public void onOpen() {

        this.options = new ArrayList<>();
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            this.options.addAll(trader.getPermissionOptions());
        int startHeight = this.calculateStartHeight();
        for(int i = 0; i < this.options.size(); ++i)
        {
            int xPos = this.getXPos(i);
            int yPos = this.getYPosOffset(i) + startHeight;
            PermissionOption option = this.options.get(i);
            PermissionOption.OptionWidgets optionWidgets = option.initWidgets(this, xPos, yPos);
            optionWidgets.getRenderableWidgets().forEach(this::addWidget);
            optionWidgets.getListeners().forEach(this::addWidget);
            this.widgets.add(optionWidgets);
        }

    }


    private int getYPosOffset(int index)
    {
        int yIndex = index / 2;
        //Trying 18 pixels per input instead of 20 to see if it'll let them all fit properly
        return 18 * yIndex;
    }

    private int getXPos(int index)
    {
        return this.screen.getGuiLeft() + (index % 2 == 0 ? 5 : 105);
    }

    @Override
    public void onClose() {

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        int startHeight = this.calculateStartHeight();
        for(int i = 0; i < this.options.size(); ++i)
        {
            PermissionOption option = this.options.get(i);
            int xPos = this.getXPos(i) + option.widgetWidth();
            int yPos = this.getYPosOffset(i) + startHeight;
            int textWidth = 90 - option.widgetWidth();
            int textHeight = this.font.wordWrapHeight(option.widgetName().getString(), textWidth);
            int yStart = ((20 - textHeight) / 2) + yPos;
            this.font.drawWordWrap(option.widgetName(), xPos, yStart, textWidth, 0xFFFFFF);
        }

    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

    }

    @Override
    public void tick() {
        for (PermissionOption option : this.options)
            option.tick();
    }

    @Override
    public boolean shouldRenderInventoryText() { return this.options.size() < 15; }

}