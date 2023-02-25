package io.github.lightman314.lightmanscurrency.client.gui.widget.easy.button;

import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;

import java.util.function.Consumer;

public abstract class EasyButton extends EasyWidget {

    private final Consumer<Object> onClick;

    protected EasyButton(IEasyButtonBuilder builder) {
        super(builder);
        this.onClick = builder.getClickConsumer();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button == 0 && this.isMouseOver(mouseX, mouseY))
        {
            try{
                //Play click noise
                SoundManager sm = Minecraft.getInstance().getSoundManager();
                sm.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                //Trigger click consumer
                this.onClick.accept(this);
                return true;
            } catch(Throwable ignored) {}
        }
        return false;
    }

}
