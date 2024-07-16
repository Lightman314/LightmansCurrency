package io.github.lightman314.lightmanscurrency.integration.ftbteams.ownership;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin.PotentialTeamOwner;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PotentialFTBTeamOwner extends PotentialOwner {

    private final UUID teamID;
    protected PotentialFTBTeamOwner(@Nonnull UUID teamID) {
        super(new FTBTeamOwner(teamID), PotentialTeamOwner.TEAM_PRIORITY);
        this.teamID = teamID;
    }

    @Nullable
    private Team getTeam()
    {
        if(this.isClient() && FTBTeamsAPI.api().isClientManagerLoaded())
            return FTBTeamsAPI.api().getClientManager().getTeamByID(this.teamID).orElse(null);
        else if(FTBTeamsAPI.api().isManagerLoaded())
            return FTBTeamsAPI.api().getManager().getTeamByID(this.teamID).orElse(null);
        return null;
    }

    @Nonnull
    @Override
    public IconData getIcon() {
        int count = 0;
        Team team = this.getTeam();
        if(team != null)
            count = team.getMembers().size();
        return IconData.of(IconUtil.ITEM_ALEX_HEAD,String.valueOf(count));
    }

    @Override
    public void appendTooltip(@Nonnull List<Component> tooltip) {
        Team team = this.getTeam();
        if(team != null)
            tooltip.addAll(LCText.TOOLTIP_OWNER_TEAM_FTB.get(team.getName().getString(),team.getMembers().size()));
    }

}
