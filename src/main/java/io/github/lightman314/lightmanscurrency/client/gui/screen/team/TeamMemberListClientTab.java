package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamMemberListTab;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.List;

public class TeamMemberListClientTab extends TeamManagementClientTab<TeamMemberListTab> {

    public TeamMemberListClientTab(@Nonnull Object screen, @Nonnull TeamMemberListTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(Items.PLAYER_HEAD); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TEAM_MEMBERS.get(); }

    ScrollTextDisplay memberDisplay;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.memberDisplay = this.addChild(ScrollTextDisplay.builder()
                .position(screenArea.pos.offset(10,10))
                .size(screenArea.width - 20,screenArea.height - 20)
                .text(this::getMemberList)
                .columns(2)
                .build());

    }

    private List<Component> getMemberList()
    {
        List<Component> list = Lists.newArrayList();
        ITeam team = this.menu.selectedTeam();
        if(team != null)
        {
            //List Owner
            list.add(team.getOwner().getNameComponent(true).withStyle(ChatFormatting.GREEN));
            //List Admins
            team.getAdmins().forEach(admin -> list.add(admin.getNameComponent(true).withStyle(ChatFormatting.DARK_GREEN)));
            //List members
            team.getMembers().forEach(member -> list.add(member.getNameComponent(true)));
        }

        return list;
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

}
