package io.github.lightman314.lightmanscurrency.api.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeData;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeUnit;

import java.util.List;
import java.util.function.Consumer;

public final class TimeInputWidget extends AbstractMultiWidget {

    private final List<TimeUnit> relevantUnits;
    private final int spacing;
    public final long minDuration;
    public final long maxDuration;
    private final Consumer<TimeData> handler;

    private long days = 0;
    private long hours = 0;
    private long minutes = 0;
    private long seconds = 0;

    public TimeData getTime() { return new TimeData(this.days,this.hours,this.minutes,this.seconds); }

    public TimeInputWidget(Builder builder) {
        super(builder);
        this.handler = builder.handler;
        this.relevantUnits = TimeUnit.rangeLargeToSmall(builder.largestUnit,builder.smallestUnit);
        this.spacing = builder.spacing;
        this.minDuration = Math.min(builder.minDuration,builder.maxDuration);
        this.maxDuration = Math.max(builder.minDuration,builder.maxDuration);
        this.setTimeInternal(builder.startTime);
        this.validateTime();
    }

    @Override
    protected void addEarlyChildren(ScreenArea area) {
        for(int i = 0; i < this.relevantUnits.size(); ++i) {
            final TimeUnit unit = this.relevantUnits.get(i);
            int xOff = (20 + this.spacing) * i;
            this.addChild(SpriteButton.builder()
                    .atPos(area.pos.offset(xOff,0))
                    .withSprite(LCSprites.BUTTON_BIG_ARROW_UP)
                    .onPress(() -> this.addTime(unit))
                    .matchWith(this)
                    .build());
            this.addChild(SpriteButton.builder()
                    .atPos(area.pos.offset(xOff,23))
                    .withSprite(LCSprites.BUTTON_BIG_ARROW_DOWN)
                    .onPress(() -> this.removeTime(unit))
                    .matchWith(this)
                    .build());
        }
    }

    @Override
    protected void addLateChildren(ScreenArea area) { }

    public void setTime(long milliseconds) {
        this.setTime(new TimeData(milliseconds));
    }

    public void setTime(TimeData time) {
        this.setTimeInternal(time);
        this.validateTime();
        this.handler.accept(this.getTime());
    }

    private void setTimeInternal(long milliseconds) { this.setTimeInternal(new TimeData(milliseconds)); }

    private void setTimeInternal(TimeData time) { this.setTimeInternal(time.days,time.hours,time.minutes,this.seconds); }

    private void setTimeInternal(long days,long hours,long minutes,long seconds) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;

        if(!this.validUnit(TimeUnit.DAY)) {
            this.hours += this.days * 24;
            this.days = 0;
        }
        if(!this.validUnit(TimeUnit.HOUR)) {
            this.minutes += this.hours * 60;
            this.hours = 0;
        }
        if(!this.validUnit(TimeUnit.MINUTE)) {
            this.seconds += this.minutes * 60;
            this.minutes = 0;
        }
        if(!this.validUnit(TimeUnit.SECOND))
            this.seconds = 0;
    }

    private boolean validUnit(TimeUnit unit) { return this.relevantUnits.contains(unit); }

    private void addTime(TimeUnit unit) { this.setTime(this.getTime().milliseconds + unit.getDurationMillis()); }

    private void removeTime(TimeUnit unit) {
        long millis = this.getTime().milliseconds;
        //Don't affect smaller units when attempting to remove higher units
        if(millis >= unit.getDurationMillis())
            this.setTime(millis - unit.getDurationMillis());
    }

    private void validateTime() {
        long duration = this.getTime().milliseconds;
        if(duration > this.maxDuration)
            this.setTimeInternal(this.maxDuration);
        if(duration < this.minDuration)
            this.setTimeInternal(this.minDuration);
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        for(int i = 0; i < this.relevantUnits.size(); ++i) {
            int centerX = ((20 + this.spacing) * i) + 10;
            gui.centeredText(this.getTime().getUnitString(this.relevantUnits.get(i),true),centerX,12,0xFFFFFFFF,false);
        }
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder,TimeInputWidget> {

        private Builder() { super(0,0); }

        private int spacing = 10;
        private TimeUnit smallestUnit = TimeUnit.SECOND;
        private TimeUnit largestUnit = TimeUnit.DAY;
        private Consumer<TimeData> handler = t -> {};
        private long minDuration = 0;
        private long maxDuration = TimeHelper.DURATION_YEAR;
        private long startTime = 0;

        public Builder withSpacing(int spacing) { this.spacing = spacing; return this; }

        public Builder smallestUnit(TimeUnit unit) { this.smallestUnit = unit; return this; }
        public Builder largestUnit(TimeUnit unit) { this.largestUnit = unit; return this; }
        public Builder withUnitRange(TimeUnit smallestUnit, TimeUnit largestUnit) { return this.smallestUnit(smallestUnit).largestUnit(largestUnit); }

        public Builder minDuration(long minDuration) { this.minDuration = minDuration; return this; }
        public Builder maxDuration(long maxDuration) { this.maxDuration = maxDuration; return this; }
        public Builder withRange(long minDuration, long maxDuration) { return this.minDuration(minDuration).maxDuration(maxDuration); }

        public Builder startTime(long startTime) { this.startTime = startTime; return this; }
        public Builder startTime(TimeData startTime) { this.startTime = startTime.milliseconds; return this; }

        public Builder withHandler(Runnable handler) { return this.withHandler(t -> handler.run()); }
        public Builder withHandler(Consumer<TimeData> handler) { this.handler = handler; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public TimeInputWidget build() { return new TimeInputWidget(this); }

    }

}
