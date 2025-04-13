package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class NestedModelTest extends CustomModelTest {

    public NestedModelTest(ResourceLocation type) { super(type); }

    public final void checkForInfiniteLoops() throws IllegalArgumentException { checkForInfiniteLoops(this,this.children()); }

    private static void checkForInfiniteLoops(NestedModelTest parent, List<CustomModelTest> children) throws IllegalArgumentException
    {
        for(CustomModelTest child : children)
        {
            if(child == parent)
                throw new IllegalArgumentException("Illegal Nested Test found!");
            if(child instanceof NestedModelTest nmt)
            {
                //Check if we are in the other nested tests children
                checkForInfiniteLoops(parent,nmt.children());
                //Check if the other nested test is within our own children
                checkForInfiniteLoops(nmt,parent.children());
            }
        }
    }

    protected abstract List<CustomModelTest> children();

}
