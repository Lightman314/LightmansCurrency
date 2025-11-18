package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.tests;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.CustomModelTest;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.NestedModelTest;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AndTest extends NestedModelTest {

    public static final ResourceLocation TYPE = VersionUtil.lcResource("and");

    private final List<CustomModelTest> tests;
    public AndTest(CustomModelTest... tests) { this(ImmutableList.copyOf(tests)); }
    public AndTest(List<CustomModelTest> tests) { super(TYPE); this.tests = ImmutableList.copyOf(tests); this.checkForInfiniteLoops(); }

    @Override
    public boolean test(@Nullable  BlockEntity blockEntity, ItemStack item) {
        for(CustomModelTest test : this.tests)
        {
            if(!test.test(blockEntity,item))
                return false;
        }
        return true;
    }

    @Override
    protected void writeAdditional(JsonObject json) {
        JsonArray list = new JsonArray();
        for(CustomModelTest test : this.tests)
            list.add(test.write());
        json.add("children",list);
    }

    @Override
    protected List<CustomModelTest> children() { return this.tests; }

    public static AndTest parse(JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        JsonArray list = GsonHelper.getAsJsonArray(json,"children");
        List<CustomModelTest> tests = new ArrayList<>();
        for(int i = 0; i < list.size(); ++i)
        {
            JsonObject entry = GsonHelper.convertToJsonObject(list.get(i),"children[" + i + "]");
            tests.add(CustomModelTest.parse(entry));
        }
        return new AndTest(tests);
    }

}
