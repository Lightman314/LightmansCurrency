package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class InventoryButton extends PlainButton {

    protected final boolean isCreativeScreen;
    protected final AbstractContainerScreen<?> inventoryScreen;
    private ScreenPosition getScreenCorner() { return ScreenPosition.getScreenCorner(this.inventoryScreen); }

    protected InventoryButton(AbstractContainerScreen<?> inventoryScreen, Runnable pressable, FixedSizeSprite sprite) { this(inventoryScreen, pressable, () -> sprite); }
    protected InventoryButton(AbstractContainerScreen<?> inventoryScreen, Runnable pressable, Supplier<FixedSizeSprite> sprite) {
        super(PlainButton.builder().pressAction(pressable).sprite(sprite));
        this.inventoryScreen = inventoryScreen;
        this.isCreativeScreen = this.inventoryScreen instanceof CreativeModeInventoryScreen;
    }

    protected abstract ScreenPosition getPositionOffset(boolean isCreativeScreen);

    protected boolean canShow() { return true; }

    @Override
    protected void renderTick() {
        if(this.inventoryScreen instanceof CreativeModeInventoryScreen cs)
            this.visible = cs.isInventoryOpen() && this.canShow();
        else
            this.visible = this.canShow();

        //We don't care about the position if we're not visible
        if(!this.visible)
            return;
        //Reposition the button based on the containers top/left most position
        this.setPosition(this.getScreenCorner().offset(this.getPositionOffset(this.isCreativeScreen)));
    }
}
