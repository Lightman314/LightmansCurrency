package io.github.lightman314.lightmanscurrency.api.money;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.PlayerMoneyHolder;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Utility class with Money Related utilities such as processing a payment to or from a player,
 * registering custom {@link CurrencyType}'s, etc.
 */
public final class MoneyAPI {

    public static final String MODID = "lightmanscurrency";

    private static final Map<ResourceLocation, CurrencyType> REGISTERED_TYPES = new HashMap<>();
    private static final Map<UUID,PlayerMoneyHolder> CLIENT_PLAYER_CACHE = new HashMap<>();
    private static final Map<UUID,PlayerMoneyHolder> SERVER_PLAYER_CACHE = new HashMap<>();

    private MoneyAPI() {}

    /**
     * Returns a list of all registered currency types
     */
    @Nonnull
    public static List<CurrencyType> getAllCurrencyTypes() { return ImmutableList.copyOf(REGISTERED_TYPES.values()); }

    /**
     * Returns the {@link CurrencyType} registered with the given id.
     * Will return <code>null</code> if no type was registered with that id.
     */
    @Nullable
    public static CurrencyType getCurrencyType(@Nonnull ResourceLocation id) { return REGISTERED_TYPES.get(id); }

    /**
     * Registers the given {@link CurrencyType} to the system.
     * Required before loading any custom {@link MoneyValue} data.
     * I recommend registering these during the {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent FMLCommonSetupEvent}
     */
    public static void registerCurrencyType(@Nonnull CurrencyType type)
    {
        if(REGISTERED_TYPES.containsKey(type.getType()))
        {
            CurrencyType existingType = REGISTERED_TYPES.get(type.getType());
            if(existingType == type)
                LightmansCurrency.LogWarning("Money Type " + type.getType() + " was registered twice!");
            else
                LightmansCurrency.LogError("Tried to registerNotification Money Type " + type.getType() + ", but another type has already been registered under that id!");
            return;
        }
        REGISTERED_TYPES.put(type.getType(),type);
        LightmansCurrency.LogDebug("Registered Currency Type: " + type.getType());
    }

    ///Player related functions

    /**
     * Gets a safe {@link io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder} for the given player.
     * Should not be saved locally unless used in a situation where the player is guaranteed to not leave the dimension while in-use (such as a menu/screen).
     * Use this over
     */
    @Nonnull
    public static PlayerMoneyHolder getPlayersMoneyHolder(@Nonnull Player player)
    {
        Map<UUID,PlayerMoneyHolder> map = player.level.isClientSide ? CLIENT_PLAYER_CACHE : SERVER_PLAYER_CACHE;
        if(!map.containsKey(player.getUUID()))
            map.put(player.getUUID(), new PlayerMoneyHolder(player));
        return map.get(player.getUUID()).updatePlayer(player);
    }


    /**
     * Returns all funds available to the player.
     */
    @Nonnull
    public static MoneyView getPlayersAvailableFunds(@Nonnull Player player) { return getPlayersMoneyHolder(player).getStoredMoney(); }

    /**
     * Whether the player can afford the given price with the funds on hand.
     */
    public static boolean canPlayerAfford(@Nonnull Player player, @Nonnull MoneyValue price) {
        return getPlayersAvailableFunds(player).containsValue(price);
    }

    /**
     * Adds the given money amount to the players funds.
     */
    public static void giveMoneyToPlayer(@Nonnull Player player, @Nonnull MoneyValue value)
    {
        CurrencyType currencyType = value.getCurrency();
        if(currencyType == null)
            return;
        currencyType.giveMoneyToPlayer(player, value);
    }

    public static boolean takeMoneyFromPlayer(@Nonnull Player player, @Nonnull MoneyValue value)
    {
        CurrencyType currencyType = value.getCurrency();
        if(currencyType == null)
            return false;
        return currencyType.takeMoneyFromPlayer(player, value);
    }

    ///Container related functions

    /**
     * Obtains the total value of the items stored within the container
     */
    @Nonnull
    public static MoneyView valueOfContainer(@Nonnull List<ItemStack> container) { return valueOfContainer(InventoryUtil.buildInventory(container)); }

    /**
     * Obtains the total value of the items stored within the container
     */
    @Nonnull
    public static MoneyView valueOfContainer(@Nonnull Container container)
    {
        MoneyView.Builder funds = MoneyView.builder();
        for(CurrencyType currencyType : REGISTERED_TYPES.values())
            currencyType.getValueInContainer(container, funds);
        return funds.build();
    }

    /**
     * Whether the given value can be added to the given container under the assumption that space is a non-issue.
     * @param container The {@link Container} to attempt to add money to.
     * @param moneyToAdd The {@link MoneyValue} to attemp to give.
     * @return Whether money can be added to the container or not.
     */
    public static boolean canAddMoneyToContainer(@Nonnull Container container, @Nonnull MoneyValue moneyToAdd)
    {
        CurrencyType type = moneyToAdd.getCurrency();
        if(type != null)
            return type.canAddValueToContainer(container, moneyToAdd);
        return false;
    }


    /**
     * Calls {@link #addMoneyToContainer(Container, Consumer, MoneyValue)} with the default overflow handler of giving the money to the given player.
     * Will not give any money to the players personal money storage.
     */
    public static boolean addMoneyToContainer(@Nonnull Container container, @Nonnull Player player, @Nonnull MoneyValue moneyToAdd)
    {
        return addMoneyToContainer(container, s -> ItemHandlerHelper.giveItemToPlayer(player, s), moneyToAdd);
    }

    /**
     * Adds the given money to the container and only to the container.
     */
    public static boolean addMoneyToContainer(@Nonnull Container container, @Nonnull Consumer<ItemStack> overflowHandler, @Nonnull MoneyValue moneyToAdd) {
        CurrencyType currencyType = moneyToAdd.getCurrency();
        if(currencyType == null)
            return false;
        return currencyType.addValueToContainer(container, moneyToAdd, overflowHandler);
    }

    public static boolean takeMoneyFromContainer(@Nonnull Container container, @Nonnull Player player, @Nonnull MoneyValue moneyToTake)
    {
        return takeMoneyFromContainer(container, s -> ItemHandlerHelper.giveItemToPlayer(player,s), moneyToTake);
    }

    public static boolean takeMoneyFromContainer(@Nonnull Container container, @Nonnull Consumer<ItemStack> overflowHandler, @Nonnull MoneyValue moneyToTake) {
        CurrencyType currencyType = moneyToTake.getCurrency();
        if(currencyType == null)
            return false;
        return currencyType.takeValueFromContainer(container, moneyToTake, overflowHandler);
    }

}
