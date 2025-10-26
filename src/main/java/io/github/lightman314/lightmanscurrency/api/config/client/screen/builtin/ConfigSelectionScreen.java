package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ConfigSelectionScreen extends ConfigScreen {

    private final List<ConfigFileOption> configFiles;
    private final String modName;

    private boolean firstOpen = true;

    private ConfigSelectionScreen(Screen parentScreen, String modName, List<ConfigFileOption> configFiles)
    {
        super(parentScreen);
        this.modName = modName;
        this.configFiles = configFiles;

    }

    @Override
    protected void initialize(ScreenArea screenArea) {
        if(this.firstOpen)
        {
            this.firstOpen = false;
            for(ConfigFileOption option : this.configFiles)
                option.onSelectionScreenOpened(this.minecraft);
        }
        int centerX = screenArea.centerX();
        int centerY = screenArea.centerY();
        int yPos = Math.round(((float)this.configFiles.size() / 2f) * -30) + 5 + centerY;
        for(ConfigFileOption option : this.configFiles)
        {
            this.addChild(EasyTextButton.builder()
                    .position(centerX - 100, yPos)
                    .width(200)
                    .text(option.name())
                    .pressAction(() -> this.editConfig(option))
                    .addon(EasyAddonHelper.tooltips(option.buttonTooltip()))
                    .addon(EasyAddonHelper.activeCheck(() -> option.canAccess(this.minecraft)))
                    .build());

            yPos += 30;
        }

        //Back Button
        this.addChild(EasyTextButton.builder()
                .position(centerX - 100,screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(200)
                .text(LCText.CONFIG_BACK)
                .pressAction(this::onClose)
                .build());

    }

    @Override
    protected void afterClose() {
        for(ConfigFileOption option : this.configFiles)
            option.onSelectionScreenClosed(this.minecraft);
    }

    @Override
    protected List<Component> getTitleSections() { return List.of(LCText.CONFIG_TITLE_FILES.get(this.modName)); }

    public static IConfigScreenFactory createFactory(ConfigFile... configFiles)
    {
        List<ConfigFileOption> entries = new ArrayList<>();
        for(ConfigFile file : configFiles)
            entries.add(ConfigFileOption.create(file));
        return createFactory(entries);
    }
    public static IConfigScreenFactory createFactory(ConfigFileOption... configFiles) { return createFactory(ImmutableList.copyOf(configFiles)); }
    public static IConfigScreenFactory createFactory(List<ConfigFileOption> configFiles) {
        return (c,s) -> new ConfigSelectionScreen(s,c.getModInfo().getDisplayName(),ImmutableList.copyOf(configFiles));
    }

    private void editConfig(ConfigFileOption entry) {
        if(entry.canAccess(this.minecraft))
            this.minecraft.setScreen(entry.openScreen(this));
    }

}
