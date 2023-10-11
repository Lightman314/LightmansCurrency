package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.List;

public class InputTab extends SettingsSubTab {

    public InputTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    DirectionalSettingsWidget inputWidget;
    DirectionalSettingsWidget outputWidget;

    protected InputTraderData getInputTrader() {
        TraderData trader = this.menu.getTrader();
        if(trader instanceof InputTraderData)
            return (InputTraderData)trader;
        return null;
    }

    protected boolean getInputSideValue(Direction side) {
        InputTraderData trader = this.getInputTrader();
        if(trader != null)
            return trader.allowInputSide(side);
        return false;
    }

    protected boolean getOutputSideValue(Direction side) {
        InputTraderData trader = this.getInputTrader();
        if(trader != null)
            return trader.allowOutputSide(side);
        return false;
    }

    protected ImmutableList<Direction> getIgnoreList() {
        InputTraderData trader = this.getInputTrader();
        if(trader != null)
            return trader.ignoreSides;
        return ImmutableList.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN);
    }

    @Nonnull
    @Override
    public IconData getIcon() {
        InputTraderData trader = this.getInputTrader();
        if(trader != null)
            return trader.inputSettingsTabIcon();
        return IconData.of(Items.HOPPER);
    }

    @Override
    public MutableComponent getTooltip() {
        InputTraderData trader = this.getInputTrader();
        if(trader != null)
            return trader.inputSettingsTabTooltip();
        return EasyText.translatable("tooltip.lightmanscurrency.settings.iteminput");
    }

    public List<? extends InputTabAddon> getAddons() {
        InputTraderData trader = this.getInputTrader();
        if(trader != null)
            return trader.inputSettingsAddons();
        return ImmutableList.of();
    }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.InputTrader.EXTERNAL_INPUTS); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.inputWidget = new DirectionalSettingsWidget(screenArea.pos.offset(20, 25), this::getInputSideValue, this.getIgnoreList(), this::ToggleInputSide, this::addChild);
        this.outputWidget = new DirectionalSettingsWidget(screenArea.pos.offset(110, 25), this::getOutputSideValue, this.getIgnoreList(), this::ToggleOutputSide, this::addChild);

        this.getAddons().forEach(a -> a.onOpen(this, screenArea, firstOpen));

    }

    @Override
    protected void onSubtabClose() { this.getAddons().forEach(a -> a.onClose(this)); }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        //Side Widget Labels
        gui.drawString(EasyText.translatable("gui.lightmanscurrency.settings.iteminput.side"), 20, 7, 0x404040);
        gui.drawString(EasyText.translatable("gui.lightmanscurrency.settings.itemoutput.side"), 110, 7, 0x404040);

        this.getAddons().forEach(a -> a.renderBG(this, gui));

    }

    @Override
    public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {

        this.getAddons().forEach(a -> a.renderBG(this, gui));

    }

    @Override
    public void tick() { this.getAddons().forEach(a -> a.tick(this)); }

    private void ToggleInputSide(Direction side)
    {
        this.sendMessage(LazyPacketData.builder()
                .setBoolean("SetInputSide", !this.getInputSideValue(side))
                .setInt("Side", side.get3DDataValue()));
    }

    private void ToggleOutputSide(Direction side)
    {
        this.sendMessage(LazyPacketData.builder()
                .setBoolean("SetOutputSide", !this.getOutputSideValue(side))
                .setInt("Side", side.get3DDataValue()));
    }

}
