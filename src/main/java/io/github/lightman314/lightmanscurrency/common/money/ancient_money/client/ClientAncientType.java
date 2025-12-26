package io.github.lightman314.lightmanscurrency.common.money.ancient_money.client;

import io.github.lightman314.lightmanscurrency.api.money.client.ClientCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.common.attachments.EventUnlocks;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientMoneyType;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientMoneyValue;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

public class ClientAncientType extends ClientCurrencyType {

    public static final ClientAncientType INSTANCE = new ClientAncientType();

    private ClientAncientType() { super(AncientMoneyType.INSTANCE); }
    @Override
    public List<MoneyInputHandler> getInputHandlers(@Nullable Player player) {
        if(player == null || EventUnlocks.isUnlocked(player, "ancient_coins") || LCAdminMode.isAdminPlayer(player))
            return List.of(new AncientCoinValueInput());
        return List.of();
    }

    @Override
    public DisplayEntry getDisplayEntry(MoneyValue value, @Nullable List<Component> additionalTooltips, boolean overrideTooltips) {
        if(value instanceof AncientMoneyValue val)
            return new AncientPriceEntry(val, additionalTooltips, overrideTooltips);
        return this.throwIllegalDisplayException(value,AncientMoneyValue.class);
    }
}
