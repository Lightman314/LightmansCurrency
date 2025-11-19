package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.atm;

import com.google.common.base.Predicates;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaValues;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Predicate;

public class ATMPeripheral extends LCPeripheral {

    public static ATMPeripheral INSTANCE = new ATMPeripheral();
    public static LazyOptional<IPeripheral> LAZY = LazyOptional.of(() -> INSTANCE);
    private ATMPeripheral() {}

    @Override
    public String getType() { return "lc_atm"; }

    public LCLuaTable getBankAccount(IArguments args) throws LuaException
    {
        BankReference br = BankReference.load(LCLuaTable.toTag(args.getTable(0)));
        if(br == null)
            throw LuaValues.badArgumentOf(args,0,"bankReferenceKey");
        IBankAccount account = br.get();
        if(account == null)
            throw new LuaException("Bank Account for that key could not be found!");
        return this.getBankAccountData(br,account);
    }

    public LCLuaTable searchBankAccounts(IArguments args) throws LuaException
    {
        String searchText = args.getString(0).toLowerCase(Locale.ENGLISH);
        return this.getMultiAccountData(account -> account.getName().getString().toLowerCase(Locale.ENGLISH).contains(searchText));
    }

    public LCLuaTable getAllBankAccounts()  { return this.getMultiAccountData(Predicates.alwaysTrue()); }

    private LCLuaTable getMultiAccountData(Predicate<IBankAccount> filter)
    {
        LCLuaTable table = new LCLuaTable();
        int index = 1;
        for(BankReference br : BankAPI.getApi().GetAllBankReferences(false))
        {
            IBankAccount account = br.get();
            if(account != null && filter.test(account))
                table.put(index++,this.getBankAccountData(br,account));
        }
        return table;
    }

    private LCLuaTable getBankAccountData(BankReference br,IBankAccount account)
    {
        LCLuaTable table = new LCLuaTable();
        table.put("ReferenceKey",LCLuaTable.fromTag(br.save()));
        table.put("Name",account.getName().getString());
        table.put("Balance",LCLuaTable.fromMoney(account));
        return table;
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("getBankAccount").withArgs(this::getBankAccount));
        registration.register(LCPeripheralMethod.builder("searchBankAccounts").withArgs(this::searchBankAccounts));
        registration.register(LCPeripheralMethod.builder("getAllBankAccounts").simple(this::getAllBankAccounts));
    }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) { return peripheral == INSTANCE; }

}