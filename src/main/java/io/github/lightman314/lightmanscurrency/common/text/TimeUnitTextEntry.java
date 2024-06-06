package io.github.lightman314.lightmanscurrency.common.text;

import javax.annotation.Nonnull;

public class TimeUnitTextEntry {

    public final TextEntry fullText;
    public final TextEntry pluralText;
    public final TextEntry shortText;
    private TimeUnitTextEntry(@Nonnull String unit)
    {
        this.fullText = TextEntry.gui("lightmanscurrency","time.unit." + unit);
        this.pluralText = TextEntry.extend(this.fullText, "plural");
        this.shortText = TextEntry.extend(this.fullText, "short");
    }

    public static TimeUnitTextEntry of(@Nonnull String unit) { return new TimeUnitTextEntry(unit); }

}
