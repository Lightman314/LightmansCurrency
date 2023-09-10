package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertType;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class TaxSettingsTab extends SettingsSubTab {

    public TaxSettingsTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_TAXES; }

    @Nullable
    @Override
    public Component getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.taxes"); }

    @Override
    public boolean canOpen() { return true; }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.addChild(IconAndButtonUtil.plusButton(screenArea.pos.offset(20, 30), this::increaseAcceptableTaxRate)
                .withAddons(EasyAddonHelper.activeCheck(() -> this.getAcceptableTaxRate() < 99)));
        this.addChild(IconAndButtonUtil.minusButton(screenArea.pos.offset(20, 40), this::decreaseAcceptableTaxRate)
                .withAddons(EasyAddonHelper.activeCheck(() -> this.getAcceptableTaxRate() > 0)));

        this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(30, 80), this::toggleIgnoreAllTaxes, this::getIgnoreAllTaxes)
                .withAddons(EasyAddonHelper.visibleCheck(this::isIgnoreAllTaxesVisible)));

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            int totalRate = trader.getTotalTaxPercentage();
            int acceptableRate = trader.getAcceptableTaxRate();
            int color = totalRate > acceptableRate ? AlertType.ERROR.color : totalRate == acceptableRate ? AlertType.WARN.color : AlertType.HELPFUL.color;
            TextRenderUtil.drawCenteredText(gui, EasyText.translatable("tooltip.lightmanscurrency.trader.tax_info.total_rate", totalRate), this.screen.getXSize() / 2, 16, color);
        }

        gui.drawString(EasyText.translatable("gui.lightmanscurrency.settings.acceptabletaxrate", this.getAcceptableTaxRate()), 34, 37, 0x404040);
        gui.drawString(EasyText.translatable("gui.lightmanscurrency.settings.acceptabletaxrate", this.getAcceptableTaxRate()), 34, 37, 0x404040);

        if(this.isIgnoreAllTaxesVisible())
            gui.drawString(EasyText.translatable("gui.lightmanscurrency.settings.ingoretaxes"), 40, 82, 0x404040);

    }

    private boolean isIgnoreAllTaxesVisible() { return CommandLCAdmin.isAdminPlayer(this.menu.player) || this.getIgnoreAllTaxes(); }

    private boolean getIgnoreAllTaxes()
    {
        TraderData trader = this.menu.getTrader();
        return trader != null && trader.ShouldIgnoreAllTaxes();
    }

    private int getAcceptableTaxRate()
    {
        TraderData trader = this.menu.getTrader();
        return trader != null ? trader.getAcceptableTaxRate() : 0;
    }

    private void toggleIgnoreAllTaxes(EasyButton button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            CompoundTag message = new CompoundTag();
            message.putBoolean("ForceIgnoreAllTaxCollectors", !trader.ShouldIgnoreAllTaxes());
            this.sendNetworkMessage(message);
        }
    }

    private void increaseAcceptableTaxRate(EasyButton button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            int oldRate = trader.getAcceptableTaxRate();
            int newRate = Screen.hasShiftDown() ? oldRate + 10 : oldRate + 1;
            CompoundTag message = new CompoundTag();
            message.putInt("AcceptableTaxRate", newRate);
            this.sendNetworkMessage(message);
        }
    }

    private void decreaseAcceptableTaxRate(EasyButton button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            int oldRate = trader.getAcceptableTaxRate();
            int newRate = Screen.hasShiftDown() ? oldRate - 10 : oldRate - 1;
            CompoundTag message = new CompoundTag();
            message.putInt("AcceptableTaxRate", newRate);
            this.sendNetworkMessage(message);
        }
    }

}