package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.atm.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestExchangeUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExchangeUpgradeTab extends CoinChestTab.Upgrade
{

    public ExchangeUpgradeTab(CoinChestUpgradeData data, Object screen) { super(data, screen); }

    List<ATMExchangeButton> buttons = new ArrayList<>();

    EasyButton exchangeWhileOpenButton;

    @Override
    public boolean coinSlotsVisible() { return false; }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.buttons = new ArrayList<>();

        List<ATMExchangeButtonData> buttonData = ATMData.get().getConversionButtons();
        for(ATMExchangeButtonData data : buttonData)
            this.buttons.add(this.addChild(new ATMExchangeButton(screenArea.pos, data, this::SelectNewCommand)));

        this.exchangeWhileOpenButton = this.addChild(new EasyTextButton(screenArea.pos.offset(10, 124), screenArea.width - 20, 20, this::GetExchangeWhileOpenText, this::ToggleExchangeWhileOpen));

        this.tick();

    }

    private void updateSelectedButton()
    {
        CoinChestUpgradeData data = this.getUpgradeData();
        String currentCommand = data == null ? "" : UpgradeType.COIN_CHEST_EXCHANGE.getExchangeCommand(data);
        for(ATMExchangeButton button : this.buttons)
            button.selected = Objects.equals(button.data.command, currentCommand);
    }

    private void SelectNewCommand(String command)
    {
        this.screen.getMenu().SendMessageToServer(LazyPacketData.builder().setString("SetExchangeCommand", command));
    }

    private void ToggleExchangeWhileOpen(EasyButton button)
    {
        CoinChestUpgradeData data = this.getUpgradeData();
        boolean currentState = data != null && data.upgrade instanceof CoinChestExchangeUpgrade upgrade && upgrade.getExchangeWhileOpen(data);
        this.screen.getMenu().SendMessageToServer(LazyPacketData.builder().setBoolean("SetExchangeWhileOpen", !currentState));
    }

    @Override
     public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    @Override
    public void tick() { this.updateSelectedButton(); }

    private Component GetExchangeWhileOpenText()
    {
        CoinChestUpgradeData data = this.getUpgradeData();
        if(data != null)
            return EasyText.translatable("button.lightmanscurrency.upgrade.coin_chest.exchange.while_open." + (UpgradeType.COIN_CHEST_EXCHANGE.getExchangeWhileOpen(data) ? "y" : "n"));
        return EasyText.empty();
    }

}
