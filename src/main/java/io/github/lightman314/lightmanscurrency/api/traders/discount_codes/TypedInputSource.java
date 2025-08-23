package io.github.lightman314.lightmanscurrency.api.traders.discount_codes;

import com.google.common.collect.ImmutableSet;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TypedInputSource implements IDiscountCodeSource{

    @Override
    public int priority() { return 100; }

    private final Set<String> codes;
    public TypedInputSource() { this(new HashSet<>()); }
    public TypedInputSource(Set<String> codes) { this.codes = new HashSet<>(codes); }

    public void addCode(String code) { this.codes.add(code); }
    public void removeCode(String code) { this.codes.remove(code); }
    public void clearCodes() { this.codes.clear(); }
    public Set<String> getCodes() { return ImmutableSet.copyOf(this.codes); }

    @Override
    public boolean containsCode(String code) { return this.codes.contains(code); }
    @Override
    public Set<Integer> getDiscountCodes() { return ImmutableSet.copyOf(this.codes.stream().map(Object::hashCode).toList()); }
    @Override
    public boolean consumeCode(String code) { return this.containsCode(code); }

}
