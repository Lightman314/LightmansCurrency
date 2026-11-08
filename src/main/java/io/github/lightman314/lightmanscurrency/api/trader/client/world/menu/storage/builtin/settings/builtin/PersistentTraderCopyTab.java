package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.builtin;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.PersistentDataNode;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.TraderDataCache;
import net.minecraft.network.chat.Component;

public class PersistentTraderCopyTab extends SettingsSubTab {

    public PersistentTraderCopyTab(SettingsClientTab parent) { super(parent); }

    @Override
    public boolean isVisible() { return LCApi.isInAdminMode(this.getPlayer()); }

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/persistent")); }

    @Override
    public Component getName() { return PersistentDataNode.TOOLTIP_SETTINGS.get(); }

    private TextBoxWrapper<String> idInput;
    private TextBoxWrapper<String> ownerInput;
    private TextBoxWrapper<String> nameOverrideInput;

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {

        //ID input
        this.idInput = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(30,25))
                .ofWidth(area.width - 60)
                .withOldWidget(this.idInput)
                .allowedString(TraderDataCache::validPersistentID)
                .build());

        //Owner Input
        this.ownerInput = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(30,55))
                .ofWidth(area.width - 60)
                .withOldWidget(this.ownerInput)
                .withOldValue()
                .withStartingValue("Server")
                .build());

        //Name Input
        this.nameOverrideInput = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(30,85))
                .ofWidth(area.width - 60)
                .withOldWidget(this.nameOverrideInput)
                .build());

        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(30,115))
                .ofWidth(area.width - 60)
                .withText(PersistentDataNode.BUTTON_CREATE_COPY)
                .onPress(this::createPersistentCopy)
                .active(this::canCreateCopy)
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        //ID Label
        gui.text(PersistentDataNode.GUI_ID.get(),32,17,0xFF404040,false);
        //Owner Label
        gui.text(PersistentDataNode.GUI_OWNER.get(),32,47,0xFF404040,false);
        //Name Label
        gui.text(PersistentDataNode.GUI_NAME.get(),32,77,0xFF404040,false);
    }

    private boolean canCreateCopy() {
        if(this.idInput == null)
            return false;
        return TraderDataCache.validPersistentID(this.idInput.getValue()) && this.ownerInput != null && !this.ownerInput.getString().isBlank();
    }

    private void createPersistentCopy() {
        if(this.idInput == null || this.ownerInput == null || this.nameOverrideInput == null)
            return;
        this.sendSettingRequest(PersistentDataNode.TYPE,FancyPacketMap.map().setMap("makePersistentCopy",FancyPacketMap.map()
                .setString("id",this.idInput.getValue())
                .setString("owner",this.ownerInput.getValue())
                .setString("name",this.nameOverrideInput.getValue())));
    }

}