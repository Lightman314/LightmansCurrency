package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
    public void onOpen() {

        this.inputWidget = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 25, this::getInputSideValue, this.getIgnoreList(), this::ToggleInputSide, this::addWidget);
        this.outputWidget = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 110, this.screen.getGuiTop() + 25, this::getOutputSideValue, this.getIgnoreList(), this::ToggleOutputSide, this::addWidget);

        this.getAddons().forEach(a -> a.onInit(this));

    }

    @Override
    public void onClose() {

        this.getAddons().forEach(a -> a.onClose(this));

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        //Side Widget Labels
        this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.settings.iteminput.side"), this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 7, 0x404040);
        this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.settings.itemoutput.side"), this.screen.getGuiLeft() + 110, this.screen.getGuiTop() + 7, 0x404040);

        this.getAddons().forEach(a -> a.renderBG(this, pose, mouseX, mouseY, partialTicks));

    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

        //Render side tooltips
        this.inputWidget.renderTooltips(pose, mouseX, mouseY, this.screen);
        this.outputWidget.renderTooltips(pose, mouseX, mouseY, this.screen);

        this.getAddons().forEach(a -> a.renderTooltips(this, pose, mouseX, mouseY));

    }

    @Override
    public void tick() {

        this.inputWidget.tick();
        this.outputWidget.tick();

        this.getAddons().forEach(a -> a.tick(this));

    }

    private void ToggleInputSide(Direction side)
    {
        CompoundTag message = new CompoundTag();
        message.putBoolean("SetInputSide", !this.getInputSideValue(side));
        message.putInt("Side", side.get3DDataValue());
        this.sendNetworkMessage(message);
    }

    private void ToggleOutputSide(Direction side)
    {
        CompoundTag message = new CompoundTag();
        message.putBoolean("SetOutputSide", !this.getOutputSideValue(side));
        message.putInt("Side", side.get3DDataValue());
        this.sendNetworkMessage(message);
    }

}
