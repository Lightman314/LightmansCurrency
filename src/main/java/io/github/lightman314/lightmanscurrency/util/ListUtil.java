package io.github.lightman314.lightmanscurrency.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListUtil {

    @SafeVarargs
    public static <T> List<T> newArrayList(T... values) { return new ArrayList<>(Arrays.stream(values).toList()); }

    public static <T> List<T> immutableCopyOf(List<T> list) { return Collections.unmodifiableList(list); }
    @SafeVarargs
    public static <T> List<T> immutableList(T... values) { return Collections.unmodifiableList(Arrays.asList(values)); }

}
