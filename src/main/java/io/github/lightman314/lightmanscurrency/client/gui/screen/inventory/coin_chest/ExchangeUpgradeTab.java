package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.CoinChestScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMPageManager;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.ATMAPI;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestExchangeUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ExchangeUpgradeTab extends CoinChestTab.Upgrade
{

    private final ATMPageManager exchangeData;

    public ExchangeUpgradeTab(CoinChestUpgradeData data, Object screen) {
        super(data, screen);
        this.exchangeData = ATMAPI.getATMPageManager(((CoinChestScreen)screen).getPlayer(), this::addExchangeButton, this::removeExchangeButton, this::ToggleCommand, this::isButtonSelected);
    }

    List<ATMExchangeButton> buttons = new ArrayList<>();

    EasyButton exchangeWhileOpenButton;

    @Override
    public boolean coinSlotsVisible() { return false; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.buttons.clear();

        this.exchangeData.initialize(screenArea);

        this.exchangeWhileOpenButton = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(10,124))
                .width(screenArea.width - 20)
                .text(this::GetExchangeWhileOpenText)
                .pressAction(this::ToggleExchangeWhileOpen)
                .build());

        this.tick();

    }

    private boolean isButtonSelected(ATMExchangeButton button) {
        CoinChestUpgradeData data = this.getUpgradeData();
        List<String> commands = data == null ? ImmutableList.of() : Upgrades.COIN_CHEST_EXCHANGE.getExchangeCommands(data);
        return commands.contains(button.data.command);
    }

    private void addExchangeButton(Object child)
    {
        if(child instanceof ATMExchangeButton b)
            this.buttons.add(b);
        this.addChild(child);
    }

    private void removeExchangeButton(Object child)
    {
        if(child instanceof ATMExchangeButton b)
            this.buttons.remove(b);
        this.removeChild(child);
    }

    private void ToggleCommand(String command)
    {
        this.screen.getMenu().SendMessageToServer(this.builder().setString("ToggleExchangeCommand", command));
    }

    private void ToggleExchangeWhileOpen(EasyButton button)
    {
        CoinChestUpgradeData data = this.getUpgradeData();
        boolean currentState = data != null && data.upgrade instanceof CoinChestExchangeUpgrade upgrade && upgrade.getExchangeWhileOpen(data);
        this.screen.getMenu().SendMessageToServer(this.builder().setBoolean("SetExchangeWhileOpen", !currentState));
    }

    @Override
     public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    private Component GetExchangeWhileOpenText()
    {
        CoinChestUpgradeData data = this.getUpgradeData();
        if(data != null)
            return Upgrades.COIN_CHEST_EXCHANGE.getExchangeWhileOpen(data) ? LCText.BUTTON_EXCHANGE_UPGRADE_EXCHANGE_WHILE_OPEN_YES.get() : LCText.BUTTON_EXCHANGE_UPGRADE_EXCHANGE_WHILE_OPEN_NO.get();
        return EasyText.empty();
    }

}
