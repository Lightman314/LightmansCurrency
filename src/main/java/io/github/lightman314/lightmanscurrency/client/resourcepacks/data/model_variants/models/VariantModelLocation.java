package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.models;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantModelLocation {

    public final ResourceLocation variant;
    public final ResourceLocation target;
    public final int index;
    public final int yRot;
    private VariantModelLocation(ResourceLocation variant, ResourceLocation target, int index, int yRot)
    {
        this.variant = Objects.requireNonNull(variant);
        this.target = Objects.requireNonNull(target);
        this.index = index;
        this.yRot = yRot;
    }

    public static VariantModelLocation item(ResourceLocation variant, ResourceLocation target) { return new VariantModelLocation(variant,target,-1,0); }
    public static VariantModelLocation basic(ResourceLocation variant, ResourceLocation target, int index) { return new VariantModelLocation(variant,target,index,0); }
    public static VariantModelLocation rotatable(ResourceLocation variant, ResourceLocation target, int index, int yRotation) { return new VariantModelLocation(variant,target,index,yRotation); }

    @Override
    public int hashCode() { return Objects.hash(this.variant,this.target,this.index,this.yRot); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof VariantModelLocation other)
            return this.variant.equals(other.variant) && this.target.equals(other.target) && this.index == other.index && this.yRot == other.yRot;
        return false;
    }

    @Override
    public String toString() {
        return "VariantModelLocation[" +
                "Variant:" + this.variant +
                ",Target:" + this.target +
                ",Index:" + this.index +
                ",yRotation:" + this.yRot +
                "]";
    }

}