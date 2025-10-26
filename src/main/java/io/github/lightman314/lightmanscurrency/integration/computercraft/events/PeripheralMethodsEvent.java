package io.github.lightman314.lightmanscurrency.integration.computercraft.events;

import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.PeripheralMethod;
import net.neoforged.bus.api.Event;

import java.util.List;
import java.util.function.Consumer;

public class PeripheralMethodsEvent extends Event {

    private final LCPeripheral peripheral;
    public LCPeripheral getPeripheral() { return this.peripheral; }
    public String getPeripheralType() { return this.peripheral.getType(); }
    public List<String> getPeripheralTypes() { return this.peripheral.getTypeList(); }
    public boolean isType(String type) { return this.peripheral.isType(type); }
    private final PeripheralMethod.Registration registration;
    public PeripheralMethodsEvent(LCPeripheral peripheral, PeripheralMethod.Registration registration)
    {
        this.peripheral = peripheral;
        this.registration = registration;
    }

    public void register(PeripheralMethod method) { this.registration.register(method); }
    public void register(PeripheralMethod.Builder builder) { this.registration.register(builder); }
    public void remove(String method) { this.registration.remove(method); }

}
