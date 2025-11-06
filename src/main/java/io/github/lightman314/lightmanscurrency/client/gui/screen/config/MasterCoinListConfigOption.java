package io.github.lightman314.lightmanscurrency.client.gui.screen.config;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.MCLChainSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;

public class MasterCoinListConfigOption extends ConfigFileOption {

    public static final ConfigFileOption INSTANCE = new MasterCoinListConfigOption();

    private MasterCoinListConfigOption() {}

    @Override
    public Component name() { return LCText.CONFIG_MCL_LABEL.get(); }

    @Nullable
    @Override
    public List<Component> buttonTooltip() { return ImmutableList.of(LCText.CONFIG_LABEL_FILE.get(CoinAPI.MONEY_FILE_LOCATION)); }

    @Override
    public boolean canAccess(Minecraft minecraft) { return false; }

    @Override
    public boolean canEdit(Minecraft minecraft) {
        if(minecraft.player != null)
            return minecraft.player.hasPermissions(2);
        return false;
    }

    @Override
    public Screen openScreen(Screen parentScreen) { return new MCLChainSelectScreen(parentScreen); }

}