package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SectionLabel extends EasyWidget implements ITooltipWidget {

    private final ResourceLocation file;
    private final ConfigFile.ConfigSection section;
    protected SectionLabel(Builder builder) {
        super(builder);
        this.file = builder.file;
        this.section = builder.section;
    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {
        TextRenderUtil.drawCenteredText(gui,this.section.getDisplayName(this.file),this.getWidth() / 2,10,0xFFFFFF,true);
    }

    public static SectionLabel create(ConfigFile file, ConfigFile.ConfigSection section) { return new Builder(file.getFileID(),section).build(); }

    @Override
    public List<Component> getTooltipText() { return TooltipHelper.splitTooltips(this.section.getTooltips(this.file)); }

    protected static class Builder extends EasyBuilder<Builder>
    {
        private final ResourceLocation file;
        private final ConfigFile.ConfigSection section;
        private Builder(ResourceLocation file, ConfigFile.ConfigSection section) { super(OptionWidget.WIDTH,OptionWidget.HEIGHT); this.file = file; this.section = section; }
        @Override
        protected Builder getSelf() { return this; }
        public SectionLabel build() { return new SectionLabel(this); }
    }

}