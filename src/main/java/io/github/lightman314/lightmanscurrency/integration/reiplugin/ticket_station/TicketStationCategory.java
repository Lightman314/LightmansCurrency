package io.github.lightman314.lightmanscurrency.integration.reiplugin.ticket_station;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TicketStationScreen;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class TicketStationCategory implements DisplayCategory<TicketStationDisplay> {

    public static final CategoryIdentifier<TicketStationDisplay> ID = CategoryIdentifier.of(LightmansCurrency.MODID,"ticket_station");
    public static final TicketStationCategory INSTANCE = new TicketStationCategory();

    private TicketStationCategory() {}

    @Override
    public CategoryIdentifier<? extends TicketStationDisplay> getCategoryIdentifier() { return ID; }

    @Override
    public Component getTitle() { return LCText.GUI_TICKET_STATION_TITLE.get(); }

    @Override
    public Renderer getIcon() { return EntryStacks.of(ModBlocks.TICKET_STATION.get()); }

    @Override
    public List<Widget> setupDisplay(TicketStationDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();

        Point startPoint = new Point(bounds.getCenterX() - 59, bounds.getCenterY() - 20);
        widgets.add(Widgets.createRecipeBase(bounds));

        //Background
        widgets.add(Widgets.createTexturedWidget(TicketStationScreen.GUI_TEXTURE, startPoint.x, startPoint.y, 0, TicketStationScreen.HEIGHT, 118, 40));

        //Modifier Slot
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 1, startPoint.y + 5))
                .entries(display.getInputEntries().get(0))
                .disableBackground()
                .markInput());

        //Ingredient Slot
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 37, startPoint.y + 5))
                .entries(display.getInputEntries().get(1))
                .disableBackground()
                .markInput());

        //Output Slot
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 97, startPoint.y + 5))
                .entries(display.getOutputEntries().get(0))
                .disableBackground()
                .markOutput());

        //Code Input
        if(display.codeInputs)
        {
            widgets.add(Widgets.createTexturedWidget(TicketStationScreen.GUI_TEXTURE,startPoint.x,startPoint.y + 26,0,TicketStationScreen.HEIGHT + 40,107,14));
            widgets.add(Widgets.createLabel(new Point(startPoint.x + 3, startPoint.y + 29), EasyText.literal("ExampleCode")));
        }
        //Durability Input
        if(display.durabilityData.isValid())
        {
            widgets.add(Widgets.createTexturedWidget(TicketStationScreen.GUI_TEXTURE,startPoint.x + 107,startPoint.y + 26,107,TicketStationScreen.HEIGHT + 40,11,14));
            List<Component> lines = new ArrayList<>();
            int min = display.durabilityData.min;
            int max = display.durabilityData.max;
            boolean allowInfinite = display.durabilityData.allowInfinite || min <= 0;
            if(min <= 0)
                min = 1;
            lines.add(LCText.JEI_INFO_TICKET_DURABILITY.get(min,max));
            if(allowInfinite)
                lines.add(LCText.JEI_INFO_TICKET_DURABILITY_INFINITE.get());
            widgets.add(Widgets.createTooltip(new Rectangle(startPoint.x + 107,startPoint.y + 26,11,14),lines));
        }

        return widgets;
    }
}