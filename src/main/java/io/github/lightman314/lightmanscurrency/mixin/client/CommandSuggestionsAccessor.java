package io.github.lightman314.lightmanscurrency.mixin.client;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CommandSuggestions.class)
public interface CommandSuggestionsAccessor {

    @Accessor("commandUsage")
    List<FormattedCharSequence> getCommandUsage();
    @Accessor("suggestions")
    CommandSuggestions.SuggestionsList getSuggestions();
    @Accessor("fillColor")
    int getFillColor();

}
