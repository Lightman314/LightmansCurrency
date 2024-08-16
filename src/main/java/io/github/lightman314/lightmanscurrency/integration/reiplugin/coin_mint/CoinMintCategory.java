package io.github.lightman314.lightmanscurrency.integration.reiplugin.coin_mint;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MintScreen;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class CoinMintCategory implements DisplayCategory<CoinMintDisplay> {

    public static final CategoryIdentifier<CoinMintDisplay> ID = CategoryIdentifier.of(LightmansCurrency.MODID,"coin_mint");
    public static final CoinMintCategory INSTANCE = new CoinMintCategory();

    private CoinMintCategory() {}

    @Override
    public CategoryIdentifier<CoinMintDisplay> getCategoryIdentifier() { return ID; }

    @Override
    public Component getTitle() { return LCText.GUI_COIN_MINT_TITLE.get(); }

    @Override
    public Renderer getIcon() { return EntryStacks.of(ModBlocks.COIN_MINT.get()); }

    @Override
    public List<Widget> setupDisplay(CoinMintDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();

        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 13);
        widgets.add(Widgets.createRecipeBase(bounds));

        //Background
        widgets.add(Widgets.createTexturedWidget(MintScreen.GUI_TEXTURE, startPoint.x, startPoint.y, 55, 16, 82, 26));

        //Input Slot
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 1, startPoint.y + 5))
                .entries(display.getInputEntries().get(0))
                .disableBackground()
                .markInput());

        //Output Slot
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 5))
                .entries(display.getOutputEntries().get(0))
                .disableBackground()
                .markOutput());

        //Animated Arrow
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 24, startPoint.y + 4))
                .animationDurationTicks(display.recipe.getDuration()));

        if(!display.recipe.allowed())
        {
            //Disabled alert
            widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getMinY() + 4), LCText.TOOLTIP_COIN_MINT_DISABLED_TOP.getWithStyle(ChatFormatting.BOLD, ChatFormatting.RED))
                    .centered().noShadow());
            widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getMaxY() - 12), LCText.TOOLTIP_COIN_MINT_DISABLED_BOTTOM.getWithStyle(ChatFormatting.BOLD, ChatFormatting.RED))
                    .centered().noShadow());
        }

        return widgets;
    }

}