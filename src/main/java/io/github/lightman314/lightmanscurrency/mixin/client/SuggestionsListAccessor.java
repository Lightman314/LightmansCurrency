package io.github.lightman314.lightmanscurrency.mixin.client;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestions.SuggestionsList.class)
public interface SuggestionsListAccessor {

    @Accessor("rect")
    Rect2i getRect();
    @Mutable
    @Accessor("rect")
    void setRect(Rect2i rect);

}
