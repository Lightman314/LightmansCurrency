package io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.TeamOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.api.teams.TeamAPI;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.List;

public class PotentialTeamOwner extends PotentialOwner {

    public static final int TEAM_PRIORITY = 1000;

    private final long teamID;
    public PotentialTeamOwner(long teamID) { super(TeamOwner.of(teamID), TEAM_PRIORITY); this.teamID = teamID; }

    @Nonnull
    @Override
    public IconData getIcon() {
        int count = 0;
        ITeam team = TeamAPI.getTeam(true, this.teamID);
        if(team != null)
            count = team.getMemberCount();
        return IconData.of(Items.PLAYER_HEAD, String.valueOf(count));
    }

    @Override
    public void appendTooltip(@Nonnull List<Component> tooltip) {
        ITeam team = TeamAPI.getTeam(true, this.teamID);
        if(team != null)
            LCText.TOOLTIP_OWNER_TEAM.tooltip(tooltip,team.getName(),team.getMemberCount());
    }

}
