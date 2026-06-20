package io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner;

import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;

public enum WidgetFacing {
    LEFT,TOP,RIGHT,BOTTOM;

    WidgetFacing nextClockwise() { return EnumHelper.enumFromOrdinal(this.ordinal() + 1,WidgetFacing.values(),WidgetFacing.LEFT); }
    WidgetFacing nextCounterClockwise() { return EnumHelper.enumFromOrdinal(this.ordinal() - 1,WidgetFacing.values(),WidgetFacing.BOTTOM); }

}