package io.github.lightman314.lightmanscurrency.api.misc;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class QuarantineAPI {

    private QuarantineAPI() {}

    public static boolean IsDimensionQuarantined(Item.TooltipContext context) {
        Level level = context.level();
        return level != null && IsDimensionQuarantined(level);
    }
    public static boolean IsDimensionQuarantined(EasyMenu menu) { return IsDimensionQuarantined(menu.player); }
    public static boolean IsDimensionQuarantined(Entity entity) { return IsDimensionQuarantined(entity.level()); }
    public static boolean IsDimensionQuarantined(BlockEntity be) { return IsDimensionQuarantined(be.getLevel()); }
    public static boolean IsDimensionQuarantined(Level level) { return IsDimensionQuarantined(level.dimension()); }
    public static boolean IsDimensionQuarantined(ResourceKey<Level> level) { return IsDimensionQuarantined(level.location()); }
    public static boolean IsDimensionQuarantined(ResourceLocation dimension) { return LCConfig.SERVER.quarantinedDimensions.get().contains(dimension); }

}
