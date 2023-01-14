package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tabs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs.EasyTabRotation;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EasyTabButton extends Button implements ITooltipWidget {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/tabs.png");

    public static final int SIZE = 20;

    public final EasyTab<?> tab;
    EasyTabRotation rotation = EasyTabRotation.TOP;

    public final int getX() { return this.x; }
    public final int getY() { return this.y; }

    public EasyTabButton(EasyTab<?> tab, Button.OnPress pressable) {
        super(0,0, SIZE, SIZE, EasyText.empty(), pressable);
        this.tab = tab;
    }

    public void setRotation(EasyTabRotation rotation) { this.rotation = rotation; }

    @Override
    public List<Component> getTooltip() { return this.tab.getTooltip(); }

    @Override
    public void renderButton(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTick) {

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        float r = (float)(this.tab.getTabColor() >> 16 & 255) / 255f;
        float g = (float)(this.tab.getTabColor() >> 8 & 255) / 255f;
        float b = (float)(this.tab.getTabColor() & 255) / 255f;
        float activeColor = this.active ? 1f : 0.5f;
        RenderSystem.setShaderColor(r * activeColor, g * activeColor, b * activeColor, 1f);

        //Render the background
        this.blit(pose, this.getX(), this.getY(), this.rotation.x, 0, this.width, this.height);

        //Render the icon
        this.tab.getTabIcon().render(pose, this, Minecraft.getInstance().font, this.getX() + 2, this.getY() + 2);

    }


}