package io.github.lightman314.lightmanscurrency.api.enchantments;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.enchantments.CoinMagnetEnchantment;
import io.github.lightman314.lightmanscurrency.common.enchantments.MoneyMendingEnchantment;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnchantmentUtil {

    private EnchantmentUtil() {}

    /**
     * Ticks the entities Money Mending &amp; Coin Magnet enchantments.
     * @param entity The entity to run the enchantment ticks on.
     * @param entityMoney The entities money access, used to pay for Money Mending repairs.<br>
     *                    If <code>null</code> Money Mending cannot and will not be run for this entity unless it has a valid {@link IMoneyHandler} capability
     */
    public static void tickAllEnchantments(@Nonnull LivingEntity entity, @Nullable IMoneyHandler entityMoney)
    {
        tickCoinMagnet(entity);
        if(entityMoney == null)
        {
            //Try to get a money handler capability from the entity
            entityMoney = entity.getCapability(CapabilityMoneyHandler.MONEY_HANDLER_ENTITY);
            if(entityMoney == null)
                LightmansCurrency.LogDebug("Cannot run Money Mending tick without a money handler!");
        }
        if(entityMoney != null)
            tickMoneyMending(entity,entityMoney);
    }

    /**
     * Ticks the entitys Coin Magnet enchantments if they have an {@link IMoneyHandler equipped wallet capability} attached, and said wallet has the Coin Magnet enchantment.
     * @param entity The entity to run the enchantment tick on.
     */
    public static void tickCoinMagnet(@Nonnull LivingEntity entity)
    {
        WalletHandler walletHandler = WalletHandler.get(entity);
        if(walletHandler != null)
            CoinMagnetEnchantment.runEntityTick(walletHandler,entity);
    }

    /**
     * Ticks the entitys Money Mending enchantments.
     * @param entity The entity to run the enchantment tick on.
     * @param entityMoney The entities money access, used to pay for the Money Mending repairs.
     */
    public static void tickMoneyMending(@Nonnull LivingEntity entity, @Nonnull IMoneyHandler entityMoney)
    {
        MoneyMendingEnchantment.runEntityTick(entity, entityMoney);
    }

}
