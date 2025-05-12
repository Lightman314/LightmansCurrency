package io.github.lightman314.lightmanscurrency.common.blocks.properties;

import com.google.common.collect.ImmutableList;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class YRotationProperty extends Property<Integer> {

    public static final Collection<Integer> POSSIBLE_VALUES = ImmutableList.of(0,90,180,270);
    protected YRotationProperty(String name) { super(name, Integer.class); }

    public static YRotationProperty of(String name) { return new YRotationProperty(name); }

    @Override
    public Collection<Integer> getPossibleValues() { return POSSIBLE_VALUES; }

    @Override
    public String getName(Integer value) { return value.toString(); }

    @Override
    public Optional<Integer> getValue(String value) {
        try {
            Integer integer = Integer.valueOf(value);
            if(POSSIBLE_VALUES.contains(integer))
                return Optional.of(integer);
            return Optional.empty();
        } catch (NumberFormatException numberformatexception) {
            return Optional.empty();
        }
    }

}