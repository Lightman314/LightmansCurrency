package io.github.lightman314.lightmanscurrency.integration.computercraft.data.builtin;

import dan200.computercraft.api.lua.LuaException;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.AncientCoinItem;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.BasicItemParser;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class AncientCoinParser implements BasicItemParser {

    public static final BasicItemParser INSTANCE = new AncientCoinParser();
    private AncientCoinParser() {}

    @Override
    public void modifyResult(ItemStack stack, Map<?, ?> table) throws LuaException {
        if(stack.getItem() instanceof AncientCoinItem ancientCoin)
        {
            try {
                String typeString = (String)table.get("AncientCoinType");
                AncientCoinType type = EnumUtil.enumFromString(typeString,AncientCoinType.values(),null);
                if(type == null)
                    throw new LuaException(typeString + " is not a valid 'AncientCoinType' entry!");
                stack.set(ModDataComponents.ANCIENT_COIN_TYPE,type);
            } catch (ClassCastException ignored) { throw new LuaException("Ancient Coin items require a valid 'AncientCoinType' entry!"); }
        }
    }
}