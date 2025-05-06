package io.github.lightman314.lightmanscurrency.integration.jade;

import io.github.lightman314.lightmanscurrency.common.blocks.EasyBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.integration.jade.providers.*;
import snownee.jade.api.*;

@WailaPlugin
public class LCJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockIcon(VariantComponentProvider.INSTANCE,EasyBlock.class);
        registration.registerBlockComponent(VariantComponentProvider.INSTANCE,EasyBlock.class);
        registration.registerBlockComponent(PaygateComponentProvider.INSTANCE,PaygateBlock.class);
    }

}
