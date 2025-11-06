package io.github.lightman314.lightmanscurrency.integration.computercraft.events;

import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class PeripheralMethodsEvent extends Event {

    private final LCPeripheral peripheral;
    public LCPeripheral getPeripheral() { return this.peripheral; }
    public String getPeripheralType() { return this.peripheral.getType(); }
    public List<String> getPeripheralTypes() { return this.peripheral.getTypeList(); }
    public boolean isType(String type) { return this.peripheral.isType(type); }
    private final LCPeripheralMethod.Registration registration;
    public PeripheralMethodsEvent(LCPeripheral peripheral, LCPeripheralMethod.Registration registration)
    {
        this.peripheral = peripheral;
        this.registration = registration;
    }

    public void register(LCPeripheralMethod method) { this.registration.register(method); }
    public void register(LCPeripheralMethod.Builder builder) { this.registration.register(builder); }
    public void remove(String method) { this.registration.remove(method); }

}