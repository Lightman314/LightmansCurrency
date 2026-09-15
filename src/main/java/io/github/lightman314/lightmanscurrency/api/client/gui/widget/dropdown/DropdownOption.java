package io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

/**
 * Data defining the option to be displayed by the dropdown
 * @param visible A test to determine if the option should be visible. If it returns false, the button will be hidden
 * @param label The text to be displayed on the dropdown button.
 * @param icon An optional 8x8 sprite to be displayed to the left of the text.
 */
public record DropdownOption(BooleanSupplier visible,Component label,@Nullable Identifier icon) {

    public boolean isVisible() { return this.visible.getAsBoolean(); }

    public DropdownOption(BooleanSupplier visible,Component label) { this(visible,label,null); }
    public DropdownOption(Component label) { this(label,null); }
    public DropdownOption(Component label,@Nullable Identifier icon) { this(() -> true,label,icon); }

    public DropdownOption withVisibleCheck(BooleanSupplier visible) { return new DropdownOption(visible,this.label,this.icon); }

}