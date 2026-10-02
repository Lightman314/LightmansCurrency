package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.WidgetScrollingArea;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.IExternalInteractionDisplayProvider;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.IInputSettingAddon;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleSettingTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.ExternalInteractionsNode;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ExternalInteractionSettingsTab extends SettingsSubTab.ForNode<ExternalInteractionsNode> {

    private final IconData icon;
    private final Component name;
    private final List<ScrollingWidgetBuilder> addonBuilders;

    private WidgetScrollingArea addonArea;

    public ExternalInteractionSettingsTab(SettingsClientTab parent) {
        super(parent,ExternalInteractionsNode.TYPE);
        this.name = IExternalInteractionDisplayProvider.getName(this);
        this.icon = IExternalInteractionDisplayProvider.getIcon(this);
        List<ScrollingWidgetBuilder> temp = new ArrayList<>();
        for(IInputSettingAddon addon : ClientTraderNode.getClientNodes(this, IInputSettingAddon.class))
            addon.addAdditionalInputSettings(this,this,this::sendSettingRequest,new IInputSettingAddon.InputAddonsBuider(temp));
        this.addonBuilders = List.copyOf(temp);
    }

    @Override
    public IconData getIcon() { return this.icon; }
    @Override
    public Component getName() { return this.name; }

    @Override
    public boolean isVisible() { return this.getPermission(LCPermissions.EXTERNAL_ACCESS_SETTINGS); }

    @Override
    protected void initialize(ScreenArea area,FancyPacketMap message) {
        DirectionalSettingsWidget dsw = this.addChild(DirectionalSettingsWidget.builder()
                .atPos(area.pos.offset(area.halfWidth(),25))
                .forSettings(this::getNode)
                .onPress(this::toggleSide)
                .build());

        //Don't bother placing addon stuff if no addons are present
        if(this.addonBuilders.isEmpty())
            return;

        //Add addon area
        int width = area.width - 40;
        int spaceTaken = 30 + dsw.getHeight();
        this.addonArea = this.addChild(WidgetScrollingArea.builder()
                .atPos(area.pos.offset(20,spaceTaken))
                .ofSize(width,115 - spaceTaken)
                .withOldWidget(this.addonArea)
                .build());

        //And now add the addons to the area
        int y = 0;
        for(ScrollingWidgetBuilder builder : this.addonBuilders) {
            int height = builder.buildWidgets(this.addonArea,width,y);
            y += height + SimpleSettingTab.SPACING;
        }

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        //Input Widget Label
        //gui.centeredText(ExternalInteractionsNode.GUI_SETTINGS_LABEL.get(),area.halfWidth(),7,0xFF404040,false);
    }

    private void toggleSide(Direction side,boolean inverse) {
        ExternalInteractionsNode node = this.getNode();
        if(node != null) {
            DirectionalSettingsState state = node.getSidedState(side);
            if(inverse)
                state = state.getPrevious(node);
            else
                state = state.getNext(node);
            this.sendSettingRequest(FancyPacketMap.map()
                    .setMap("setSidedState",FancyPacketMap.map()
                            .setEnum("state",state)
                            .setEnum("side",side)));
        }
    }

}
