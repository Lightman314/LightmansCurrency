package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.List;

public class AllyTab extends SettingsSubTab {

    public AllyTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    EditBox nameInput;
    EasyButton buttonAddAlly;
    EasyButton buttonRemoveAlly;

    ScrollTextDisplay display;

    @Override
    @Nonnull
    public IconData getIcon() { return IconData.of(Items.PLAYER_HEAD); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_ALLY.get(); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.ADD_REMOVE_ALLIES); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 10, 160, 20, EasyText.empty()));
        this.nameInput.setMaxLength(16);

        this.buttonAddAlly = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,35))
                .width(74)
                .text(LCText.BUTTON_ADD)
                .pressAction(this::AddAlly)
                .build());
        this.buttonRemoveAlly = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 93, 25))
                .width(74)
                .text(LCText.BUTTON_REMOVE)
                .pressAction(this::RemoveAlly)
                .build());

        this.display = this.addChild(new ScrollTextDisplay(screenArea.pos.offset(5, 60), screenArea.width - 10, 75, this::getAllyList));
        this.display.setColumnCount(2);

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    private List<Component> getAllyList()
    {
        List<Component> list = Lists.newArrayList();
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            trader.getAllies().forEach(ally -> list.add(ally.getNameComponent(true)));
        return list;
    }

    @Override
    public void tick() {
        this.buttonAddAlly.active = this.buttonRemoveAlly.active = !this.nameInput.getValue().isEmpty();
    }

    private void AddAlly(EasyButton button)
    {
        String allyName = this.nameInput.getValue();
        this.sendMessage(this.builder().setString("AddAlly", allyName));
        this.nameInput.setValue("");
    }

    private void RemoveAlly(EasyButton button)
    {
        String allyName = this.nameInput.getValue();
        this.sendMessage(this.builder().setString("RemoveAlly", allyName));
        this.nameInput.setValue("");
    }

}
