package io.github.lightman314.lightmanscurrency.api.taxes;

import java.util.Set;

public interface ITaxableContext {

    ITaxable taxable();

    boolean networkAccess();

    static ITaxableContext defaultContext(ITaxable taxable) { return simpleContext(taxable,false); }
    static ITaxableContext simpleContext(ITaxable taxable, boolean networkAccess) { return new SimpleContext(taxable,networkAccess); }

    static Set<ITaxableContext> defaultSet(ITaxable taxable) { return Set.of(defaultContext(taxable)); }
    static Set<ITaxableContext> fullSet(ITaxable taxable) { return Set.of(simpleContext(taxable,true), simpleContext(taxable,false)); }

    final class SimpleContext implements ITaxableContext
    {

        private final ITaxable taxable;
        private final boolean networkAccess;
        private SimpleContext(ITaxable taxable, boolean networkAccess)
        {
            this.taxable = taxable;
            this.networkAccess = networkAccess;
        }
        @Override
        public ITaxable taxable() { return this.taxable; }
        @Override
        public boolean networkAccess() { return this.networkAccess; }
    }

}