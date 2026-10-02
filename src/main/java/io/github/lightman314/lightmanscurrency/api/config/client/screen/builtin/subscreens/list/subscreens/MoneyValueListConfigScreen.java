package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.subscreens;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.MoneyValueListOption;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class MoneyValueListConfigScreen extends ConfigScreen {

    private final ConfigFileOption file;
    private final MoneyValueListOption option;
    private final int index;
    private final Consumer<Object> changeHandler;
    public MoneyValueListConfigScreen(Screen parentScreen,ConfigFileOption file,MoneyValueListOption option,int index,Consumer<Object> changeHandler) {
        super(parentScreen);
        this.file = file;
        this.option = option;
        this.index = index;
        this.changeHandler = changeHandler;
    }

    private MoneyValueWidget valueWidget;

    private MoneyValue getValue() { return ListHelper.getOrDefault(this.option.get(),this.index,MoneyValue.empty()); }

    @Override
    protected boolean centerWidgets() { return true; }

    @Override
    protected void initialize(ScreenArea area, IPositionalWidgetHolder scrollingArea) {

        this.valueWidget = scrollingArea.addChild(area.centerX() - MoneyValueWidget.HALF_WIDTH,0,MoneyValueWidget.builder()
                .startingValue(this.getValue())
                .oldWidget(this.valueWidget)
                .handler(this.changeHandler::accept)
                .active(() -> this.file.canEdit(this.minecraft))
                .build());

        this.addChild(TextButton.builder()
                .atPos(area.centerX() - 100,area.height - BOTTOM_BUTTON_OFFSET)
                .ofWidth(200)
                .withText(CommonComponents.GUI_BACK)
                .onPress(this::onClose)
                .build());

    }

    @Override
    protected void extractAdditionalBG(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitSprite(LCSprites.GENERIC_BACKGROUND,this.valueWidget.getArea().offsetPosition(-5,-10).grow(10,20));
    }

    @Override
    protected List<Component> getTitleSections() { return List.of(this.file.name(),this.option.getDisplayName(),LCText.Config.CONFIG_OPTION_LIST_ENTRY.get(this.index + 1)); }

    @Override
    protected void clientTick() {
        if(this.index < 0 | this.index >= this.option.getSize())
            this.onClose();
    }

}
