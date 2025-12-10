package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertType;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TaxSettingsTab extends SettingsSubTab {

    public TaxSettingsTab(TraderSettingsClientTab parent) { super(parent); }

    @Override
    public IconData getIcon() { return IconUtil.ICON_TAXES; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_TAXES.get(); }

    @Override
    public boolean canOpen() { return true; }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(20,30))
                .pressAction(this::increaseAcceptableTaxRate)
                .sprite(SpriteUtil.BUTTON_SIGN_PLUS)
                .addon(EasyAddonHelper.activeCheck(() -> this.getAcceptableTaxRate() < 99))
                .build());
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(20,40))
                .pressAction(this::decreaseAcceptableTaxRate)
                .sprite(SpriteUtil.BUTTON_SIGN_MINUS)
                .addon(EasyAddonHelper.activeCheck(() -> this.getAcceptableTaxRate() > 0))
                .build());

        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(30,80))
                .pressAction(this::toggleIgnoreAllTaxes)
                .sprite(SpriteUtil.createCheckbox(this::getIgnoreAllTaxes))
                .addon(EasyAddonHelper.visibleCheck(this::isIgnoreAllTaxesVisible))
                .build());

    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            Pair<Integer,Integer> result = trader.getTotalTaxPercentageRange();
            int acceptableRate = trader.getAcceptableTaxRate();
            //If min rate > acceptable rate -> ERROR
            //If max rate > acceptable rate OR min rate == acceptable rate -> WARNING
            //Otherwise HELPFUL
            int color = result.getFirst() > acceptableRate ? AlertType.ERROR.color : result.getSecond() > acceptableRate || result.getFirst() == acceptableRate ? AlertType.WARN.color : AlertType.HELPFUL.color;
            if(Objects.equals(result.getFirst(),result.getSecond()))
                TextRenderUtil.drawCenteredText(gui, LCText.GUI_TRADER_TAXES_TOTAL_RATE.get(result.getFirst()), this.screen.getXSize() / 2, 16, color);
            else
                TextRenderUtil.drawCenteredText(gui, LCText.GUI_TRADER_TAXES_TOTAL_RATE_RANGE.get(result.getFirst(),result.getSecond()), this.screen.getXSize() / 2, 16, color);
        }

        gui.drawString(LCText.GUI_TRADER_SETTINGS_TAXES_ACCEPTABLE_RATE.get(this.getAcceptableTaxRate()), 34, 37, 0x404040);

        if(this.isIgnoreAllTaxesVisible())
            gui.drawString(LCText.GUI_TRADER_SETTINGS_TAXES_IGNORE_TAXES.get(), 40, 82, 0x404040);

    }

    private boolean isIgnoreAllTaxesVisible() { return LCAdminMode.isAdminPlayer(this.menu.getPlayer()) || this.getIgnoreAllTaxes(); }

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
            this.sendMessage(this.builder().setBoolean("ForceIgnoreAllTaxCollectors", !trader.ShouldIgnoreAllTaxes()));
    }

    private void increaseAcceptableTaxRate(EasyButton button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            int oldRate = trader.getAcceptableTaxRate();
            int newRate = Screen.hasShiftDown() ? oldRate + 10 : oldRate + 1;
            this.sendMessage(this.builder().setInt("AcceptableTaxRate", newRate));
        }
    }

    private void decreaseAcceptableTaxRate(EasyButton button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            int oldRate = trader.getAcceptableTaxRate();
            int newRate = Screen.hasShiftDown() ? oldRate - 10 : oldRate - 1;
            this.sendMessage(this.builder().setInt("AcceptableTaxRate", newRate));
        }
    }

}
