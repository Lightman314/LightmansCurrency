package io.github.lightman314.lightmanscurrency.client.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.client.ClientPlayerNameCache;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.bank.BankAccountSelectionWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.bank.BankBalanceDisplay;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMClientTab;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMScreen;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.MoneyTransferTab;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.UUID;

public class MoneyTransferClientTab extends ATMClientTab<MoneyTransferTab> implements ITickerClient {

    public static final TabBuilder<ATMMenu,MoneyTransferTab,ATMTab,ATMScreen> BUILDER = MoneyTransferClientTab::new;

    public static final int RESPONSE_DURATION = 100;

    protected MoneyTransferClientTab(ATMMenu menu, MoneyTransferTab commonTab, ATMScreen screen) { super(menu, commonTab, screen); }

    private boolean playerMode = false;
    private boolean isPlayerMode() { return this.playerMode; }
    private boolean isNormalMode() { return !this.playerMode; }
    private int responseTimer = 0;
    private Component responseMessage = null;
    private boolean hasMessage() { return this.responseMessage != null; }
    private boolean noMessage() { return this.responseMessage == null; }

    private BankReference selectedAccount = null;

    private MoneyValueWidget amountWidget;
    private BankAccountSelectionWidget accountSelectionWidget;
    private TextBoxWrapper<String> playerInput;

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/store_coins")); }
    @Override
    public Component getName() { return MoneyTransferTab.TOOLTIP.get(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {

        this.amountWidget = this.addChild(MoneyValueWidget.builder()
                .atPos(area.pos)
                .oldWidget(this.amountWidget)
                .disallowFreeInput()
                .visible(this::noMessage)
                .build());

        this.accountSelectionWidget = this.addChild(BankAccountSelectionWidget.builder()
                .atPos(area.pos.offset(10,84))
                .ofWidth(area.width - 20)
                .oldWidget(this.accountSelectionWidget)
                .withRows(3)
                .withFilter(this::allowAccount)
                .withSelected(() -> this.selectedAccount)
                .withHandler(r -> this.selectedAccount = r)
                .visible(this::isNormalMode)
                .build());

        this.playerInput = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(10,104))
                .ofWidth(area.width - 20)
                .withOldWidget(this.playerInput)
                .visible(this::isPlayerMode)
                .withMaxLength(16)
                .build());

        //Balance Display
        this.addChild(BankBalanceDisplay.getAccountBalanceDisplay(this::getBankAccount)
                .atPos(area.pos.offset(5,72))
                .ofWidth(area.width - 10)
                .build());

        //Mode Toggle Icon
        this.addChild(IconButton.builder()
                .atPos(area.pos.offset(area.width,84))
                .onPress(() -> this.playerMode = !this.playerMode)
                .withIcon(() -> this.playerMode ? ItemIcon.of(Items.PLAYER_HEAD) : ItemIcon.of(ItemHelper.ALEX_HEAD))
                .tooltip(TooltipSource.deferredSingle(() -> this.playerMode ? MoneyTransferTab.TOOLTIP_TRANSFER_MODE_LIST.get() : MoneyTransferTab.TOOLTIP_TRANSFER_MODE_PLAYER.get()))
                .build());

        //Transfer Button
        this.addChild(IconButton.builder()
                .atPos(area.pos.offset(area.width,124))
                .onPress(this::attemptTransfer)
                .withIcon(SpriteIcon.of(LCApi.id("icon/store_coins")))
                .withColor(this::getTransferColor)
                .active(this::canTriggerTransfer)
                .tooltip(TooltipSource.deferredSingle(this::getTransferTooltip))
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        if(this.responseMessage != null)
            gui.centeredTextWithWordWrap(this.responseMessage,area.halfWidth(),6,area.width - 10,0xFF404040,false);
    }

    private boolean allowAccount(BankReference reference) {
        return reference != null && !reference.equals(this.getSelectedAccount());
    }

    private int getTransferColor(IconButton button) {
        if(button.active) {
            if(this.playerMode) {
                //Check if selected account exists
                UUID playerID = ClientPlayerNameCache.lookupID(this.playerInput.getString());
                if(playerID == null)
                    return ARGB.opaque(ChatFormatting.GOLD.getColor());
            }
            return 0xFF00FF00;
        }
        return -1;
    }

    @Nullable
    private Component getTransferTooltip() {
        if(this.playerMode) {
            UUID playerID = ClientPlayerNameCache.lookupID(this.playerInput.getString());
            if(playerID == null)
                return null;
            String playerName = ClientPlayerNameCache.lookupName(playerID);
            if(playerName == null)
                playerName = this.playerInput.getString();
            return MoneyTransferTab.TOOLTIP_TRANSFER_TRIGGER.get(this.amountWidget.getCurrentValue().getText(),BankAccount.GUI_BANK_ACCOUNT_NAME.get(playerName));
        }
        else if(this.selectedAccount != null) {
            BankAccount account = this.selectedAccount.get();
            if(account != null)
                return MoneyTransferTab.TOOLTIP_TRANSFER_TRIGGER.get(this.amountWidget.getCurrentValue().getText(),account.getName());
        }
        return null;
    }

    private void attemptTransfer() {
        if(this.playerMode) {
            this.getCommonTab().transferToPlayer(this.amountWidget.getCurrentValue(),this.playerInput.getString());
            this.playerInput.setValue("");
            this.amountWidget.changeValue(MoneyValue.empty());
        }
        else if(this.selectedAccount != null) {
            this.getCommonTab().transferToAccount(this.amountWidget.getCurrentValue(),this.selectedAccount);
            this.selectedAccount = null;
            this.amountWidget.changeValue(MoneyValue.empty());
        }
    }

    private boolean canTriggerTransfer() {
        if(this.amountWidget.getCurrentValue().isEmpty())
            return false;
        if(this.playerMode) {
            UUID playerID = ClientPlayerNameCache.lookupID(this.playerInput.getString());
            if(playerID == null)
                return false;
            if(this.getSelectedAccount() instanceof PlayerBankReference prb)
                return !prb.getPlayer().equals(playerID);
            return true;
        }
        return this.selectedAccount != null;
    }

    @Override
    protected void afterTabClosed() {
        this.responseTimer = 0;
        this.responseMessage = null;
    }

    @Override
    public void clientTick() {
        if(this.responseMessage != null) {
            this.responseTimer++;
            if(this.responseTimer >= RESPONSE_DURATION) {
                this.responseTimer = 0;
                this.responseMessage = null;
            }
        }
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("transferResponse"))
            this.responseMessage = message.getText("transferResponse");
    }
}
