package io.github.lightman314.lightmanscurrency.api.traders.client;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterClientTraderAttachmentsEvent;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.BooleanPermission;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.InfoSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.TraderInfoClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.core.TaxInfoClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.core.TraderLogClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.info.core.TraderStatsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.*;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.addons.MiscTabAddon;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TraderClientHooks {

    public static List<Object> collectClientAttachments(TraderData trader) {
        RegisterClientTraderAttachmentsEvent event = new RegisterClientTraderAttachmentsEvent(trader);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResults();
    }

    @SuppressWarnings("deprecation")
    public static List<SettingsSubTab> getSettingsTabs(TraderData trader, TraderSettingsClientTab tab)
    {
        //Set up defailt tabs
        List<SettingsSubTab> tabs = Lists.newArrayList(new NameTab(tab),new CreativeSettingsTab(tab),new PersistentTab(tab),new AllyTab(tab),new PermissionsTab(tab),new MiscTab(tab),new TaxSettingsTab(tab));
        //Add tabs from deprecated methods
        tabs.addAll(trader.getSettingsTabs(tab));
        //Add tabs from client attachments
        forEach(trader,attachment -> attachment.addSettingsTabs(trader,tab,tabs));
        //Add ownership tab last
        tabs.add(new OwnershipTab(tab));
        return tabs;
    }

    @SuppressWarnings("deprecation")
    public static List<PermissionOption> getPermissionOptions(TraderData trader)
    {
        //Get default permission options
        List<PermissionOption> options = Lists.newArrayList(
                BooleanPermission.of(Permissions.OPEN_STORAGE),
                BooleanPermission.of(Permissions.CHANGE_NAME),
                BooleanPermission.of(Permissions.EDIT_TRADES),
                BooleanPermission.of(Permissions.COLLECT_COINS),
                BooleanPermission.of(Permissions.STORE_COINS),
                BooleanPermission.of(Permissions.EDIT_TRADE_RULES),
                BooleanPermission.of(Permissions.EDIT_SETTINGS),
                BooleanPermission.of(Permissions.ADD_REMOVE_ALLIES),
                BooleanPermission.of(Permissions.EDIT_PERMISSIONS),
                BooleanPermission.of(Permissions.VIEW_LOGS),
                BooleanPermission.of(Permissions.BANK_LINK),
                BooleanPermission.of(Permissions.BREAK_TRADER),
                BooleanPermission.of(Permissions.TRANSFER_OWNERSHIP)
        );
        if(trader.showOnTerminal())
            options.add(BooleanPermission.of(Permissions.INTERACTION_LINK));
        //Add options from deprecated methods
        options.addAll(trader.getPermissionOptions());
        //Add options from client attachments
        forEach(trader,attachment -> attachment.addPermissionOptions(trader,options));
        //Handle blocked permissions
        options.removeIf(option -> trader.getBlockedPermissions().contains(option.permission));
        return options;
    }

    @SuppressWarnings("deprecation")
    public static List<InfoSubTab> getInfoSubtabs(TraderData trader, TraderInfoClientTab tab)
    {
        //Add built-in tabs
        List<InfoSubTab> tabs = Lists.newArrayList(new TraderLogClientTab(tab,false),new TraderStatsClientTab(tab),new TraderLogClientTab(tab,true),new TaxInfoClientTab(tab));
        //Add tabs from deprecated methods
        tabs.addAll(trader.getInfoTabs(tab));
        //Add tabs from client attachments
        forEach(trader,attachment -> attachment.addInfoTabs(trader,tab,tabs));
        return tabs;
    }

    @SuppressWarnings("deprecation")
    public static List<MiscTabAddon> getMiscTabAddons(TraderData trader)
    {
        //Add addons from deprecated methods
        List<MiscTabAddon> addons = new ArrayList<>(trader.getMiscTabAddons());
        //Add tabs from client attachments
        forEach(trader, attachment -> attachment.addMiscTabAddons(trader,addons));
        return addons;
    }

    public static void forEach(TraderData trader, Consumer<ClientTraderAttachment> action)
    {
        for(Object ca : trader.getClientAttachments())
        {
            if(ca instanceof ClientTraderAttachment attachment)
                action.accept(attachment);
        }
    }

}