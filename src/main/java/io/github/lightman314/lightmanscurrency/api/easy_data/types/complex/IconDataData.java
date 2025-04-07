package io.github.lightman314.lightmanscurrency.api.easy_data.types.complex;

import io.github.lightman314.lightmanscurrency.api.easy_data.ComplexData;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataKey;
import io.github.lightman314.lightmanscurrency.api.easy_data.IEasyDataHost;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class IconDataData extends ComplexData<IconData> {

    private IconData value;
    public IconDataData(EasyDataKey key, IEasyDataHost host, IconData defaultValue) {
        super(key,host,IconData::save,IconData::load);
        this.value = defaultValue;
    }

    public static IconDataData of(EasyDataKey key, IEasyDataHost host) { return of(key,host,IconData.Null()); }
    public static IconDataData of(EasyDataKey key, IEasyDataHost host, IconData defaultValue) { return new IconDataData(key,host,defaultValue); }

    @Override
    public IconData get() { return this.value; }
    @Override
    protected void setInternal(IconData newValue) { this.value = Objects.requireNonNull(newValue); }

}
