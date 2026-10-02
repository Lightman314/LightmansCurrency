package io.github.lightman314.lightmanscurrency.features.atm.tabs;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.PlayerBankDataCache;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.features.atm.ATMTab;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public class AccountSelectionTab extends ATMTab {

    public static final Identifier CLIENT_KEY = LCApi.id("account_selection");

    public static final TextEntry TOOLTIP = TextEntry.tooltip(LCApi.MODID,"atm.selection");
    public static final TextEntry BUTTON_PLAYER_ACCOUNT = TextEntry.button(LCApi.MODID,"atm.selection.player_account");
    public static final TextEntry GUI_SELECT_PLAYER_SUCCESS = TextEntry.button(LCApi.MODID,"atm.selection.player.success");

    public AccountSelectionTab(ATMMenu menu) { super(menu); }
    @Override
    public boolean usesMoneySlots() { return false; }

    @Override
    public Identifier getClientTabKey() { return CLIENT_KEY; }

    public void selectAccount(@Nullable BankReference account) {
        if(account == null)
            return;
        if(this.isClient()) {
            this.send(FancyPacketMap.map().set("selectAccount",LCFancyPacketTypes.BANK_REFERENCE,account));
            return;
        }
        if(account.allowedAccess(this.getPlayer()))
            PlayerBankDataCache.TYPE.get(this).setSelectedAccount(this.getPlayer(),account);
    }

    public void selectPlayerAccount(String playerName) {
        if(playerName.isBlank())
            return;
        if(this.isClient()) {
            this.send(FancyPacketMap.map().setString("selectPlayerAccount",playerName));
            return;
        }
        PlayerReference pr = PlayerReference.of(this,playerName);
        if(pr != null) {
            BankReference br = PlayerBankReference.of(pr).setSidedContext(this);
            this.selectAccount(br);
            BankAccount a = br.get();
            if(a != null)
                this.sendToClient(FancyPacketMap.map().setText("playerAccountSuccess",a.getName()));
        }
    }

    @Override
    public void onMenuClosed() {
        if(this.isClient())
            return;
        BankReference selected = this.getSelectedAccount();
        if(!selected.canPersist(this.getPlayer()))
            this.selectAccount(PlayerBankReference.of(this.getPlayer()));
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("selectAccount"))
            this.selectAccount(message.get("selectAccount",LCFancyPacketTypes.BANK_REFERENCE).setSidedContext(this));
        if(message.contains("selectPlayerAccount"))
            this.selectPlayerAccount(message.getString("selectPlayerAccount"));
    }

}
