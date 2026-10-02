package io.github.lightman314.lightmanscurrency.datagen.client.models.multi;

import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record TallModelTemplate(ModelTemplate top,ModelTemplate bottom,ModelTemplate item) {

    public static TallModelTemplate create(Function<String,ModelTemplate> factory) { return new TallModelTemplate(factory.apply("_top"),factory.apply("_bottom"),factory.apply("_item")); }

    public TexturedModelProvider asTexturedModelProvider(Function<ModelTemplate,TexturedModel.Provider> factory) { return new TexturedModelProvider(factory.apply(this.top),factory.apply(this.bottom),factory.apply(this.item)); }

    public record TexturedModelProvider(TexturedModel.Provider top, TexturedModel.Provider bottom, TexturedModel.Provider item) {

        public Identifier createTop(Block block,BiConsumer<Identifier,ModelInstance> modelOutput) { return this.top.create(block,modelOutput); }
        public Identifier createBottom(Block block,BiConsumer<Identifier,ModelInstance> modelOutput) { return this.bottom.create(block,modelOutput); }
        public Identifier createItem(Block block,BiConsumer<Identifier,ModelInstance> modelOutput) { return this.item.create(block,modelOutput); }

    }

}