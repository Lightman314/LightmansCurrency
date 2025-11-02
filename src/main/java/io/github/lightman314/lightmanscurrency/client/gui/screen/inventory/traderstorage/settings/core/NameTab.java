package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.easy.GhostSlot;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NameTab extends SettingsSubTab implements IMouseListener {

    public NameTab(TraderSettingsClientTab parent) {
        super(parent);
    }

    private ScreenArea iconArea;
    private boolean iconEditable() {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.showOnTerminal();
        return false;
    }

    EditBox nameInput;
    EasyButton buttonSetName;
    EasyButton buttonResetName;

    EasyButton buttonPickupTrader;


    @Override
    public IconData getIcon() { return IconUtil.ICON_SHOW_LOGGER; }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_NAME.get(); }

    @Override
    public boolean canOpen() { return true; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        this.iconArea = ScreenArea.of((screenArea.width / 2) - 8, 96,16,16);

        this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 25, 160, 20, EasyText.empty()));
        this.nameInput.setMaxLength(32);
        this.nameInput.setValue(trader.getCustomName());

        this.buttonSetName = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,50))
                .width(74)
                .text(LCText.BUTTON_SETTINGS_CHANGE_NAME)
                .pressAction(this::SetName)
                .build());
        this.buttonResetName = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 93, 50))
                .width(74)
                .text(LCText.BUTTON_SETTINGS_RESET_NAME)
                .pressAction(this::ResetName)
                .build());

        //Pickup Button
        this.buttonPickupTrader = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,118))
                .width(screenArea.width - 40)
                .text(LCText.BUTTON_TRADER_SETTINGS_PICKUP_TRADER)
                .pressAction(this::PickupTrader)
                .addon(EasyAddonHelper.tooltips(this::getPickupTooltip, TooltipHelper.DEFAULT_TOOLTIP_WIDTH))
                .build());

        //Add Ghost Slot for the trader icons
        this.addChild(GhostSlot.simpleItem(screenArea.pos.offset(this.iconArea.pos),this::ChangeIcon).asProvider(this::iconEditable));

        this.tick();

    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        gui.drawString(LCText.GUI_NAME.get(), 20, 15, 0x404040);

        if(this.iconEditable())
        {
            //Render Label
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TRADER_SETTINGS_CUSTOM_ICON.get(), screen.getXSize() / 2, this.iconArea.y - 12, 0x404040);
            //Render slot background
            SpriteUtil.EMPTY_SLOT_NORMAL.render(gui,this.iconArea.pos.offset(-1,-1));
            //Render custom icon
            IconData icon = trader.getCustomIcon();
            if(icon != null)
                icon.render(gui, this.iconArea.pos);
        }

    }

    @Override
    public void tick() {
        boolean canChangeName = this.menu.hasPermission(Permissions.CHANGE_NAME);
        this.nameInput.setEditable(canChangeName);

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        this.buttonSetName.active = !this.nameInput.getValue().contentEquals(trader.getCustomName());
        this.buttonSetName.visible = canChangeName;
        this.buttonResetName.active = trader.hasCustomName();
        this.buttonResetName.visible = canChangeName;

        TraderBlockEntity<?> be = trader.getBlockEntity();
        this.buttonPickupTrader.visible = be != null && be.supportsTraderPickup() && this.menu.hasPermission(Permissions.BREAK_TRADER);

    }

    private void SetName(EasyButton button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        String customName = trader.getCustomName();
        if(!customName.contentEquals(this.nameInput.getValue()))
        {
            this.sendMessage(this.builder().setString("ChangeName", this.nameInput.getValue()));
            //LightmansCurrency.LogInfo("Sent 'Change Name' message with value:" + this.nameInput.getValue());
        }
    }

    private void ResetName(EasyButton button)
    {
        this.nameInput.setValue("");
        this.SetName(button);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if(this.iconEditable() && this.iconArea.offsetPosition(this.screen.getCorner()).isMouseInArea(mouseX,mouseY))
        {
            this.ChangeIcon(this.menu.getHeldItem());
            return true;
        }
        return false;
    }

    private void ChangeIcon(ItemStack iconItem)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            IconData icon = trader.getIconForItem(iconItem);
            if(icon != null)
                this.sendMessage(this.builder().setCompound("ChangeIcon",icon.save()));
        }
    }

    private List<Component> getPickupTooltip()
    {
        List<Component> result = new ArrayList<>();
        result.add(LCText.TOOLTIP_TRADER_SETTINGS_PICKUP_TRADER.get());
        if(LCAdminMode.isAdminPlayer(this.menu.getPlayer()))
            result.add(LCText.TOOLTIP_TRADER_SETTINGS_PICKUP_TRADER_ADVANCED.get());
        return result;
    }

    private void PickupTrader(EasyButton button)
    {
        this.sendMessage(this.builder().setBoolean("PickupTrader", Screen.hasShiftDown()));
    }

}