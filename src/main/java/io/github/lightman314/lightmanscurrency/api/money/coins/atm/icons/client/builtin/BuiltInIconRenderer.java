package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.client.builtin;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.api.client.sprites.builtin.WidgetStateSprite;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.ATMItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.SimpleArrowIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.SimpleArrowIcon.ArrowType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.client.ATMIconRenderer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class BuiltInIconRenderer extends ATMIconRenderer {

    public static ATMIconRenderer INSTANCE = new BuiltInIconRenderer();

    private BuiltInIconRenderer() {
        ImmutableMap.Builder<ArrowType,ArrowSprites> builder = ImmutableMap.builderWithExpectedSize(ArrowType.values().length);
        for(ArrowType type : ArrowType.values())
            builder.put(type,new ArrowSprites(type));
        this.arrowSprites = builder.buildKeepingLast();
    }

    private final Map<ArrowType,ArrowSprites> arrowSprites;

    @Override
    public void render(ATMExchangeButton button, ATMIconData icon, EasyGuiGraphics gui, boolean isHovered) {
        if(icon instanceof ATMItemIcon ii)
            gui.renderItem(ii.item, icon.xPos, icon.yPos, "");
        else if(icon instanceof SimpleArrowIcon sai)
            this.arrowSprites.get(sai.direction).getSprite().render(gui,icon.xPos,icon.yPos,true,isHovered);
        else if(icon instanceof SpriteIcon si)
        {
            if(si.sprite == null)
                si.sprite = new NormalSprite(si.data);
            if(si.sprite instanceof FixedSizeSprite sprite)
                sprite.render(gui,icon.xPos,icon.yPos,true,isHovered);
        }
    }

    private static class ArrowSprites
    {
        private final Supplier<FixedSizeSprite> sprite;
        public ArrowSprites(ArrowType type)
        {
            this.sprite = Suppliers.memoize(() -> WidgetStateSprite.lazyHoverable(getSpriteID(type),6,6));
        }

        private static ResourceLocation getSpriteID(ArrowType type) { return LightmansCurrency.id("common/widgets/atm_arrow_" + type.name().toLowerCase(Locale.ENGLISH)); }
        private FixedSizeSprite getSprite() { return this.sprite.get(); }

    }

}
