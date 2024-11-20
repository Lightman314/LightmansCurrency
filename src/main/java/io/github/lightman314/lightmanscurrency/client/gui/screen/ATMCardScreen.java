package io.github.lightman314.lightmanscurrency.client.gui.screen;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountSelectionWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.ATMCardMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class ATMCardScreen extends EasyMenuScreen<ATMCardMenu> {

    public ATMCardScreen(ATMCardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.resize(200,200);
    }

    private EasyButton buttonLockAccount;

    @Override
    protected void initialize(ScreenArea screenArea) {

        this.addChild(BankAccountSelectionWidget.builder()
                .position(screenArea.pos.offset(20,25))
                .width(screenArea.width - 40)
                .rows(6)
                .filter(this::canAccess)
                .selected(this::getVisibleSelectedAccount)
                .handler(this.menu::setSelectedAccount)
                .build());

        this.buttonLockAccount = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,screenArea.height - 40))
                .width(screenArea.width - 40)
                .text(this::getLockButtonText)
                .pressAction(this::toggleAccountLocked)
                .addon(EasyAddonHelper.tooltips(this::getLockButtonTooltip))
                .build());

    }

    private BankReference getVisibleSelectedAccount()
    {
        BankReference reference = this.menu.getSelectedAccount();
        if(reference != null && this.menu.isAccountValid())
            return reference;
        return null;
    }

    private boolean canAccess(@Nonnull BankReference reference) { return reference.allowedAccess(this.menu.player); }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.renderNormalBackground(this);
        //gui.renderNormalBackground(GUI_TEXTURE,this);
        gui.renderSlot(this, this.menu.slots.get(0));

        BankReference reference = this.menu.getSelectedAccount();
        if(reference != null)
        {
            IBankAccount account = reference.get();
            if(account != null)
                gui.drawString(account.getName(), 25, 10, 0x404040);
        }

    }

    @Override
    protected void renderTick() {
        this.buttonLockAccount.active = this.menu.getAccountLocked() || this.menu.getSelectedAccount() != null;
    }

    private Component getLockButtonText() { return this.menu.getAccountLocked() ? LCText.BUTTON_ATM_CARD_UNLOCK.get() : LCText.BUTTON_ATM_CARD_LOCK.get(); }

    private List<Component> getLockButtonTooltip() { return this.menu.getAccountLocked() ? null : LCText.TOOLTIP_ATM_CARD_LOCK.getAsList(); }

    private void toggleAccountLocked() { this.menu.setAccountLocked(!this.menu.getAccountLocked()); }

}