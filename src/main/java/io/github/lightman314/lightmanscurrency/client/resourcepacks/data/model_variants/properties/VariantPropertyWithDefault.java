package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties;

public abstract class VariantPropertyWithDefault<T> extends VariantProperty<T> {

    public abstract T getMissingDefault();
    public abstract T getBuilderDefault();

}
