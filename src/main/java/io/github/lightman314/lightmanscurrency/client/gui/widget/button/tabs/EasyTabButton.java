package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tabs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs.EasyTabRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class EasyTabButton extends Button implements ITooltipWidget {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/tabs.png");

    public static final int SIZE = 20;

    public final EasyTab<?> tab;
    EasyTabRotation rotation = EasyTabRotation.TOP;

    public EasyTabButton(EasyTab<?> tab, Button.OnPress pressable) {
        super(0,0, SIZE, SIZE, Component.empty(), pressable, new Narration(tab));
        this.tab = tab;
    }

    public void setRotation(EasyTabRotation rotation) { this.rotation = rotation; }

    @Override
    public List<Component> getTooltip() { return this.tab.getTooltip(); }

    private record Narration(EasyTab<?> tab) implements CreateNarration {
        @Override
        public @NotNull MutableComponent createNarrationMessage(@NotNull Supplier<MutableComponent> buttonText) {
            MutableComponent result = Component.empty();
            for (Component line : this.tab.getTooltip()) {
                if (!result.getString().isEmpty())
                    result.append(Component.literal("\n"));
                result.append(line);
            }
            return result;
        }
    }

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
