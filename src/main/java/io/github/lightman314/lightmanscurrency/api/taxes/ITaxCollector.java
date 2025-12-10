package io.github.lightman314.lightmanscurrency.api.taxes;

import io.github.lightman314.lightmanscurrency.api.misc.world.WorldArea;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface ITaxCollector extends IClientTracker {

    /**
     * The tax collectors id.
     */
    long getID();

    /**
     * Whether the given {@link ITaxCollector Tax Collector} is the admin server-wide tax collector.<br>
     * Should have no bearing on the actual functionality of the Tax Collector, this is mostly used to change the tax collector notifications to specify that it's a server-wide tax.
     */
    boolean isServerEntry();

    /**
     * The effective area of the Tax Collector.
     */
    WorldArea getArea();

    /**
     * The percentage of money that will be taxed by this tax collector.
     */
    int getTaxRate();

    /**
     * The tax collectors name.
     */
    MutableComponent getName();

    /**
     * The owner of this tax collector.
     */
    OwnerData getOwner();

    /**
     * Whether the given player is allowed to access this tax collectors menus.
     */
    boolean canAccess(Player player);

    /**
     * Whether this tax collector will tax the given {@link ITaxable Taxable} machine.
     */
    default boolean ShouldTax(ITaxable taxable) { return this.ShouldTax(ITaxableContext.defaultContext(taxable)); }

    /**
     * Whether this tax collector will tax the given {@link ITaxable Taxable} machine.
     */
    boolean ShouldTax(ITaxableContext context);

    /**
     * Whether the given {@link ITaxable} machine is within this tax collectors effective area.
     */
    boolean IsInArea(ITaxable taxable);

    /**
     * Flags the given {@link ITaxable Taxable} Machine as giving consent to being taxed by this tax collector.<br>
     * Required for {@link #ShouldTax(ITaxableContext)} to accept the given machine.
     */
    void AcceptTaxable(ITaxable taxable);

    /**
     * Flags the given {@link ITaxable Taxable} Machine as removed from the world.<br>
     * Can also be used to remove the consent to be taxed by this tax collector, but that's not how tax collectors are designed to work.
     */
    void TaxableWasRemoved(ITaxable taxable);

    /**
     * Calculates the taxes to be paid on the given monetary amount, and collects it.<br>
     * Will automatically post any relevant notifications to the local logger, but will not do so for the given {@link ITaxable Taxable} machine.<br>
     * Will also automatically note the interaction into the tax collectors statistics.
     * @param taxable The taxable machine that is paying the taxes.
     * @param taxableAmount The amount of money to be taxed.
     * @return The amount of taxes that were collected by the tax collector.
     */
    
    MoneyValue CalculateAndPayTaxes(ITaxable taxable, MoneyValue taxableAmount);

    /**
     * Pays a pre-calculated amount of taxes to this tax collector<br>
     * Stats and notification will only be sent if the Taxable is null,
     * otherwise all this does is directly place the money into the tax collectors money storage.
     * @param taxable The taxable machine that is paying the taxes. May be <code>null</code>
     * @param taxes The amount of taxes to be paid to the tax collector.
     */
    void PayTaxesDirectly(@Nullable ITaxable taxable, MoneyValue taxes);

}