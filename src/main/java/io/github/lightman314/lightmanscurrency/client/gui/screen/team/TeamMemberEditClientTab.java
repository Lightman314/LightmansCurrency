package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.teams.TeamManagementClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.teams.tabs.TeamMemberEditTab;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class TeamMemberEditClientTab extends TeamManagementClientTab<TeamMemberEditTab> {

    public TeamMemberEditClientTab(@Nonnull Object screen, @Nonnull TeamMemberEditTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_ALEX_HEAD; }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TEAM_MEMBER_EDIT.get(); }

    ScrollTextDisplay memberDisplay;

    EditBox memberNameInput;
    EasyButton buttonAddMember;
    EasyButton buttonPromoteMember;
    EasyButton buttonRemoveMember;

    @Override
    public boolean blockInventoryClosing() { return true; }
    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.memberNameInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 11, screenArea.y + 9, 178, 20, EasyText.empty()));
        this.memberNameInput.setMaxLength(16);

        this.buttonAddMember = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(10,30))
                .width(60)
                .text(LCText.BUTTON_ADD)
                .pressAction(this::addMember)
                .build());
        this.buttonPromoteMember = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(70,30))
                .width(60)
                .text(LCText.BUTTON_TEAM_MEMBER_PROMOTE)
                .pressAction(this::addAdmin)
                .build());
        this.buttonRemoveMember = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(130,30))
                .width(60)
                .text(LCText.BUTTON_REMOVE)
                .pressAction(this::removeMember)
                .build());
        this.buttonAddMember.active = this.buttonPromoteMember.active = this.buttonRemoveMember.active = false;

        this.memberDisplay = this.addChild(ScrollTextDisplay.builder()
                .position(screenArea.pos.offset(10,55))
                .size(screenArea.width - 20,screenArea.height - 65)
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
            //Do NOT List Owner
            //list.add(new TextComponent(team.getOwner().lastKnownName()).withStyle(ChatFormatting.GREEN));
            //List Admins
            team.getAdmins().forEach(admin -> list.add(admin.getNameComponent(true).withStyle(ChatFormatting.DARK_GREEN)));
            //List members
            team.getMembers().forEach(member -> list.add(member.getNameComponent(true)));
        }

        return list;
    }

    @Override
    public void tick() {

        ITeam team = this.menu.selectedTeam();
        if(team != null && team.isAdmin(this.menu.player))
        {
            this.buttonAddMember.active = this.buttonPromoteMember.active = this.buttonRemoveMember.active = !this.memberNameInput.getValue().isBlank();
        }
        else
        {
            this.buttonAddMember.active = this.buttonPromoteMember.active = false;
            this.buttonRemoveMember.active = this.menu.player.getGameProfile().getName().equalsIgnoreCase(this.memberNameInput.getValue());
        }

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    private void addMember(EasyButton button)
    {
        if(this.memberNameInput.getValue().isBlank())
            return;

        this.commonTab.AddMember(this.memberNameInput.getValue());
        this.memberNameInput.setValue("");

    }

    private void addAdmin(EasyButton button)
    {
        if(this.memberNameInput.getValue().isBlank())
            return;

        this.commonTab.AddAdmin(this.memberNameInput.getValue());
        this.memberNameInput.setValue("");
    }

    private void removeMember(EasyButton button)
    {
        if(this.memberNameInput.getValue().isBlank())
            return;

        this.commonTab.RemoveMember(this.memberNameInput.getValue());
        this.memberNameInput.setValue("");
    }

}
