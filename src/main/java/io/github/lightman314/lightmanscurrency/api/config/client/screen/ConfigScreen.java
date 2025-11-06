package io.github.lightman314.lightmanscurrency.api.config.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ConfigScreen extends EasyScreen {

    private final Screen parentScreen;

    public static final int BOTTOM_BUTTON_OFFSET = 25;

    //Copied from AbstractSelectionList
    private static final ResourceLocation HEADER_SEPARATOR = VersionUtil.vanillaResource("textures/gui/header_separator.png");
    private static final ResourceLocation FOOTER_SEPARATOR = VersionUtil.vanillaResource("textures/gui/footer_separator.png");
    private static final ResourceLocation MENU_LIST_BACKGROUND = VersionUtil.vanillaResource("textures/gui/menu_list_background.png");
    private static final ResourceLocation INWORLD_MENU_LIST_BACKGROUND = VersionUtil.vanillaResource("textures/gui/inworld_menu_list_background.png");

    public ConfigScreen(Screen parentScreen) {
        super(EasyText.empty());
        this.parentScreen = parentScreen;
    }

    //Set the screens "size" to the total screen size
    @Override
    protected void preInit() { this.resize(this.width, this.height); }

    @Override
    public final void onClose() {
        this.minecraft.setScreen(this.parentScreen);
        this.afterClose();
    }

    protected void afterClose() { }

    protected int headerSize() { return 33; }

    protected int footerSize() { return 33; }

    @Override
    protected final void renderBG(EasyGuiGraphics gui) {

        ScreenArea area = this.getArea();
        super.renderBackground(gui.getGui());
        //Render the header
        gui.setColor(0.125f, 0.125f, 0.125f);
        gui.blit(Screen.BACKGROUND_LOCATION, 0, 0, 0, 0, width, this.headerSize(), 32, 32);
        gui.resetColor();
        RenderSystem.enableBlend();
        gui.blit(HEADER_SEPARATOR, 0, this.headerSize() - 2, 0, 0, area.width, 2, 32, 2);
        RenderSystem.disableBlend();

        //Background
        gui.setColor(0.125f, 0.125f, 0.125f, 1f);
        gui.blit(Screen.BACKGROUND_LOCATION, 0, this.headerSize(), 0, 0, area.width, area.height - this.headerSize() - this.footerSize(), 32, 32);
        gui.resetColor();

        //Render the footer
        gui.setColor(0.125f, 0.125f, 0.125f);
        gui.blit(Screen.BACKGROUND_LOCATION, 0, area.height - this.footerSize(), 0, 0, this.width, this.footerSize(), 32, 32);
        gui.resetColor();
        RenderSystem.enableBlend();
        gui.blit(FOOTER_SEPARATOR, 0, area.height - this.footerSize(), 0, 0, area.width, 2, 32, 2);
        RenderSystem.disableBlend();

        //Render the title
        TextRenderUtil.drawCenteredText(gui, this.getTitle(), this.getArea().centerX(), 10, 0xFFFFFF, true);

        //Render the rest of the background
        this.renderAdditionalBG(gui);


    }

    @Override
    public Component getTitle() {
        MutableComponent partialTitle = EasyText.empty();
        List<Component> titleSections = this.getTitleSections();
        if (titleSections.isEmpty())
            return partialTitle;
        if (titleSections.size() == 1)
            return titleSections.get(0);
        for (int i = 0; i < titleSections.size(); ++i) {
            if (i > 0)
                partialTitle.append(LCText.CONFIG_TITLE_SEPERATOR.get());
            partialTitle.append(titleSections.get(i));
        }
        return partialTitle;
    }

    protected abstract List<Component> getTitleSections();

    @Override
    public boolean blockInventoryClosing() {
        return true;
    }

    protected void renderAdditionalBG(EasyGuiGraphics gui) {
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

}
