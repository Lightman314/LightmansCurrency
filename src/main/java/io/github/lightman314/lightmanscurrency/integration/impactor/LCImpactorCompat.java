package io.github.lightman314.lightmanscurrency.integration.impactor;


import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.integration.impactor.money.ImpactorCurrencyType;
import net.impactdev.impactor.api.configuration.key.ConfigKey;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.impactdev.impactor.core.economy.ImpactorEconomyService;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LCImpactorCompat {

    public static void setup() {
        if(LCConfig.COMMON.compatImpactor.get())
            MoneyAPI.getApi().RegisterCurrencyType(ImpactorCurrencyType.INSTANCE);
    }

    @Nullable
    public static Account getPlayerAccount(Player player, Currency currency) { return getPlayerAccount(player.getUUID(),currency); }
    @Nullable
    public static Account getPlayerAccount(UUID player, Currency currency) {
        try {
            return EconomyService.instance().account(currency,player).get();
        }catch (Exception e) { return null; }
    }

    @Nullable
    public static Currency getCurrency(Key key) { return EconomyService.instance().currencies().currency(key).orElse(null); }

    //I want to update this to properly copy text components at some point
    //Sadly, I may not be able to do this as their text system is completely different from vanilla MC's
    public static MutableComponent convertComponent(Component adventureComponent) {
        return EasyText.literal(adventureComponent.toString());
    }

    public static <T> Optional<T> getConfigValue(ConfigKey<T> key) {
        if(EconomyService.instance() instanceof ImpactorEconomyService e)
            return Optional.ofNullable(e.config().get(key));
        return Optional.empty();
    }
}
