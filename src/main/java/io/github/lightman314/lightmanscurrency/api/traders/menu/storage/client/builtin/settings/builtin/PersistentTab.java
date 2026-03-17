package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.interfaces.IPersistentTrader;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.network.message.persistentdata.CPacketCreatePersistentTrader;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;

public class PersistentTab extends SettingsSubTab {

    public PersistentTab(TraderSettingsClientTab parent) { super(parent); }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(Items.COMMAND_BLOCK); }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_PERSISTENT.get(); }

    EasyButton buttonSavePersistentTrader;
    EditBox persistentTraderIDInput;
    EditBox persistentTraderOwnerInput;

    @Override
    public boolean canOpen() { return this.menu.getTrader() instanceof IPersistentTrader && LCAdminMode.isAdminPlayer(this.menu.getPlayer()); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.buttonSavePersistentTrader = this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(10,30))
                .pressAction(this::SavePersistentTraderData)
                .icon(IconUtil.ICON_PERSISTENT_DATA)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_PERSISTENT_CREATE_TRADER))
                .build());

        int idWidth = this.getFont().width(LCText.GUI_PERSISTENT_ID.get());
        this.persistentTraderIDInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 37 + idWidth, screenArea.y + 110, 108 - idWidth, 18, EasyText.empty()));

        int ownerWidth = this.getFont().width(LCText.GUI_PERSISTENT_OWNER.get());
        this.persistentTraderOwnerInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 12 + ownerWidth, screenArea.y + 85, 178 - ownerWidth, 18, EasyText.empty()));

        this.tick();

    }

    @Override
    public void tick() {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        if(this.buttonSavePersistentTrader != null)
            this.buttonSavePersistentTrader.active = trader.hasValidTrade();
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {
        if(this.persistentTraderIDInput != null)
        {
            //Draw ID input label
            gui.drawString(LCText.GUI_PERSISTENT_ID.get(), 35, 115, 0xFFFFFF);
            //Draw Owner input label
            gui.drawString(LCText.GUI_PERSISTENT_OWNER.get(), 10, 90, 0xFFFFFF);
        }
    }

    private void SavePersistentTraderData(EasyButton button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader instanceof IPersistentTrader)
            new CPacketCreatePersistentTrader(trader.getID(), this.persistentTraderIDInput.getValue(), this.persistentTraderOwnerInput.getValue()).sendToServer();
    }
}
