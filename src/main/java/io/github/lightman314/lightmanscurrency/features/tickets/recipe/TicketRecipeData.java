package io.github.lightman314.lightmanscurrency.features.tickets.recipe;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;

public record TicketRecipeData(String code, int durability) implements FancyPacketMap.FancyPacketWriter {

    public static final TicketRecipeData DEFAULT = new TicketRecipeData("",1);

    public TicketRecipeData withCode(String code) { return new TicketRecipeData(code,this.durability); }
    public TicketRecipeData withDurability(int durability) { return new TicketRecipeData(this.code,durability); }

    public static TicketRecipeData readData(FancyPacketMap packet) {
        return new TicketRecipeData(packet.getString("code"),packet.getInt("durability"));
    }

    @Override
    public void writeToPacket(FancyPacketMap.Mutable packet) {
        packet.setString("code",this.code);
        packet.setInt("durability",this.durability);
    }

}
