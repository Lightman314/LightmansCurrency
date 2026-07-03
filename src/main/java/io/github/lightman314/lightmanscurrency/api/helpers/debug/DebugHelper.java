package io.github.lightman314.lightmanscurrency.api.helpers.debug;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class DebugHelper {
    private DebugHelper() {}

    public static String sideName(Entity context) { return sideName(ISidedContext.wrap(context)); }
    public static String sideName(Level context) { return sideName(ISidedContext.known(context.isClientSide())); }
    public static String sideName(ISidedContext context) { return context.isClient() ? "client" : " server"; }

    public static String debugList(List<?> list) { return debugList(list,Object::toString); }
    public static String debugList(List<?> list,String seperator) { return debugList(list,seperator,Object::toString); }
    public static <T> String debugList(List<? extends T> list, Function<T,String> toString) { return debugList(list,", ",toString); }
    public static <T> String debugList(List<? extends T> list,String seperator,Function<T,String> toString) {
        StringBuilder builder = new StringBuilder();
        for(T entry : list)
        {
            if(!builder.isEmpty())
                builder.append(seperator);
            builder.append(toString.apply(entry));
        }
        return builder.toString();
    }

    public static String simpleClassName(Object object) { return object.getClass().getSimpleName(); }
    public static String fullClassName(Object object) { return object.getClass().getCanonicalName(); }

    public static String debugMap(Map<?,?> map) { return debugMap(map,Object::toString,Object::toString); }
    public static String debugMap(Map<?,?> map,String seperator,String label) { return debugMap(map,seperator,label,Object::toString,Object::toString); }
    public static <A,B> String debugMap(Map<? extends A,? extends B> map,Function<A,String> toStringA,Function<B,String> toStringB) { return debugMap(map,"\n",": ",Object::toString,Object::toString); }
    public static <A,B> String debugMap(Map<? extends A,? extends B> map,String seperator,String label,Function<A,String> toStringA,Function<B,String> toStringB) {
        StringBuilder builder = new StringBuilder();
        for(var entry : map.entrySet())
        {
            if(!builder.isEmpty())
                builder.append(seperator);
            builder.append(toStringA.apply(entry.getKey())).append(label).append(toStringB.apply(entry.getValue()));
        }
        return builder.toString();
    }



}