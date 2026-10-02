package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IClipboardAccess;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.WidgetScrollingArea;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.ISettingsStorageIO;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsSelection;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SettingsClipboardTab;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

import java.util.List;

public class SettingsClipboardClientTab extends TraderStorageClientTab<SettingsClipboardTab> implements IClipboardAccess {

    public static final TabBuilder<TraderStorageMenu,SettingsClipboardTab,TraderStorageTab,TraderStorageScreen> BUILDER = SettingsClipboardClientTab::new;

    protected SettingsClipboardClientTab(TraderStorageMenu menu,SettingsClipboardTab commonTab,TraderStorageScreen screen) { super(menu, commonTab, screen); }

    private SettingsSelection selection = null;
    private WidgetScrollingArea area = null;

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/counting")); }

    public List<ISettingsStorageIO> getSettings() { return this.getCommonTab().getSettings(); }

    @Override
    public Component getName() { return SettingsClipboardTab.TOOLTIP.get(); }

    @Override
    protected void initialize(ScreenArea area,FancyPacketMap message) {
        if(this.selection == null)
            this.selection = new SettingsSelection(this.getSettings());

        //Copy and Paste Buttons
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(10,120))
                .ofWidth(74)
                .withText(SettingsClipboardTab.BUTTON_SETTINGS_COPY)
                .onPress(this::tryCopy)
                .build());
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(area.width - 84,120))
                .ofWidth(74)
                .withText(SettingsClipboardTab.BUTTON_SETTINGS_PASTE)
                .onPress(this::tryLoad)
                .active(this::canReadSettings)
                .build());

        //Scrollable Area
        this.area = this.addChild(WidgetScrollingArea.builder()
                .atPos(area.pos.offset(20,8))
                .ofSize(area.width - 40,112)
                .withOldWidget(this.area)
                .build());

        int yPos = 0;
        //Populate the scrollable area with the settings toggles
        for(ISettingsStorageIO settings : this.getSettings()) {
            final DualKey key = settings.getSettingsKey();
            int xOff = key.hasSecondaryKey() ? 10 : 0;
            //Add the checkmark
            this.area.addChild(ScreenPosition.of(xOff,yPos),
                    SpriteButton.builder()
                    .withSprite(LCSprites.CHECKBOX.buildSprite(() -> this.selection.getState(key)))
                    .onPress(() -> this.selection.toggleState(key,this.getSettings()))
                    .build());
            //Add the text
            this.area.addChild(ScreenPosition.of(xOff + 12,yPos + 1),
                    TextDisplayWidget.builder()
                            .withText(settings.getSettingsName())
                            .ofWidth(this.area.getWidth() - xOff - 14)
                            .build());
            //Increment the y position
            yPos += 14;
        }

        //Validate the scroll value of the area now that all the children have been added
        this.area.validateScroll();
    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitSlot(this.getCommonTab().getSlot());
    }

    private boolean canReadSettings() {
        if(this.getCommonTab().canReadFromItemInSlot())
            return true;
        else {
            try {
                JsonObject json = GsonHelper.parse(this.getClipboard());
                //Can be read if the given json data has *any* keys matching our settings data
                return this.getSettings().stream().anyMatch(s -> json.has(s.getSettingsKey().toString()));
            } catch (JsonParseException ignored) { return false; }
        }
    }

    private void tryCopy() {
        if(this.getCommonTab().canWriteToItemInSlot())
            this.getCommonTab().saveDataToItem(this.selection);
        else
            this.getCommonTab().saveDataToClipboard(this.selection);
    }

    private void tryLoad() {
        if(this.getCommonTab().canReadFromItemInSlot())
            this.getCommonTab().loadDataFromItem(this.selection);
        else if(this.canReadSettings())
            this.getCommonTab().loadDataFromClipboard(this.getClipboard(),this.selection);
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("settingsClipboardContents"))
            this.setClipboard(message.getString("settingsClipboardContents"));
    }

}
