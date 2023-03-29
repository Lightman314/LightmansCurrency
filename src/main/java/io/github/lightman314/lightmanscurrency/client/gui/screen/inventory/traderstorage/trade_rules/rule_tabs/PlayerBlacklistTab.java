package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerBlacklist;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class PlayerBlacklistTab extends TradeRuleSubTab<PlayerBlacklist> {

    public PlayerBlacklistTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, PlayerBlacklist.TYPE); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_BLACKLIST; }

    EditBox nameInput;

    Button buttonAddPlayer;
    Button buttonRemovePlayer;

    ScrollTextDisplay playerDisplay;

    @Override
    public void onOpen() {

        this.nameInput = this.addWidget(new EditBox(this.font, this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 9, screen.getXSize() - 20, 20, Component.empty()));

        this.buttonAddPlayer = this.addWidget(Button.builder(Component.translatable("gui.button.lightmanscurrency.blacklist.add"), this::PressBlacklistButton).pos(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 30).size(78, 20).build());
        this.buttonRemovePlayer = this.addWidget(Button.builder(Component.translatable("gui.button.lightmanscurrency.blacklist.remove"), this::PressForgiveButton).pos(this.screen.getGuiLeft() + this.screen.getXSize() - 88, this.screen.getGuiTop() + 30).size(78, 20).build());

        this.playerDisplay = this.addWidget(new ScrollTextDisplay(this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 55, this.screen.getXSize() - 14, 84, this.font, this::getBlacklistedPlayers));
        this.playerDisplay.setColumnCount(2);

    }

    private List<Component> getBlacklistedPlayers()
    {
        List<Component> playerList = Lists.newArrayList();
        PlayerBlacklist rule = this.getRule();
        if(rule == null)
            return playerList;
        for(PlayerReference player : rule.getBannedPlayers())
            playerList.add(player.getNameComponent(true));
        return playerList;
    }



    @Override
    public void onClose() {

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

    }

    void PressBlacklistButton(Button button)
    {
        String name = nameInput.getValue();
        if(!name.isBlank())
        {
            nameInput.setValue("");
            CompoundTag updateInfo = new CompoundTag();
            updateInfo.putBoolean("Add", true);
            updateInfo.putString("Name", name);
            this.sendUpdateMessage(updateInfo);
        }
    }

    void PressForgiveButton(Button button)
    {
        String name = nameInput.getValue();
        if(!name.isBlank())
        {
            nameInput.setValue("");
            CompoundTag updateInfo = new CompoundTag();
            updateInfo.putBoolean("Add", false);
            updateInfo.putString("Name", name);
            this.sendUpdateMessage(updateInfo);
        }
    }

}
