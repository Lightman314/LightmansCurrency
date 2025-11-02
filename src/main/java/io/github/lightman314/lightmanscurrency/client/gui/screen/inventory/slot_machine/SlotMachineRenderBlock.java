package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.slot_machine;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class SlotMachineRenderBlock {

    private static final SlotMachineRenderBlock EMPTY = forIcon(0, SlotMachineEntry.DEFAULT_ICON);

    public void render(EasyGuiGraphics gui, int x, int y) { this.icon.render(gui,x,y); }
    public final int weight;
    private final IconData icon;
    private SlotMachineRenderBlock(double odds,IconData icon) { this(oddsToWeight(odds),icon); }
    private SlotMachineRenderBlock(int weight,IconData icon) { this.weight = weight; this.icon = icon; }

    public static int oddsToWeight(double odds) { return (int)Math.round(odds * 100d); }

    public static SlotMachineRenderBlock empty() { return EMPTY; }
    public static SlotMachineRenderBlock forIcon(double odds,IconData icon) { return new SlotMachineRenderBlock(odds,icon); }

}