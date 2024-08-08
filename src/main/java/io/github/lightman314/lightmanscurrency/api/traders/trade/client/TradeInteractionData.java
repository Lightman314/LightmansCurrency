package io.github.lightman314.lightmanscurrency.api.traders.trade.client;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import javax.annotation.Nonnull;

public record TradeInteractionData(int localMouseX, int localMouseY, int mouseButton, boolean shiftHeld, boolean ctrlHeld, boolean altHeld) {

    @Nonnull
    public LazyPacketData.Builder encode(@Nonnull LazyPacketData.Builder builder)
    {
        builder.setInt("MouseX",this.localMouseX);
        builder.setInt("MouseY",this.localMouseY);
        builder.setInt("MouseButton",this.mouseButton);
        builder.setBoolean("ShiftHeld",this.shiftHeld);
        builder.setBoolean("CtrlHeld",this.ctrlHeld);
        builder.setBoolean("AltHeld",this.altHeld);
        return builder;
    }

    @Nonnull
    public static TradeInteractionData decode(@Nonnull LazyPacketData data)
    {
        return new TradeInteractionData(data.getInt("MouseX"), data.getInt("MouseY"), data.getInt("MouseButton"),data.getBoolean("ShiftHeld"),data.getBoolean("CtrlHeld"),data.getBoolean("AltHeld"));
    }

}
