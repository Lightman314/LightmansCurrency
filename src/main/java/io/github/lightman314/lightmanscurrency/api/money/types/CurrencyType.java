package io.github.lightman314.lightmanscurrency.api.money.types;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * A MoneyType class for use with registering
 */
public abstract class CurrencyType {

    protected CurrencyType(@Nonnull ResourceLocation type) {
        this.type = type;
    }

    private final ResourceLocation type;

    public final ResourceLocation getType() {
        return this.type;
    }

    /**
     * A quick method to sum a list of money values for {@link MoneyView} purposes
     * Can be modified by the {@link CurrencyType} to make the math more efficient, as this often requires adding 20-30 values
     * together when querying the contents of a container.
     */
    public final MoneyValue sumValues(@Nonnull List<MoneyValue> values)
    {
        if(values.size() == 0)
            return MoneyValue.empty();
        if(values.size() == 1)
            return values.get(0);
        return this.sumValuesInternal(values);
    }

    /**
     * Method used by {@link #sumValues(List)} when the list size is greater than 1
     * Override this to optimize your addition calculations should your {@link MoneyValue} init
     * function do complicated math.
     */
    @Nonnull
    protected abstract MoneyValue sumValuesInternal(@Nonnull List<MoneyValue> values);

    public abstract boolean hasPlayersMoneyChanged(@Nonnull Player player);

    /**
     * Adds the amount of money available for this mods currency type to the {@link MoneyView.Builder}.
     * Mostly used by {@link io.github.lightman314.lightmanscurrency.api.money.value.holder.PlayerMoneyHolder} to safely cache the players stored money amounts to save processing time.
     */
    public abstract void getAvailableMoney(@Nonnull Player player, @Nonnull MoneyView.Builder builder);


    /**
     * Adds the given amount of money to the player.
     * Assumes that the player can always be given money.
     * @param player The player to give money to.
     * @param value The {@link MoneyValue} to give to the player.
     */
    public abstract void giveMoneyToPlayer(@Nonnull Player player, @Nonnull MoneyValue value);

    /**
     * Takes the given amount of money from the player.
     * Check {@link MoneyView#containsValue(MoneyValue)} on the result of {@link io.github.lightman314.lightmanscurrency.api.money.MoneyAPI#getPlayersAvailableFunds(Player)} before calling this to confirm that they have the funds available.
     * @param player The player to take the money from.
     * @param value The {@link MoneyValue} to take from the player.
     * @return Whether the money was successfully taken. Should only be <code>false</code> if {@link MoneyView#containsValue(MoneyValue)} on the results of {@link io.github.lightman314.lightmanscurrency.api.money.MoneyAPI#getPlayersAvailableFunds(Player)} is also false.
     */
    public abstract boolean takeMoneyFromPlayer(@Nonnull Player player, @Nonnull MoneyValue value);

    /**
     * Returns the salvagable value of any coin items within the {@link Container}.
     * Return an empty {@link ArrayList} if no value can be salvaged from these items.
     * {@link MoneyValue} entries returns should accurately describe if a value can be added or removed from the items currently present.
     */
    public final MoneyView valueInContainer(@Nonnull Container container) {
        MoneyView.Builder builder = MoneyView.builder();
        this.getValueInContainer(container, builder);
        return builder.build();
    }

    public void getValueInContainer(@Nonnull Container container, @Nonnull MoneyView.Builder builder) { }

    /**
     * Removes the given value from the container.
     * Should always check {@link #valueInContainer(Container)} and then {@link MoneyValue#containsValue(MoneyValue)} first before executing.
     * @return <code>false</code>> if the interaction failed. <code>true</code> if the requested amount was successfully taken.
     */
    public boolean takeValueFromContainer(@Nonnull Container container, @Nonnull MoneyValue amount, @Nonnull Consumer<ItemStack> overflowHandler) { return false; }

    /**
     * Whether the given value can be inserted into an item within the inventory.
     * Note: All actual addition of money will have a handler for overflow items
     * if more items are created than can be fit in the available space,
     * so space limitations could/should be ignored if that is the only concern.
     */
    public boolean canAddValueToContainer(@Nonnull Container container, @Nonnull MoneyValue value) { return false; }

    /**
     * Actually add the value to the inventory.
     * Should call {@link #canAddValueToContainer(Container, MoneyValue)} before calling this.
     */
    public boolean addValueToContainer(@Nonnull Container container, @Nonnull MoneyValue value, @Nonnull Consumer<ItemStack> overflowHandler) { return false; }

    /**
     * Function to load a money value saved to NBT.
     * Should load the value saved by {@link MoneyValue#save()}
     */
    public abstract MoneyValue loadMoneyValue(@Nonnull CompoundTag valueTag);

    /**
     * Function to load a money value saved to JSON.
     * Should load the value saved by {@link MoneyValue#toJson()}
     */
    public abstract MoneyValue loadMoneyValueJson(@Nonnull JsonObject json);

    @Nullable
    public abstract MoneyValueParser getValueParser();

    /**
     * Only in {@link Dist#CLIENT}
     * Return a list of each {@link MoneyInputHandler} required for this mod!
     * {@link MoneyInputHandler}s are used by {@link io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget} to allow defining prices for your mods' currency.
     * See {@link io.github.lightman314.lightmanscurrency.api.money.input.templates.SimpleDisplayInput} for a simple text template, or {@link io.github.lightman314.lightmanscurrency.api.money.input.builtin.CoinValueInput} to see how my mod handles this for coins.
     */
    @OnlyIn(Dist.CLIENT)
    public abstract List<Object> getInputHandlers(@Nullable Player player);

}
