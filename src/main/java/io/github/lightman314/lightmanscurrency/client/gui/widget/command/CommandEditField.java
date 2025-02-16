package io.github.lightman314.lightmanscurrency.client.gui.widget.command;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IKeyboardListener;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.mixin.client.CommandSuggestionsAccessor;
import io.github.lightman314.lightmanscurrency.mixin.client.SuggestionsListAccessor;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandEditField extends EasyWidgetWithChildren implements IKeyboardListener, IMouseListener, IScrollListener  {


    private final CommandEditField oldWidget;
    private final Consumer<String> handler;
    private final int suggestionCount;

    protected CommandEditField(Builder builder) {
        super(builder);
        this.oldWidget = builder.oldWidget;
        this.handler = builder.handler;
        this.suggestionCount = builder.suggestionCount;
    }

    private EditBox textbox;
    private CommandSuggestions commandSuggestions;

    @Override
    protected void renderTick() {
        if(this.textbox != null)
            this.textbox.visible = this.visible;
    }

    @Override
    public void addChildren(ScreenArea area) {

        this.textbox = this.addChild(new EditBox(this.getFont(), this.getX(), this.getY(), this.width, this.height, EasyText.empty()));
        this.textbox.setMaxLength(32500);
        this.textbox.setResponder(this::onCommandChanged);
        this.textbox.visible = this.visible;

        this.commandSuggestions = new CommandSuggestions(Minecraft.getInstance(),Minecraft.getInstance().screen,this.textbox,this.getFont(),true,true,0,this.suggestionCount,false,Integer.MIN_VALUE);
        this.commandSuggestions.setAllowSuggestions(true);
        this.commandSuggestions.updateCommandInfo();

        if(this.oldWidget != null && this.oldWidget.textbox != null)
            this.textbox.setValue(this.oldWidget.textbox.getValue());

    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {
        this.repositionSuggestions();
        if(!this.commandSuggestions.renderSuggestions(gui.getGui(),gui.mousePos.x,gui.mousePos.y))
        {
            //Manually render usage so that we get the right y-pos
            if(this.commandSuggestions instanceof CommandSuggestionsAccessor sa)
            {
                int i = 0;
                int fillColor = sa.getFillColor();
                for (FormattedCharSequence formattedcharsequence : sa.getCommandUsage()) {
                    Font font = gui.font;
                    if(formattedcharsequence instanceof FormattedText text)
                    {
                        for(FormattedCharSequence line : font.split(text,this.width))
                        {
                            int j = 18 + 12 * i;
                            gui.fill(0, j, this.width, 12, fillColor);
                            gui.drawShadowed(line, 0, j + 2, -1);
                            i++;
                        }
                    }
                    else
                    {
                        int j = 18 + 12 * i;
                        gui.fill(0, j, this.width, 12, fillColor);
                        gui.drawShadowed(formattedcharsequence, 0, j + 2, -1);
                        i++;
                    }
                }
            }
        }
    }

    public void setCommand(String newCommand) {
        if(this.textbox != null)
            this.textbox.setValue(newCommand);
    }

    private void onCommandChanged(String newCommand) {
        this.commandSuggestions.updateCommandInfo();
        this.handler.accept(newCommand);
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        return this.commandSuggestions.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        this.repositionSuggestions();
        if(!this.commandSuggestions.mouseClicked(mouseX,mouseY,button))
        {
            if(!this.textbox.mouseClicked(mouseX,mouseY,button))
                this.textbox.setFocused(false);
            return false;
        }
        return true;
    }

    private void repositionSuggestions() {
        if(this.commandSuggestions instanceof CommandSuggestionsAccessor sa)
        {
            CommandSuggestions.SuggestionsList suggestions = sa.getSuggestions();
            if(suggestions instanceof SuggestionsListAccessor sla)
            {
                Rect2i rect = sla.getRect();
                //x-position is typically accurate, so we only need to correct the y-position to start at 18px below the input box
                sla.setRect(new Rect2i(rect.getX(),this.getY() + 18, rect.getWidth(),rect.getHeight()));
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) { return this.commandSuggestions.mouseScrolled(delta); }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends EasySizableBuilder<Builder>
    {

        @Nullable
        private CommandEditField oldWidget = null;
        private Consumer<String> handler = s -> {};
        private int suggestionCount = 5;

        private Builder() { super(100,18); }

        @Override
        protected Builder getSelf() { return this; }

        public Builder oldIfNotFirst(boolean firstOpen, @Nullable CommandEditField oldWidget) { if(!firstOpen) this.oldWidget = oldWidget; return this; }

        public Builder handler(Consumer<String> handler) { this.handler = handler; return this; }

        public Builder suggestions(int suggestionCount) { this.suggestionCount = suggestionCount; return this; }

        public CommandEditField build() { return new CommandEditField(this); }

    }

}
