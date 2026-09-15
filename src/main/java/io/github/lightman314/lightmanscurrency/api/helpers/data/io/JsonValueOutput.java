package io.github.lightman314.lightmanscurrency.api.helpers.data.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.*;
import com.mojang.serialization.DataResult.Success;
import com.mojang.serialization.DataResult.Error;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class JsonValueOutput implements ValueOutput {

    private final ProblemReporter problemReporter;
    private final DynamicOps<JsonElement> ops;
    private final JsonObject output;
    private JsonValueOutput(ProblemReporter problemReporter,DynamicOps<JsonElement> ops,JsonObject output) {
        this.problemReporter = problemReporter;
        this.ops = ops;
        this.output = output;
    }

    public static JsonValueOutput createWithContext(ProblemReporter problemReporter, HolderLookup.Provider provider) {
        return new JsonValueOutput(problemReporter,provider.createSerializationContext(JsonOps.INSTANCE),new JsonObject());
    }

    public static JsonValueOutput createWithoutContext(ProblemReporter problemReporter) {
        return new JsonValueOutput(problemReporter,JsonOps.INSTANCE,new JsonObject());
    }

    @Override
    public <T> void store(String name, Codec<T> codec, T value) {
        switch (codec.encodeStart(this.ops,value)) {
            case Success<JsonElement> success:
                this.output.add(name,success.value());
                break;
            case Error<JsonElement> error:
                this.problemReporter.report(new TagValueOutput.EncodeToFieldFailedProblem(name,value,error));
                break;
        }
    }

    @Override
    public <T> void storeNullable(String name, Codec<T> codec, @Nullable T value) {
        if(value != null)
            this.store(name,codec,value);
    }

    @Override
    public <T> void store(MapCodec<T> codec, T value) {
        switch (codec.encoder().encodeStart(this.ops,value)) {
            case Success<JsonElement> success:
                this.merge((JsonObject)success.value());
                break;
            case Error<JsonElement> error:
                this.problemReporter.report(new TagValueOutput.EncodeToMapFailedProblem(value,error));
                error.partialValue().ifPresent(v -> this.merge((JsonObject)v));
                break;
        }
    }

    private void merge(JsonObject json) {
        for(String key : json.keySet())
            this.output.add(key,json.get(key));
    }

    @Override
    public void putBoolean(String name,boolean value) { this.output.addProperty(name,value); }
    @Override
    public void putByte(String name,byte value) { this.output.addProperty(name,value); }
    @Override
    public void putShort(String name,short value) { this.output.addProperty(name,value); }
    @Override
    public void putInt(String name, int value) { this.output.addProperty(name,value); }
    @Override
    public void putLong(String name, long value) { this.output.addProperty(name,value); }
    @Override
    public void putFloat(String name, float value) { this.output.addProperty(name,value); }
    @Override
    public void putDouble(String name, double value) { this.output.addProperty(name,value); }
    @Override
    public void putString(String name, String value) { this.output.addProperty(name,value); }
    @Override
    public void putIntArray(String name, int[] value) {
        JsonArray array = new JsonArray();
        for(int v : value)
            array.add(v);
        this.output.add(name,array);
    }

    private ProblemReporter reporterForChild(String name) {
        return this.problemReporter.forChild(new ProblemReporter.FieldPathElement(name));
    }

    @Override
    public ValueOutput child(String name) {
        JsonObject child = new JsonObject();
        this.output.add(name,child);
        return new JsonValueOutput(this.reporterForChild(name),this.ops,child);
    }

    @Override
    public ValueOutputList childrenList(String name) {
        JsonArray childList = new JsonArray();
        this.output.add(name,childList);
        return new ListWrapper(name,this.problemReporter,this.ops,childList);
    }

    @Override
    public <T> TypedOutputList<T> list(String name, Codec<T> codec) {
        JsonArray childList = new JsonArray();
        this.output.add(name,childList);
        return new TypedListWrapper<>(this.problemReporter,name,this.ops,codec,childList);
    }

    @Override
    public void discard(String name) { this.output.remove(name); }

    @Override
    public boolean isEmpty() { return this.output.isEmpty(); }

    public JsonObject buildResult() { return this.output; }

    private static class ListWrapper implements ValueOutputList {

        private final String fieldName;
        private final ProblemReporter problemReporter;
        private final DynamicOps<JsonElement> ops;
        private final JsonArray output;

        private ListWrapper(String fieldName,ProblemReporter problemReporter,DynamicOps<JsonElement> ops,JsonArray output) {
            this.fieldName = fieldName;
            this.problemReporter = problemReporter;
            this.ops = ops;
            this.output = output;
        }
        @Override
        public ValueOutput addChild() {
            int newChildIndex = this.output.size();
            JsonObject child = new JsonObject();
            this.output.add(child);
            return new JsonValueOutput(this.problemReporter.forChild(new ProblemReporter.IndexedFieldPathElement(this.fieldName,newChildIndex)),this.ops,child);
        }
        @Override
        public void discardLast() { this.output.remove(this.output.size() - 1); }
        @Override
        public boolean isEmpty() { return this.output.isEmpty(); }

    }

    private static class TypedListWrapper<T> implements TypedOutputList<T> {
        private final ProblemReporter problemReporter;
        private final String name;
        private final DynamicOps<JsonElement> ops;
        private final Codec<T> codec;
        private final JsonArray output;

        private TypedListWrapper(ProblemReporter problemReporter,String name,DynamicOps<JsonElement> ops,Codec<T> codec,JsonArray outout) {
            this.problemReporter = problemReporter;
            this.name = name;
            this.ops = ops;
            this.codec = codec;
            this.output = outout;
        }
        @Override
        public void add(T value) {
            switch (this.codec.encodeStart(this.ops,value)) {
                case Success<JsonElement> success:
                    this.output.add(success.value());
                    break;
                case Error<JsonElement> error:
                    this.problemReporter.report(new TagValueOutput.EncodeToListFailedProblem(this.name,value,error));
                    error.partialValue().ifPresent(this.output::add);
                    break;
            }
        }
        @Override
        public boolean isEmpty() { return this.output.isEmpty(); }
    }

}
