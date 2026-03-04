package io.github.lightman314.lightmanscurrency.common.traders.commands.client;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTradeOfferNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.misc.MiscTabAddon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.traders.commands.nodes.CommandTradeNode;

import java.util.List;

public class ClientCommandTradeNode extends ClientTradeOfferNode<CommandTradeNode> {

    public ClientCommandTradeNode(CommandTradeNode node) { super(node); }

    @Override
    public void addMiscTabAddons(TraderData trader, List<MiscTabAddon> addons) {
        addons.add(new CommandMiscAddon());
    }

    private static class CommandMiscAddon extends MiscTabAddon
    {

        @Override
        public void addWidgets(SettingsSubTab tab, ScreenArea screenArea, boolean firstOpen) {
            this.addWidget(CommandButtonWidget.create(this));
        }

        private int getPermissionLevel() {
            CommandTradeNode node = this.getNode(CommandTradeNode.TYPE);
            return node == null ? 0 : node.getPermissionLevel();
        }

        private void editPermissionLevel(int delta)
        {
            this.sendMessage(this.getTab().builder().setInt("ChangePermissionLevel",this.getPermissionLevel() + delta));
        }

        private static class CommandButtonWidget extends EasyWidgetWithChildren
        {

            private final CommandMiscAddon addon;
            protected CommandButtonWidget(Builder builder) {
                super(builder);
                this.addon = builder.addon;
            }

            @Override
            public void addChildren(ScreenArea area) {
                this.addChild(PlainButton.builder()
                        .sprite(SpriteUtil.BUTTON_SIGN_PLUS)
                        .position(area.pos.offset(0,0))
                        .pressAction(() -> this.addon.editPermissionLevel(1))
                        .addon(EasyAddonHelper.activeCheck(() -> this.addon.getPermissionLevel() < LCConfig.SERVER.commandTraderMaxPermissionLevel.get()))
                        .build());
                this.addChild(PlainButton.builder()
                        .sprite(SpriteUtil.BUTTON_SIGN_MINUS)
                        .position(area.pos.offset(0,10))
                        .pressAction(() -> this.addon.editPermissionLevel(-1))
                        .addon(EasyAddonHelper.activeCheck(() -> this.addon.getPermissionLevel() > 0))
                        .build());
            }

            @Override
            protected void renderWidget(EasyGuiGraphics gui) {
                gui.drawString(LCText.GUI_TRADER_SETTINGS_COMMAND_PERMISSION_LEVEL.get(this.addon.getPermissionLevel()), 14, 5, 0x404040);
            }

            private static CommandButtonWidget create(CommandMiscAddon addon) { return new CommandButtonWidget(new Builder(addon)); }

            private static class Builder extends EasyBuilder<Builder>
            {
                private final CommandMiscAddon addon;
                private Builder(CommandMiscAddon addon) { super(100,20); this.addon = addon; }
                @Override
                protected Builder getSelf() { return this; }
            }

        }
    }

}
