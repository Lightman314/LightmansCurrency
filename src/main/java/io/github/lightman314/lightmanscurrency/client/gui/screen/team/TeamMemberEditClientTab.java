package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerAction;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.player.PlayerListWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamMemberEditTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TeamMemberEditClientTab extends TeamManagementClientTab<TeamMemberEditTab> {

    public TeamMemberEditClientTab(@Nonnull Object screen, @Nonnull TeamMemberEditTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_ALEX_HEAD; }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TEAM_MEMBERS.get(); }

    @Override
    public boolean blockInventoryClosing() { return true; }
    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.addChild(PlayerListWidget.builder()
                .position(screenArea.pos.offset(20,9))
                .width(screenArea.width - 40)
                .rows(7)
                .addPlayer(this.commonTab::PromotePlayer)
                .canAddPlayer(this::CanAdd)
                .action(PlayerAction.builder()
                        .action(this.commonTab::DemotePlayer)
                        .tooltip(this::getDemoteText)
                        .permission(this::CanDemote)
                        .icon(this::getDemoteIcon)
                        .build())
                .action(PlayerAction.builder()
                        .tooltip(LCText.BUTTON_TEAM_MEMBER_PROMOTE)
                        .permission(this::CanPromote)
                        .icon(IconUtil.ICON_PLUS)
                        .action(this.commonTab::PromotePlayer)
                        .build())
                .entryList(this::getMemberList)
                .build());

    }

    private List<PlayerEntry> getMemberList()
    {
        List<PlayerEntry> list = new ArrayList<>();
        ITeam team = this.menu.selectedTeam();
        if(team != null)
        {
            //List the Owner
            list.add(PlayerEntry.of(team.getOwner(),ChatFormatting.GREEN.getColor()));
            //List Admins
            for(PlayerReference admin : team.getAdmins())
                list.add(PlayerEntry.of(admin,ChatFormatting.DARK_GREEN.getColor()));
            //List members
            for(PlayerReference member : team.getMembers())
                list.add(PlayerEntry.of(member));
        }

        return list;
    }

    private boolean CanAdd(PlayerReference player)
    {
        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return false;
        //If they're already a member or this is not an admin, we cannot add them
        return !team.isMember(player) && team.isAdmin(this.menu.player);
    }

    private boolean CanPromote(PlayerReference player)
    {
        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return false;
        //Cannot promote admins
        if(team.isAdmin(player))
            return false;
        //If they're not a member, only require an admin to promote
        if(!team.isMember(player) && team.isAdmin(this.menu.player))
            return true;
        //If they are a member, only the owner can promote to admin
        return team.isMember(player) && team.isOwner(this.menu.player);
    }

    private boolean CanDemote(PlayerReference player)
    {
        ITeam team = this.menu.selectedTeam();
        if(team == null)
            return false;
        //Cannot demote the owner
        if(team.isOwner(player))
            return false;
        //You can always demote yourself
        if(player.is(this.menu.player))
            return true;
        //Admins can demote normal members
        if(team.isMember(player) && !team.isAdmin(player) && team.isAdmin(this.menu.player))
            return true;
        //Owner can demote admins
        return team.isAdmin(player) && team.isOwner(this.menu.player);
    }

    private Component getDemoteText(PlayerReference player)
    {
        ITeam team = this.menu.selectedTeam();
        return team == null || !team.isAdmin(player) ? LCText.BUTTON_REMOVE.get() : LCText.BUTTON_TEAM_MEMBER_DEMOTE.get();
    }

    private IconData getDemoteIcon(PlayerReference player)
    {
        ITeam team = this.menu.selectedTeam();
        return team == null || !team.isAdmin(player) ? IconUtil.ICON_X : IconUtil.ICON_MINUS;
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

}