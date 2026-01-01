package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.transaction_register;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TransactionRegisterScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionData;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionList;
import io.github.lightman314.lightmanscurrency.common.menus.TransactionRegisterMenu;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TransactionRegisterTab extends EasyClientTab.Unenforced<TransactionRegisterMenu,TransactionRegisterScreen,TransactionRegisterTab> {

    public TransactionRegisterTab(TransactionRegisterScreen screen) { super(screen); }

    protected final TransactionList getData() { return this.menu.getData(); }
    protected final boolean hasTransaction(int index) { return index >= 0 && index < this.getData().transactions.size(); }
    protected final TransactionData getTransaction(int index) { return Objects.requireNonNullElse(this.getNullableTransaction(index),TransactionData.EMPTY); }
    protected final TransactionData getNullableTransaction(int index) {
        List<TransactionData> data = this.getData().transactions;
        if(index >= 0 && index < data.size())
            return data.get(index);
        return null;
    }

    protected void createBackButton(ScreenArea screenArea)
    {
        //Back Button
        this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width,0))
                .icon(IconUtil.ICON_BACK)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRANSACTION_REGISTER_BACK))
                .pressAction(this.screen::closeTab)
                .build());
    }

    protected boolean canChangeStartingValueHandler()
    {
        AtomicBoolean foundMatch = new AtomicBoolean(false);
        for(TransactionData d : this.getData().transactions)
        {
            //Check if the transaction has a non-empty value
            d.argument.ifLeft(val -> {
                if(!val.isEmpty())
                    foundMatch.set(true);
            });
            if(foundMatch.get())
                return false;
        }
        return true;
    }
    protected boolean canChangeValueHandler(int ignoreIndex)
    {
        AtomicBoolean foundMatch = new AtomicBoolean(false);
        TransactionList data = this.getData();
        if(!data.startingValue.isEmpty())
            return false;
        List<TransactionData> list = data.transactions;
        for(int i = 0; i < list.size(); ++i)
        {
            if(i == ignoreIndex)
                continue;
            //Check if the transaction has a non-empty value
            list.get(i).argument.ifLeft(val -> {
                if(!val.isEmpty())
                    foundMatch.set(true);
            });
            if(foundMatch.get())
                return false;
        }
        //If no matches found, we can use any value type we want
        return true;
    }

    protected void onValueInputInit(MoneyValueWidget widget)
    {
        //Forcibly match the input handler to the starting value if it exists
        TransactionList list = this.getData();
        if(!list.startingValue.isEmpty())
        {
            widget.tryMatchHandler(list.startingValue);
            return;
        }
        //Forcibly match the input handler to the first found transaction that has a valid value
        AtomicBoolean tryMatch = new AtomicBoolean(false);
        for(TransactionData d : list.transactions)
        {
            d.argument.ifLeft(val -> {
                if(!val.isEmpty())
                {
                    widget.tryMatchHandler(val);
                    tryMatch.set(true);
                }
            });
            if(tryMatch.get())
                break;
        }
    }

}