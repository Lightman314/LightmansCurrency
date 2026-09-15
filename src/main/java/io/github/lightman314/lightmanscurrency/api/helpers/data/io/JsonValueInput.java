package io.github.lightman314.lightmanscurrency.api.helpers.data.io;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.*;
import com.mojang.serialization.DataResult.Success;
import com.mojang.serialization.DataResult.Error;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInputContextHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class JsonValueInput implements ValueInput {

    private final ProblemReporter problemReporter;
    private final HolderLookup.Provider holders;
    private final DynamicOps<JsonElement> ops;
    private final ValueInputContextHelper helper;
    private final JsonObject input;

    private JsonValueInput(ProblemReporter problemReporter,HolderLookup.Provider holders,JsonObject input) {
        this.problemReporter = problemReporter;
        this.holders = holders;
        this.ops = this.holders.createSerializationContext(JsonOps.INSTANCE);
        this.helper = new ValueInputContextHelper(this.holders,NbtOps.INSTANCE);
        this.input = input;
    }

    public static ValueInput create(ProblemReporter problemReporter,HolderLookup.Provider holders,JsonObject json) {
        return new JsonValueInput(problemReporter,holders,json);
    }

    public static ValueInputList create(ProblemReporter problemReporter,HolderLookup.Provider holders,List<JsonObject> list) {
        return new JsonListWrapper(problemReporter,holders,list);
    }

    @Override
    public <T> Optional<T> read(String name, Codec<T> codec) {
        JsonElement element = this.input.get(name);
        if(element == null)
            return Optional.empty();
        else {
            return switch (codec.parse(this.ops,element)) {
                case Success<T> success -> Optional.of(success.value());
                case Error<T> error -> {
                    this.problemReporter.report(new DecodeFromFieldFailedProblem(name,element,error));
                    yield error.partialValue();
                }
            };
        }
    }

    @Override
    public <T> Optional<T> read(MapCodec<T> codec) {
        return switch (ops.getMap(this.input).flatMap(map -> codec.decode(ops,map))) {
            case Success<T> success ->  Optional.of(success.value());
            case Error<T> error -> {
                this.problemReporter.report(new DecodeFromMapFailedProblem(error));
                yield error.partialValue();
            }
        };
    }

    @Nullable
    private JsonObject getOptionalChild(String name) {
        if(this.input.has(name)) {
            JsonElement element = this.input.get(name);
            if(element.isJsonObject())
                return element.getAsJsonObject();
        }
        return null;
    }

    @Override
    public Optional<ValueInput> child(String name) {
        JsonObject child = this.getOptionalChild(name);
        return child != null ? Optional.of(this.wrapChild(name,child)) : Optional.empty();
    }

    @Override
    public ValueInput childOrEmpty(String name) {
        JsonObject child = this.getOptionalChild(name);
        return child != null ? this.wrapChild(name,child) : this.helper.empty();
    }

    @Nullable
    private JsonArray getOptionalList(String name) {
        if(this.input.has(name)) {
            JsonElement element = this.input.get(name);
            if(element.isJsonArray())
                return element.getAsJsonArray();
        }
        return null;
    }

    @Override
    public Optional<ValueInputList> childrenList(String name) {
        JsonArray list = this.getOptionalList(name);
        return list != null ? Optional.of(this.wrapList(name,list)) : Optional.empty();
    }

    @Override
    public ValueInputList childrenListOrEmpty(String name) {
        JsonArray list = this.getOptionalList(name);
        return list != null ? this.wrapList(name,list) : this.helper.emptyList();
    }

    @Override
    public <T> Optional<TypedInputList<T>> list(String name, Codec<T> codec) {
        JsonArray list = this.getOptionalList(name);
        return list != null ? Optional.of(this.wrapTypedList(name,list,codec)) : Optional.empty();
    }

    @Override
    public <T> TypedInputList<T> listOrEmpty(String name, Codec<T> codec) {
        JsonArray list = this.getOptionalList(name);
        return list != null ? this.wrapTypedList(name,list,codec) : this.helper.emptyTypedList();
    }

    @Nullable
    private JsonPrimitive getOptionalPrimitive(String name) {
        if(this.input.has(name)) {
            JsonElement element = this.input.get(name);
            if(element.isJsonPrimitive())
                return element.getAsJsonPrimitive();
        }
        return null;
    }

    @Nullable
    private Number getOptionalNumber(String name) {
        JsonPrimitive primitive = this.getOptionalPrimitive(name);
        if(primitive != null && primitive.isNumber())
            return primitive.getAsNumber();
        return null;
    }

    @Override
    public boolean getBooleanOr(String name, boolean defaultValue) {
        JsonPrimitive primitive = this.getOptionalPrimitive(name);
        if(primitive == null)
            return defaultValue;
        if(primitive.isBoolean())
            return primitive.getAsBoolean();
        if(primitive.isNumber())
            return primitive.getAsInt() != 0;
        return defaultValue;
    }

    @Override
    public byte getByteOr(String name, byte defaultValue) {
        Number number = this.getOptionalNumber(name);
        if(number != null)
            return number.byteValue();
        return defaultValue;
    }

    @Override
    public int getShortOr(String name, short defaultValue) {
        Number number = this.getOptionalNumber(name);
        if(number != null)
            return number.shortValue();
        return defaultValue;
    }

    @Override
    public Optional<Integer> getInt(String name) {
        Number number = this.getOptionalNumber(name);
        if(number != null)
            return Optional.of(number.intValue());
        return Optional.empty();
    }

    @Override
    public int getIntOr(String name, int defaultValue) {
        return this.getInt(name).orElse(defaultValue);
    }

    @Override
    public Optional<Long> getLong(String name) {
        Number number = this.getOptionalNumber(name);
        if(number != null)
            return Optional.of(number.longValue());
        return Optional.empty();
    }

    @Override
    public long getLongOr(String name, long defaultValue) {
        return this.getLong(name).orElse(defaultValue);
    }

    @Override
    public float getFloatOr(String name, float defaultValue) {
        Number number = this.getOptionalNumber(name);
        if(number != null)
            return number.floatValue();
        return defaultValue;
    }

    @Override
    public double getDoubleOr(String name, double defaultValue) {
        Number number = this.getOptionalNumber(name);
        if(number != null)
            return number.doubleValue();
        return defaultValue;
    }

    @Override
    public Optional<String> getString(String name) {
        JsonPrimitive primitive = this.getOptionalPrimitive(name);
        if(primitive != null && primitive.isString())
            return Optional.of(primitive.getAsString());
        return Optional.empty();
    }

    @Override
    public String getStringOr(String name, String defaultValue) {
        return this.getString(name).orElse(defaultValue);
    }

    @Override
    public Optional<int[]> getIntArray(String name) {
        JsonArray list = this.getOptionalList(name);
        List<Integer> result = new ArrayList<>();
        for(JsonElement element : list) {
            if(element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                if(primitive.isNumber())
                    result.add(primitive.getAsInt());
                else
                    return Optional.empty();
            }
            else return Optional.empty();
        }
        int[] array = new int[result.size()];
        for(int i = 0; i < list.size(); ++i)
            array[i] = result.get(i);
        return Optional.of(array);
    }

    @Override
    public HolderLookup.Provider lookup() { return this.holders; }

    private ValueInput wrapChild(String name,JsonObject child) {
        return child.isEmpty() ? this.helper.empty() : create(this.problemReporter.forChild(new ProblemReporter.FieldPathElement(name)),this.holders,child);
    }

    private ValueInputList wrapList(String name,JsonArray list) {
        return list.isEmpty() ? this.helper.emptyList() : new ListWrapper(this.problemReporter,name,this.holders,list);
    }

    private <T> TypedInputList<T> wrapTypedList(String name,JsonArray list,Codec<T> codec) {
        return list.isEmpty() ? this.helper.emptyTypedList() : new TypedListWrapper<>(this.problemReporter,name,this.holders,codec,list);
    }

    public record DecodeFromFieldFailedProblem(String name,JsonElement json,Error<?> error) implements ProblemReporter.Problem {
        @Override
        public String description() {
            return "Failed to decode value '" + this.json + "' from field '" + this.name + "': " + this.error.message();
        }
    }

    public record DecodeFromListFailedProblem(String name,int index,JsonElement json,Error<?> error) implements ProblemReporter.Problem {
        @Override
        public String description() {
            return "Failed to decode value '" + this.json + "' from field '" + this.name + "' at index " + this.index + ": " + this.error.message();
        }
    }

    public record DecodeFromMapFailedProblem(Error<?> error) implements ProblemReporter.Problem {
        @Override
        public String description() {
            return "Failed to decode from map: " + this.error.message();
        }
    }

    private static class JsonListWrapper implements ValueInputList {

        private final ProblemReporter problemReporter;
        private final HolderLookup.Provider holders;
        private final List<JsonObject> list;
        private JsonListWrapper(ProblemReporter problemReporter, HolderLookup.Provider holders, List<JsonObject> list) {
            this.problemReporter = problemReporter;
            this.holders = holders;
            this.list = list;
        }

        private ValueInput wrapChild(int index,JsonObject element) {
            return JsonValueInput.create(this.problemReporter.forChild(new ProblemReporter.IndexedPathElement(index)),this.holders,element);
        }

        @Override
        public boolean isEmpty() { return this.list.isEmpty(); }

        @Override
        public Stream<ValueInput> stream() {
            return Streams.mapWithIndex(this.list.stream(),(value,index) -> this.wrapChild((int)index,value));
        }

        @Override
        public Iterator<ValueInput> iterator() {
            final ListIterator<JsonObject> iterator = this.list.listIterator();
            return new AbstractIterator<>() {
                @Override
                @Nullable
                protected ValueInput computeNext() {
                    if(iterator.hasNext()) {
                        int index = iterator.nextIndex();
                        JsonObject value = iterator.next();
                        return JsonListWrapper.this.wrapChild(index,value);
                    }
                    else
                        return this.endOfData();
                }
            };
        }
    }

    private static class ListWrapper implements ValueInput.ValueInputList {
        private final ProblemReporter problemReporter;
        private final String name;
        private final HolderLookup.Provider holders;
        private final JsonArray list;
        private ListWrapper(ProblemReporter problemReporter,String name,HolderLookup.Provider holders,JsonArray list) {
            this.problemReporter = problemReporter;
            this.name = name;
            this.holders = holders;
            this.list = list;
        }

        @Override
        public boolean isEmpty() { return this.list.isEmpty(); }

        private ProblemReporter reporterForChild(int index) {
            return this.problemReporter.forChild(new ProblemReporter.IndexedFieldPathElement(this.name,index));
        }

        private void reportIndexUnwrapProblem(int index,JsonElement value) {
            this.problemReporter.report(new UnexpectedListElementTypeProblem(this.name,index,JsonObject.class,value.getClass()));
        }

        @Override
        public Stream<ValueInput> stream() {
            return Streams.mapWithIndex(this.list.asList().stream(),(value,index) -> {
                if(value.isJsonObject()) {
                    return create(this.reporterForChild((int)index),this.holders,value.getAsJsonObject());
                } else {
                    this.reportIndexUnwrapProblem((int)index,value);
                    return null;
                }
            });
        }

        @Override
        public Iterator<ValueInput> iterator() {
            final Iterator<JsonElement> iterator = this.list.iterator();
            return new AbstractIterator<>() {
                private int index;
                @Override
                @Nullable
                protected ValueInput computeNext() {
                    while(iterator.hasNext()) {
                        JsonElement value = iterator.next();
                        int currentIndex = this.index++;
                        if(value.isJsonObject()) {
                            return create(ListWrapper.this.reporterForChild(currentIndex),ListWrapper.this.holders,value.getAsJsonObject());
                        }
                        ListWrapper.this.reportIndexUnwrapProblem(currentIndex,value);
                    }
                    return endOfData();
                }
            };
        }
    }

    private static class TypedListWrapper<T> implements ValueInput.TypedInputList<T> {

        private final ProblemReporter problemReporter;
        private final String name;
        private final DynamicOps<JsonElement> ops;
        private final Codec<T> codec;
        private final JsonArray list;
        private TypedListWrapper(ProblemReporter problemReporter,String name,HolderLookup.Provider holders,Codec<T> codec,JsonArray list) {
            this.problemReporter = problemReporter;
            this.name = name;
            this.ops = holders.createSerializationContext(JsonOps.INSTANCE);
            this.codec = codec;
            this.list = list;
        }

        @Override
        public boolean isEmpty() { return this.list.isEmpty(); }

        private void reportIndexUnwrapProblem(int index,JsonElement value,Error<?> error) {
            this.problemReporter.report(new DecodeFromListFailedProblem(this.name,index,value,error));
        }

        @Override
        public Stream<T> stream() {
            return Streams.mapWithIndex(this.list.asList().stream(),(value,index) ->
                    switch (this.codec.parse(this.ops,value)) {
                        case Success<T> success -> success.value();
                        case Error<T> error -> {
                            this.reportIndexUnwrapProblem((int)index,value,error);
                            yield error.partialValue().orElse(null);
                        }
            });
        }

        @Override
        public Iterator<T> iterator() {
            final ListIterator<JsonElement> iterator = this.list.asList().listIterator();
            return new AbstractIterator<>() {
                @Override
                @Nullable
                protected T computeNext() {
                    while(iterator.hasNext()) {
                        int index = iterator.nextIndex();
                        JsonElement value = iterator.next();
                        switch (TypedListWrapper.this.codec.parse(TypedListWrapper.this.ops,value)) {
                            case Success<T> success:
                                return success.value();
                            case Error<T> error:
                                TypedListWrapper.this.reportIndexUnwrapProblem(index,value,error);
                                if(error.partialValue().isEmpty())
                                    break;
                                return error.partialValue().get();
                            default: throw new MatchException(null,null);
                        }
                    }
                    return this.endOfData();
                }
            };
        }
    }

    public record UnexpectedListElementTypeProblem(String name,int index,Class<? extends JsonElement> expected,Class<? extends JsonElement> actual) implements ProblemReporter.Problem {
        @Override
        public String description() {
            return "Expected list '" + this.name + "' to contain at index " + this.index + " value of type " + this.expected.getSimpleName() + ", but got " + this.actual.getSimpleName();
        }
    }

}
