package io.github.lightman314.lightmanscurrency.api.helpers.capabilities;

import com.google.common.collect.ImmutableList;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public class CombinedEnergyHandler implements EnergyHandler
{
    private final List<EnergyHandler> handlers;
    public CombinedEnergyHandler(List<EnergyHandler> handlers) { this.handlers = ImmutableList.copyOf(handlers); }
    @Override
    public long getAmountAsLong() {
        long result = 0;
        for(EnergyHandler h : this.handlers)
            result += h.getAmountAsLong();
        return result;
    }
    @Override
    public long getCapacityAsLong() {
        long result = 0;
        for(EnergyHandler h : this.handlers)
            result += h.getCapacityAsLong();
        return result;
    }
    @Override
    public int insert(int amount, TransactionContext transaction) {
        int inserted = 0;
        for(EnergyHandler h : this.handlers)
        {
            int i = h.insert(amount,transaction);
            inserted += i;
            amount -= i;
        }
        return inserted;
    }
    @Override
    public int extract(int amount, TransactionContext transaction) {
        int extracted = 0;
        for(EnergyHandler h : this.handlers)
        {
            int e = h.extract(amount,transaction);
            amount -= e;
            extracted += e;
        }
        return extracted;
    }
}