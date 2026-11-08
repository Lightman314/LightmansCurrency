package io.github.lightman314.lightmanscurrency.datagen.common;

import io.github.lightman314.lightmanscurrency.api.LCDataMapTypes;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public class LCDataMapProvider extends DataMapProvider {

    public LCDataMapProvider(PackOutput packOutput,CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput,lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        builder(LCDataMapTypes.MASTER_TICKET_SET)
                .add(LCItems.MASTER_TICKET,LCItems.TICKET.get(),false);

        builder(LCDataMapTypes.TICKET_STUB)
                .add(LCItems.TICKET,LCItems.TICKET_STUB.get(),false)
                .add(LCItems.COUPON,LCItems.TICKET_STUB.get(),false);

    }

}