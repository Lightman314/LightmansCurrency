package io.github.lightman314.lightmanscurrency.integration.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class PeripheralMethod {

    private final String name;
    public String getMethodName() { return this.name; }
    private final Supplier<Boolean> accessible;
    public boolean isAccessible() { return this.accessible.get(); }
    private final LuaBiFunction<IComputerAccess,IArguments,Object[]> method;
    private PeripheralMethod(String name, Supplier<Boolean> accessible,LuaBiFunction<IComputerAccess,IArguments,Object[]> method) { this.name = name; this.accessible = accessible; this.method = method; }

    public Object[] execute(IComputerAccess computer,IArguments args) throws LuaException{
        Object[] result = this.method.apply(computer,args);
        if(result != null)
            return result;
        return new Object[]{};
    }

    public interface LuaConsumer<A> { void accept(A value) throws LuaException; }
    public interface LuaBiConsumer<A,B> { void accept(A value1,B value2) throws LuaException; }
    public interface LuaFunction<A,R> { @Nullable R apply(A value) throws LuaException; }
    public interface LuaBiFunction<A,B,R> { @Nullable R apply(A value1, B value2) throws LuaException; }
    public interface LuaSupplier<R> { @Nullable R get() throws LuaException; }
    public interface LuaRunnable { void run() throws LuaException; }

    public static Builder builder(String name) { return new Builder(name); }

    public static class Builder
    {
        private final String name;
        private Supplier<Boolean> accessible = () -> true;
        private LuaBiFunction<IComputerAccess,IArguments,Object[]> method = null;
        private Builder(String name) { this.name = name; }

        public Builder withAccess(Supplier<Boolean> canAccess) { this.accessible = canAccess; return this; }

        private Builder noReturn(LuaBiConsumer<IComputerAccess,IArguments> method) { this.method = (comp,args) -> { method.accept(comp,args); return null; }; return this; }
        private Builder singleReturn(LuaBiFunction<IComputerAccess,IArguments,Object> method) { this.method = (comp,args) -> new Object[] {method.apply(comp,args)}; return this; }

        public Builder withContextArray(LuaBiFunction<IComputerAccess,IArguments,Object[]> method) { this.method = method; return this; }
        public Builder withContext(LuaBiFunction<IComputerAccess,IArguments,Object> method) { return singleReturn(method); }
        public Builder withContext(LuaBiConsumer<IComputerAccess,IArguments> method) { return noReturn(method); }

        public Builder withContextOnlyArray(LuaFunction<IComputerAccess,Object[]> method) { this.method = (comp,args) -> method.apply(comp); return this; }
        public Builder withContextOnly(LuaFunction<IComputerAccess,Object> method) { return singleReturn((comp,args) -> method.apply(comp)); }
        public Builder withContextOnly(LuaConsumer<IComputerAccess> method) { return noReturn((comp,args) -> method.accept(comp)); }

        public Builder withArgsArray(LuaFunction<IArguments,Object[]> method) { this.method = (comp,args) -> method.apply(args); return this; }
        public Builder withArgs(LuaFunction<IArguments,Object> method) { return singleReturn((comp,args) -> method.apply(args)); }
        public Builder withArgs(LuaConsumer<IArguments> method) { return noReturn((comp,args) -> method.accept(args)); }

        public Builder simpleArray(LuaSupplier<Object[]> method) { this.method = (comp,args) -> method.get(); return this; }
        public Builder simple(LuaSupplier<Object> method) { return singleReturn((comp,args) -> method.get()); }
        public Builder simple(LuaRunnable method) { return noReturn((comp,args) -> method.run()); }

        public PeripheralMethod build() { return new PeripheralMethod(this.name,this.accessible,Objects.requireNonNull(this.method)); }

    }

    public static final class Registration
    {
        private final Map<String,PeripheralMethod> methodMap = new HashMap<>();
        public Registration() { }

        public List<PeripheralMethod> getResults() { return new ArrayList<>(this.methodMap.values()); }

        public void register(PeripheralMethod method) { this.methodMap.put(method.getMethodName(),method); }
        public void register(PeripheralMethod.Builder builder) { this.register(builder.build()); }
        public void remove(String method) { this.methodMap.remove(method); }

    }

}
