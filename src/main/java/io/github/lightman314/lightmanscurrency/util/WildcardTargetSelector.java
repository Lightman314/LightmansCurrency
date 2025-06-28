package io.github.lightman314.lightmanscurrency.util;

import com.google.gson.JsonSyntaxException;

public record WildcardTargetSelector(String testString, TestType test)
{
    enum TestType { START(true,false), CONTAINS(true,true), END(false,true), EQUALS(false,false);
        final boolean start;
        final boolean end;
        TestType(boolean start,boolean end) { this.start = start; this.end = end; }
    }
    public boolean matches(String idString)
    {
        return switch (this.test) {
            case START -> idString.startsWith(this.testString);
            case CONTAINS -> idString.contains(this.testString);
            case END -> idString.endsWith(this.testString);
            case EQUALS -> idString.equals(this.testString);
        };
    }

    public static WildcardTargetSelector parse(String selector) throws JsonSyntaxException
    {
        boolean end = selector.startsWith("*");
        if(end)
            selector = selector.substring(1);
        boolean start = selector.endsWith("*");
        if(start)
            selector = selector.substring(0,selector.length() - 1);
        if(start)
        {
            if(end)
                return new WildcardTargetSelector(selector,TestType.CONTAINS);
            else
                return new WildcardTargetSelector(selector,TestType.START);
        }
        else
        {
            if(end)
                return new WildcardTargetSelector(selector,TestType.END);
            else
                return new WildcardTargetSelector(selector,TestType.EQUALS);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if(this.test.end)
            builder.append("*");
        builder.append(this.testString);
        if(this.test.start)
            builder.append("*");
        return builder.toString();
    }

}