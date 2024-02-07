package io.github.lightman314.lightmanscurrency.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListUtil {

    public static <T> List<T> convertList(List<? extends T> list) {
        return new ArrayList<>(list);
    }

    public static List<Integer> createList(int[] array)
    {
        List<Integer> list = new ArrayList<>();
        for(int val : array)
            list.add(val);
        return list;
    }

}
