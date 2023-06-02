package io.github.lightman314.lightmanscurrency.client.easy;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class EasyButton {

    public static Button.Builder builder(Component text, Button.OnPress press) { return Button.builder(text, press); }

}
