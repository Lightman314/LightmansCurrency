package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PendingSearch {

    public static final PendingSearch EMPTY = new PendingSearch("",new HashMap<>(),new ArrayList<>());

    public final String fullSearch;
    public boolean isBlank() { return this.fullSearch.isBlank(); }
    private final Map<String,List<String>> filteredInputs;
    private final Map<String,Result<List<String>>> filteredResults = new HashMap<>();
    private final List<String> unfilteredInputs;
    private final List<Result<String>> unfilteredResults = new ArrayList<>();

    private PendingSearch(String fullSearch,Map<String,List<String>> filteredInputs,List<String> unfilteredInputs)
    {
        this.fullSearch = fullSearch;
        this.filteredInputs = filteredInputs;
        this.unfilteredInputs = unfilteredInputs;
        filteredInputs.forEach((filter,list) -> this.filteredResults.put(filter,new Result<>(list)));
        unfilteredInputs.forEach(s -> this.unfilteredResults.add(new Result<>(s)));
    }

    public PendingSearch copy() { return new PendingSearch(this.fullSearch,this.filteredInputs,this.unfilteredInputs); }

    public static PendingSearch of(String searchText) {
        if(searchText.isBlank())
            return EMPTY;
        searchText = searchText.toLowerCase();
        Map<String,List<String>> filteredInputs = new HashMap<>();
        List<String> unfilteredInputs = new ArrayList<>();
        for(String input : splitRawString(searchText))
        {
            if(input.contains(":"))
            {
                String[] split = input.split(":",2);
                List<String> list = filteredInputs.getOrDefault(split[0],new ArrayList<>());
                list.add(split[1]);
                filteredInputs.put(split[0],list);
            }
            else
                unfilteredInputs.add(input);
        }
        return new PendingSearch(searchText,filteredInputs,unfilteredInputs);
    }

    private static List<String> splitRawString(String rawString)
    {
        List<String> list = new ArrayList<>();
        StringBuilder temp = new StringBuilder();
        boolean inQuote = false;
        for(int i = 0; i < rawString.length(); ++i)
        {
            char c = rawString.charAt(i);
            //Cancel or being next quote
            if(c == '"')
            {
                inQuote = !inQuote;
                continue;
            }
            if(inQuote && c == '\\')
            {
                temp.append(rawString.charAt(++i));
                continue;
            }
            if(c == ' ' && !inQuote)
            {
                list.add(temp.toString());
                temp = new StringBuilder();
            }
            else
                temp.append(c);
        }
        if(!temp.isEmpty())
            list.add(temp.toString());
        return list;
    }

    /**
     * Checks the given filter inputs with the given processor
     * Will also check unfiltered inputs for more flexible searching for common inputs (like name,items, etc.)
     */
    public void processFilter(String filter, Predicate<String> processor)
    {
        this.processStrictFilter(filter,processor);
        //Always process unfiltered with the same filter just in case they forgot to add the filter tag
        this.processUnfiltered(processor);
    }

    public void processStrictFilter(String filter, Predicate<String> processor)
    {
        if(this.filteredResults.containsKey(filter))
        {
            Result<List<String>> result = this.filteredResults.get(filter);
            if(result.passed)
                return;
            boolean pass = true;
            for(String string : result.value)
            {
                if(string.startsWith("!"))
                    pass = pass && !processor.test(string.substring(1));
                else
                    pass = pass && processor.test(string);
            }
            //Don't override the old result if another search handler utilizes the same filter
            result.setPassed(pass);
        }
    }

    public void processUnfiltered(Predicate<String> processor)
    {
        for(Result<String> result : this.unfilteredResults)
        {
            if(result.value.startsWith("!"))
                result.setPassed(!processor.test(result.value.substring(1)));
            else
                result.setPassed(processor.test(result.value));
        }
    }

    public boolean hasPassed() { return this.filteredResults.values().stream().allMatch(v -> v.passed) && this.unfilteredResults.stream().allMatch(v -> v.passed); }

    private static class Result<T>
    {
        final T value;
        boolean passed = false;
        Result(T value) { this.value = value; }
        void setPassed(boolean passed) { this.passed = this.passed || passed; }
    }

}
