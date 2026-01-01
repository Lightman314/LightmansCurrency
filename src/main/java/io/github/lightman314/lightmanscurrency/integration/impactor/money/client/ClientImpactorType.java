package io.github.lightman314.lightmanscurrency.integration.impactor.money.client;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display.TextDisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.integration.impactor.money.ImpactorCurrencyType;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClientImpactorType extends ClientCurrencyType {

    public static final ClientCurrencyType INSTANCE = new ClientImpactorType();

    private ClientImpactorType() { super(ImpactorCurrencyType.INSTANCE); }

    @Override
    public List<MoneyInputHandler> getInputHandlers(@Nullable Player player) {
        List<MoneyInputHandler> result = new ArrayList<>();
        for(Currency currency : EconomyService.instance().currencies().registered())
            result.add(new ImpactorMoneyInputHandler(currency));
        return result;
    }

    @Override
    public DisplayEntry getDisplayEntry(MoneyValue value, @Nullable List<Component> additionalTooltips, boolean overrideTooltips) {
        return TextDisplayEntry.of(value.getText(EasyText.empty()), TextRenderUtil.TextFormatting.create());
    }
}