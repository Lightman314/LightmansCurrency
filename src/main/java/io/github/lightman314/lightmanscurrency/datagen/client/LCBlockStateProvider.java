package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

//May or may not actually generate client data.
//May at least automate wooden block states/models to make it easier for cross-version model handling
public class LCBlockStateProvider extends BlockStateProvider {

    public LCBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, LightmansCurrency.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }

}