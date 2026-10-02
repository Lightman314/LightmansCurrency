package io.github.lightman314.lightmanscurrency.datagen.client.models.multi;

import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record TallWideModelTemplate(ModelTemplate topLeft, ModelTemplate topRight,ModelTemplate bottomLeft,ModelTemplate bottomRight,ModelTemplate item) {

    public static TallWideModelTemplate create(Function<String,ModelTemplate> factory) {
        return new TallWideModelTemplate(factory.apply("_top_left"),factory.apply("_top_right"),
                factory.apply("_bottom_left"),factory.apply("_bottom_right"),
                factory.apply("_item"));
    }

    public TexturedModelProvider asTexturedModelProvider(Function<ModelTemplate,TexturedModel.Provider> factory) { return new TexturedModelProvider(factory.apply(this.topLeft),factory.apply(this.topRight),factory.apply(this.bottomLeft),factory.apply(this.bottomRight),factory.apply(this.item)); }

    public record TexturedModelProvider(TexturedModel.Provider topLeft,TexturedModel.Provider topRight,TexturedModel.Provider bottomLeft,TexturedModel.Provider bottomRight,TexturedModel.Provider item) {

        public Identifier createTopLeft(Block block, BiConsumer<Identifier, ModelInstance> modelOutput) { return this.topLeft.create(block,modelOutput); }
        public Identifier createTopRight(Block block, BiConsumer<Identifier, ModelInstance> modelOutput) { return this.topRight.create(block,modelOutput); }
        public Identifier createBottomLeft(Block block, BiConsumer<Identifier, ModelInstance> modelOutput) { return this.bottomLeft.create(block,modelOutput); }
        public Identifier createBottomRight(Block block, BiConsumer<Identifier, ModelInstance> modelOutput) { return this.bottomRight.create(block,modelOutput); }
        public Identifier createItem(Block block, BiConsumer<Identifier, ModelInstance> modelOutput) { return this.item.create(block,modelOutput); }

    }

}