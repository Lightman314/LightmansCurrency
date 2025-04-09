package io.github.lightman314.lightmanscurrency.api.easy_data.complex.types;

import io.github.lightman314.lightmanscurrency.api.easy_data.complex.ComplexData;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataSettings;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class IconDataData extends ComplexData<IconData> {

    private IconData value;
    public IconDataData(EasyDataSettings<IconData> settings, IconData defaultValue) {
        super(settings,IconData::save,IconData::load);
        this.value = defaultValue;
    }

    public static EasyDataSettings.Builder<IconData,IconDataData> builder() { return builder(IconData.Null()); }
    public static EasyDataSettings.Builder<IconData,IconDataData> builder(IconData defaultValue) { return EasyDataSettings.builder(b -> new IconDataData(b,defaultValue)); }

    @Override
    public IconData get() { return this.value; }
    @Override
    protected void setInternal(IconData newValue) { this.value = Objects.requireNonNull(newValue); }

}
