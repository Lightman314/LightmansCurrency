package io.github.lightman314.lightmanscurrency.client.easy;

import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Utility to allow easy porting of 1.19.3 button builders to older versions.
 */
public class EasyButton {

    public static ButtonBuilder builder(Component text, Button.OnPress press) { return new ButtonBuilder(text, press); }

    public static class ButtonBuilder
    {
        private final Button.OnPress press;
        private final Component text;

        private ScreenArea area = ScreenArea.of(ScreenPosition.ZERO, 100, 20);

        private ButtonBuilder(Component text, Button.OnPress press) { this.text = text; this.press = press; }

        public ButtonBuilder pos(int x, int y) { this.area = this.area.atPosition(ScreenPosition.of(x,y)); return this; }
        public ButtonBuilder size(int width, int height) { this.area = this.area.ofSize(width, height); return this; }

        public Button build() { return new Button(area.x, area.y, area.width, area.height, this.text, this.press); }

    }

}
