package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMConversionButton;
import io.github.lightman314.lightmanscurrency.common.atm.ATMConversionButtonData;
import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestExchangeUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.client.gui.components.Button;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExchangeUpgradeTab extends CoinChestTab.Upgrade
{

    public ExchangeUpgradeTab(CoinChestUpgradeData data, Object screen) { super(data, screen); }

    List<ATMConversionButton> buttons = new ArrayList<>();

    private Button exchangeWhileOpenButton;

    @Override
    public boolean coinSlotsVisible() { return false; }

    @Override
    public void init() {

        this.buttons = new ArrayList<>();

        List<ATMConversionButtonData> buttonData = ATMData.get().getConversionButtons();
        int left = this.screen.getGuiLeft();
        int top = this.screen.getGuiTop();
        for(ATMConversionButtonData data : buttonData)
            this.buttons.add(this.screen.addRenderableTabWidget(new ATMConversionButton(left, top, data, this::SelectNewCommand)));

        this.exchangeWhileOpenButton = this.screen.addRenderableTabWidget(EasyButton.builder(EasyText.empty(), this::ToggleExchangeWhileOpen).pos(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 124).size(this.screen.getXSize() - 20, 20).build());

        this.tick();

    }

    private void updateSelectedButton()
    {
        CoinChestUpgradeData data = this.getUpgradeData();
        String currentCommand = data == null ? "" : UpgradeType.COIN_CHEST_EXCHANGE.getExchangeCommand(data);
        for(ATMConversionButton button : this.buttons)
            button.selected = Objects.equals(button.data.command, currentCommand);
    }

    private void SelectNewCommand(String command)
    {
        this.screen.getMenu().SendMessageToServer(LazyPacketData.builder().setString("SetExchangeCommand", command));
    }

    private void ToggleExchangeWhileOpen(Button button)
    {
        CoinChestUpgradeData data = this.getUpgradeData();
        boolean currentState = data != null && data.upgrade instanceof CoinChestExchangeUpgrade upgrade && upgrade.getExchangeWhileOpen(data);
        this.screen.getMenu().SendMessageToServer(LazyPacketData.builder().setBoolean("SetExchangeWhileOpen", !currentState));
    }

    @Override
     public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void postRender(PoseStack pose, int mouseX, int mouseY) { }

    @Override
    public void tick()
    {
        this.updateSelectedButton();
        CoinChestUpgradeData data = this.getUpgradeData();
        if(data != null)
            this.SetExchangeWhileOpenText(data);
    }

    private void SetExchangeWhileOpenText(@Nonnull CoinChestUpgradeData data)
    {
        if(this.exchangeWhileOpenButton == null)
            return;
        this.exchangeWhileOpenButton.setMessage(EasyText.translatable("button.lightmanscurrency.upgrade.coin_chest.exchange.while_open." + (UpgradeType.COIN_CHEST_EXCHANGE.getExchangeWhileOpen(data) ? "y" : "n")));
    }

    @Override
    public void onClose() { }

}
