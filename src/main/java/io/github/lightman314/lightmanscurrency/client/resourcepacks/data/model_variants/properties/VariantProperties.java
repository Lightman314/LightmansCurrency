package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties;

import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin.*;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.simple.*;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantProperties {

    public static final VariantProperty<ItemPositionDataEntry> ITEM_POSITION_DATA = ItemPositionDataEntry.PROPERTY;

    public static final VariantProperty<InputDisplayOffset> INPUT_DISPLAY_OFFSET = InputDisplayOffset.PROPERTY;

    public static final VariantProperty<TooltipInfo> TOOLTIP_INFO = TooltipInfo.PROPERTY;

    public static final VariantPropertyWithDefault<ShowInCreative> SHOW_IN_CREATIVE = ShowInCreative.PROPERTY;

    public static final BooleanProperty HIDDEN = new BooleanProperty();

}