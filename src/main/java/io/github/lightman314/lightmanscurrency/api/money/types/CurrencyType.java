package io.github.lightman314.lightmanscurrency.api.money.types;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;

/**
 * A MoneyType class for use with registering
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class CurrencyType {

    protected CurrencyType(ResourceLocation type) {
        this.type = type;
    }

    private final ResourceLocation type;

    public final ResourceLocation getType() { return this.type; }

    /**
     * A quick method to sum a list of money values for {@link MoneyView} purposes
     * Can be modified by the {@link CurrencyType} to make the math more efficient, as this often requires adding 20-30 values
     * together when querying the contents of a container.
     */
    public final MoneyValue sumValues(List<MoneyValue> values)
    {
        if(values.isEmpty())
            return MoneyValue.empty();
        if(values.size() == 1)
            return values.get(0);
        return this.sumValuesInternal(values);
    }

    /**
     * Method used by {@link #sumValues(List)} when the list size is greater than 1<br>
     * Override this to optimize your addition calculations should your {@link MoneyValue} init
     * function do complicated math.
     */
    protected abstract MoneyValue sumValuesInternal(List<MoneyValue> values);

    /**
     * Method used by {@link MoneyView#getAllText(ChatFormatting...)} to get the tooltip lines for a given group of money values<br>
     * Can be overridden to combine similar money values into a single tooltip line to save space.<br>
     * By default, will simply add a new line for each money value of this currency type
     */
    public void getGroupTooltip(MoneyView money, Consumer<MutableComponent> lineConsumer)
    {
        for(MoneyValue val : money.allValues())
        {
            if(val.getCurrency() == this)
                lineConsumer.accept(val.getText());
        }
    }

    /**
     * Method used by {@link io.github.lightman314.lightmanscurrency.api.money.MoneyAPI#GetPlayersMoneyHandler(Player) MoneyAPI#GetPlayersMoneyHandler(Player)} to create the universal {@link io.github.lightman314.lightmanscurrency.common.impl.PlayerMoneyHolder PlayerMoneyHolder} for said player.<br>
     * Method is {@link Nullable} and should return null if it is not possible for the player to <b>ever</b> handle money of this type.
     */
    @Nullable
    public abstract IPlayerMoneyHandler createMoneyHandlerForPlayer(Player player);

    /**
     * Method used by {@link io.github.lightman314.lightmanscurrency.api.money.MoneyAPI#GetPlayerMoneyHandlerUnsafe(Player) MoneyAPI#GetPlayerMoneyHandlerUnsafe(Player)} to create a universal {@link io.github.lightman314.lightmanscurrency.common.impl.PlayerMoneyHolder PlayerMonetHolder} for said player.<br>
     * The {@link IPlayerMoneyHandler} returned should not utilize the players inventory as an item overflow if the money-related items did not fit in their wallet, etc.<br>
     * Method is {@link Nullable} and should return null if it is not possible for the player to <b>ever</b> handle money of this type.
     */
    @Nullable
    public IPlayerMoneyHandler createUnsafeMoneyHandlerForPlayer(Player player) { return this.createMoneyHandlerForPlayer(player); }

    /**
     * Method used by {@link io.github.lightman314.lightmanscurrency.api.money.MoneyAPI#GetContainersMoneyHandler(Container, Consumer, IClientTracker) MoneyAPI#GetContainersMoneyHandler(Container, Consumer)} to create a combined {@link IMoneyHandler} for said container using the provided {@link Consumer itemOverflowHandler} to handle any items that won't fit in the container<br>
     * Method is {@link Nullable} and should return null if it is not possible for money of this type to be stored or handled in an item form that doesn't have the {@link io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyHandler IMoneyHandler} item capability
     */
    @Nullable
    public abstract IMoneyHandler createMoneyHandlerForContainer(Container container, Consumer<ItemStack> overflowHandler, IClientTracker tracker);

    /**
     * Method used by {@link io.github.lightman314.lightmanscurrency.api.money.MoneyAPI#GetATMMoneyHandler(Player, Container) MoneyAPI#GetATMMoneyHandler(Player, Container)} to create a combined {@link IMoneyHandler} for the ATM's container for use with depositing & withdrawing money from a players bank account<br>
     * Default implementation returns the results of {@link #createMoneyHandlerForContainer(Container,Consumer,IClientTracker)}<br>
     * Override if your mods money is directly attached to the player in some non-item method
     */
    @Nullable
    public IMoneyHandler createMoneyHandlerForATM(Player player, Container container) { return createMoneyHandlerForContainer(container, s -> ItemHandlerHelper.giveItemToPlayer(player,s), IClientTracker.entityWrapper(player)); }

    /**
     * Whether the given item can be placed in a {@link io.github.lightman314.lightmanscurrency.api.misc.menus.MoneySlot MoneySlot} to be potentially used as payment
     */
    public boolean allowItemInMoneySlot(Player player, ItemStack item) { return false; }

    public void addMoneySlotBackground(Consumer<Pair<ResourceLocation,ResourceLocation>> consumer, Consumer<ResourceLocation> lazyConsumer) {}

    /**
     * Function to load a money value saved to NBT.
     * Should load the value saved by {@link MoneyValue#save()}
     */
    public abstract MoneyValue loadMoneyValue(CompoundTag valueTag);

    /**
     * Function to load a money value saved to JSON.
     * Should load the value saved by {@link MoneyValue#toJson()}
     */
    public abstract MoneyValue loadMoneyValueJson(JsonObject json);

    /**
     * Returns a {@link MoneyValueParser} for this money type, allowing it to be used in commands and config options
     */
    public abstract MoneyValueParser getValueParser();

    /**
     * Only in {@link Dist#CLIENT}<br>
     * Return a list of each {@link MoneyInputHandler} required for this mod!<br>
     * {@link MoneyInputHandler}s are used by {@link io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget MoneyValueWidget} to allow defining prices for your mods' currency<br>
     * See {@link io.github.lightman314.lightmanscurrency.api.money.input.templates.SimpleDisplayInput SimpleDisplayInput} for a simple text template, or {@link io.github.lightman314.lightmanscurrency.api.money.input.builtin.CoinValueInput CoinValueInput} to see how my mod handles this for coins<br>
     * Returns a list as it's possible for a single currency type to contain several variants (such as different coin chains, etc.)
     */
    @OnlyIn(Dist.CLIENT)
    @Deprecated(since = "2.3.0.4")
    public List<Object> getInputHandlers(@Nullable Player player) { return new ArrayList<>(); }

}