package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketAddOrRemoveTrade;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;

public class NameTab extends SettingsSubTab implements IMouseListener {


    public NameTab(@Nonnull TraderSettingsClientTab parent) {
        super(parent);
        ScreenArea screenArea = this.screen.getArea();
        this.iconArea = ScreenArea.of((screenArea.width / 2) - 8, 96,16,16);
    }

    private final ScreenArea iconArea;
    private boolean iconEditable() {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.showOnTerminal();
        return false;
    }

    EditBox nameInput;
    EasyButton buttonSetName;
    EasyButton buttonResetName;

    IconButton buttonToggleCreative;
    EasyButton buttonAddTrade;
    EasyButton buttonRemoveTrade;

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_NAME.get(); }

    @Override
    public boolean canOpen() { return true; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 25, 160, 20, EasyText.empty()));
        this.nameInput.setMaxLength(32);
        this.nameInput.setValue(trader.getCustomName());

        this.buttonSetName = this.addChild(new EasyTextButton(screenArea.pos.offset(20, 50), 74, 20, LCText.BUTTON_SETTINGS_CHANGE_NAME.get(), this::SetName));
        this.buttonResetName = this.addChild(new EasyTextButton(screenArea.pos.offset(screenArea.width - 93, 50), 74, 20, LCText.BUTTON_SETTINGS_RESET_NAME.get(), this::ResetName));

        //Creative Toggle
        this.buttonToggleCreative = this.addChild(IconAndButtonUtil.creativeToggleButton(screenArea.pos.offset(176, 110), this::ToggleCreative, () -> {
            TraderData t = this.menu.getTrader();
            return t != null && t.isCreative();
        }));
        this.buttonAddTrade = this.addChild(IconAndButtonUtil.plusButton(screenArea.pos.offset(166, 110), this::AddTrade)
                .withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_SETTINGS_CREATIVE_ADD_TRADE)));
        this.buttonRemoveTrade = this.addChild(IconAndButtonUtil.minusButton(screenArea.pos.offset(166, 120), this::RemoveTrade)
                .withAddons(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_SETTINGS_CREATIVE_REMOVE_TRADE)));

        this.tick();

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        gui.drawString(LCText.GUI_NAME.get(), 20, 15, 0x404040);

        //Draw current trade count
        if(LCAdminMode.isAdminPlayer(this.menu.getPlayer()))
        {
            String count = String.valueOf(trader.getTradeCount());
            int width = gui.font.width(count);
            gui.drawString(count, 164 - width, 140 - 25, 0x404040);
        }

        if(this.iconEditable())
        {
            //Render Label
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TRADER_SETTINGS_CUSTOM_ICON.get(), screen.getXSize() / 2, this.iconArea.y - 12, 0x404040);
            //Render slot background
            gui.blit(TraderScreen.GUI_TEXTURE,this.iconArea.pos.offset(-1,-1),TraderScreen.WIDTH,0,18,18);
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

        this.buttonToggleCreative.visible = LCAdminMode.isAdminPlayer(this.menu.getPlayer());
        if(this.buttonToggleCreative.visible)
        {
            this.buttonAddTrade.visible = true;
            this.buttonAddTrade.active = trader.getTradeCount() < TraderData.GLOBAL_TRADE_LIMIT;
            this.buttonRemoveTrade.visible = true;
            this.buttonRemoveTrade.active = trader.getTradeCount() > 1;
        }
        else
        {
            this.buttonAddTrade.visible = false;
            this.buttonRemoveTrade.visible = false;
        }
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

    private void ToggleCreative(EasyButton button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        this.sendMessage(this.builder().setBoolean("MakeCreative", !trader.isCreative()));
    }

    private void AddTrade(EasyButton button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        new CPacketAddOrRemoveTrade(trader.getID(), true).send();
    }

    private void RemoveTrade(EasyButton button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        new CPacketAddOrRemoveTrade(trader.getID(), false).send();
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if(this.iconEditable() && this.iconArea.offsetPosition(this.screen.getCorner()).isMouseInArea(mouseX,mouseY))
        {
            ItemStack heldItem = this.menu.getHeldItem();
            TraderData trader = this.menu.getTrader();
            if(trader != null)
                this.sendMessage(this.builder().setCompound("ChangeIcon",trader.getIconForItem(heldItem).save(this.menu.registryAccess())));
            return true;
        }
        return false;
    }
}
