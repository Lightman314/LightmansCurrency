package io.github.lightman314.lightmanscurrency.api.helpers.time;

public final class TimeData {

    public final long days;
    public final long hours;
    public final long minutes;
    public final long seconds;
    public final long milliseconds;
    public TimeData(long days,long hours,long minutes,long seconds) { this(TimeHelper.getDuration(days,hours,minutes,seconds)); }
    public TimeData(long milliseconds) {
        this.milliseconds = Math.max(milliseconds,0);

        long seconds = this.milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        long hours = minutes / 60;
        minutes = minutes % 60;
        long days = hours / 24;
        hours = hours % 24;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    private long getUnitValue(TimeUnit unit) {
        return switch (unit) {
            case DAY -> this.days;
            case HOUR -> this.hours;
            case MINUTE -> this.minutes;
            case SECOND -> this.seconds;
        };
    }

    public String getUnitString(TimeUnit unit,boolean shortText) { return this.getUnitString(unit,shortText,true); }

    private String getUnitString(TimeUnit unit,boolean shortText,boolean force) {
        StringBuilder text = new StringBuilder();
        long count = this.getUnitValue(unit);
        if(count > 0 || force)
            text.append(count).append(shortText ? unit.getShortText().getString() : (count != 1 ? unit.getPluralText().getString() : unit.getText().getString()));
        return text.toString();
    }

    public String getString() { return this.getString(Integer.MAX_VALUE); }
    public String getString(int maxCount) { return this.getString(false,maxCount); }
    public String getShortString() { return this.getShortString(Integer.MAX_VALUE); }
    public String getShortString(int maxCount) { return this.getString(true,maxCount); }
    private String getString(boolean shortText,int maxCount) {
        StringBuilder text = new StringBuilder();
        int count = 0;
        for(TimeUnit unit : TimeUnit.UNITS_LARGE_TO_SMALL) {
            String unitText = this.getUnitString(unit,shortText,false);
            if(!unitText.isEmpty()) {
                if(!text.isEmpty())
                    text.append(" ");
                text.append(unitText);
                count++;
            }
        }
        //Force it to be 0 seconds if the text would otherwise be empty
        if(text.isEmpty())
            return getUnitString(TimeUnit.SECOND,shortText,true);
        return text.toString();
    }

}
