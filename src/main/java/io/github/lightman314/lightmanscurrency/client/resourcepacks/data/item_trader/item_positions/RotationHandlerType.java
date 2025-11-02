package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.ResourceLocationException;

public interface RotationHandlerType {

    RotationHandler parse(JsonObject json) throws JsonSyntaxException, ResourceLocationException;

}