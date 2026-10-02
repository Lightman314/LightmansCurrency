package io.github.lightman314.lightmanscurrency.api.client.gui.widget.bank;

import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.AbstractMultiWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.AccountInteractionTab;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Objects;

public class BankInteractionWidget extends AbstractMultiWidget.LateChildren {

    public static final int BUTTON_WIDTH = 70;

    private final IBankInteractionHandler handler;
    private final int spacing;
    private final boolean allowEmptyDeposits;

    private MoneyValueWidget amountSelection;

    protected BankInteractionWidget(Builder builder) {
        super(builder);
        this.handler = Objects.requireNonNull(builder.handler);
        this.spacing = builder.spacing;
        this.allowEmptyDeposits = builder.allowEmptyDeposits;
        if(builder.oldWidget != null)
            this.amountSelection = builder.oldWidget.amountSelection;
    }

    @Override
    protected void addLateChildren(ScreenArea area) {
        //Amount Widget
        this.amountSelection = this.addChild(MoneyValueWidget.builder()
                .atPos(area.pos)
                .disallowFreeInput()
                .oldWidget(this.amountSelection)
                .visible(this::isVisible)
                .build());

        //Deposit Button
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(13,MoneyValueWidget.HEIGHT + this.spacing))
                .ofWidth(BUTTON_WIDTH)
                .withText(AccountInteractionTab.BUTTON_DEPOSIT)
                .onPress(this::attemptDeposit)
                .active(this::canDeposit)
                .visible(this::isVisible)
                .build());

        //Withdraw Button
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(23 + BUTTON_WIDTH,MoneyValueWidget.HEIGHT + this.spacing))
                .ofWidth(BUTTON_WIDTH)
                .withText(AccountInteractionTab.BUTTON_WITHDRAW)
                .onPress(this::attemptWithdraw)
                .active(this::canWithdraw)
                .visible(this::isVisible)
                .build());

        //Balance Display
        this.addChild(BankBalanceDisplay.getAccountBalanceDisplay(this.handler::getBankAccount)
                .atPos(area.pos.offset(5,MoneyValueWidget.HEIGHT + 25 + this.spacing))
                .ofWidth(area.width - 10)
                .build());

    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        BankAccount account = this.handler.getBankAccount();
        //Render the account name
        Component accountName = account == null ? Component.empty() : account.getName();
        gui.scrollingText(accountName,5,MoneyValueWidget.HEIGHT + 6,area.width - 10,10,0xFF404040,false);
    }

    private boolean canDeposit() {
        BankAccount account = this.handler.getBankAccount();
        return account != null && (this.allowEmptyDeposits || !this.amountSelection.getCurrentValue().isEmpty());
    }

    private void attemptDeposit() {
        MoneyValue amount = this.amountSelection.getCurrentValue();
        if(this.allowEmptyDeposits || !amount.isEmpty()) {
            this.handler.attemptDeposit(amount);
            this.amountSelection.changeValue(MoneyValue.empty());
        }
    }

    private boolean canWithdraw() { return this.handler.getBankAccount() != null && !this.amountSelection.getCurrentValue().isEmpty(); }

    private void attemptWithdraw() {
        MoneyValue amount = this.amountSelection.getCurrentValue();
        if(!amount.isEmpty()) {
            this.handler.attemptWithdraw(amount);
            this.amountSelection.changeValue(MoneyValue.empty());
        }
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder,BankInteractionWidget> {

        private Builder() { super(176,MoneyValueWidget.HEIGHT + 40); }

        @Nullable
        private BankInteractionWidget oldWidget = null;

        private IBankInteractionHandler handler = null;
        private int spacing = 5;
        private boolean allowEmptyDeposits = true;

        public Builder withOldWidget(@Nullable BankInteractionWidget oldWidget) { this.oldWidget = oldWidget; return this; }

        public Builder withHandler(IBankInteractionHandler handler) { this.handler = handler; return this; }
        public Builder withSpacing(int spacing) { this.spacing = spacing; this.setHeight(MoneyValueWidget.HEIGHT + this.spacing + 35); return this; }
        public Builder blockEmptyDeposits() { this.allowEmptyDeposits = false; return this; }

        @Override
        protected Builder getSelf() { return this; }

        @Override
        public BankInteractionWidget build() { return new BankInteractionWidget(this); }
    }

}
