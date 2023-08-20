package io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector.OwnershipClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorTab;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;

public class OwnershipTab extends TaxCollectorTab {

    public OwnershipTab(TaxCollectorMenu menu) { super(menu); }

    @Override
    public boolean canBeAccessed() { return this.isOwner() && !this.isServerEntry(); }

    @Override
    public Object createClientTab(Object screen) { return new OwnershipClientTab(screen, this); }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    public void SetOwnerPlayer(String playerName)
    {
        TaxEntry entry = this.getEntry();
        if(entry != null)
        {
            if(this.isServer())
            {
                PlayerReference newOwner = PlayerReference.of(false, playerName);
                if(newOwner != null)
                    entry.getOwner().SetOwner(newOwner);
            }
            else
                this.menu.SendMessageToServer(LazyPacketData.simpleString("SetOwnerPlayer", playerName));
        }
    }

    public void SetOwnerTeam(long teamID)
    {
        TaxEntry entry = this.getEntry();
        if(entry != null)
        {
            Team team = TeamSaveData.GetTeam(this.isClient(), teamID);
            if(team != null && team.isMember(this.menu.player))
                entry.getOwner().SetOwner(team);
            if(this.isClient())
                this.menu.SendMessageToServer(LazyPacketData.simpleLong("SetOwnerTeam", teamID));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("SetOwnerPlayer"))
            this.SetOwnerPlayer(message.getString("SetOwnerPlayer"));
        if(message.contains("SetOwnerTeam"))
            this.SetOwnerTeam(message.getLong("SetOwnerTeam"));
    }
}
