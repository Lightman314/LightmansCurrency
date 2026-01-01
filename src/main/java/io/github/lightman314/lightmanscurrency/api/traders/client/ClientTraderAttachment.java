package io.github.lightman314.lightmanscurrency.api.traders.client;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderScreen;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.InfoSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.TraderInfoClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.addons.MiscTabAddon;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ClientTraderAttachment {

    protected ClientTraderAttachment() { }

    public void addSettingsTabs(TraderData trader, TraderSettingsClientTab tab, List<SettingsSubTab> tabs) {}
    public void addPermissionOptions(TraderData trader, List<PermissionOption> options) {}
    public void addInfoTabs(TraderData trader, TraderInfoClientTab tab, List<InfoSubTab> tabs) {}
    public void onScreenInit(TraderData trader, ITraderScreen screen, Consumer<Object> addWidget) {}
    public void onStorageScreenInit(TraderData trader, ITraderStorageScreen screen, Consumer<Object> addWidget) {}
    public void addMiscTabAddons(TraderData trader, List<MiscTabAddon> addons) {}

}