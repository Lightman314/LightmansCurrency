package io.github.lightman314.lightmanscurrency.client.helpers;

import net.minecraft.client.KeyMapping;

public class KeyInputHelpers {

    public static boolean consumeAllClicks(KeyMapping key) {
        boolean pressed = false;
        while(key.consumeClick())
            pressed = true;
        return pressed;
    }

}