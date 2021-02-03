package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;

import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InsufficientAccBalanceException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.SameAccountCreationException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class PersistentAccountDAO implements AccountDAO {
    private DBHandler db;

    public PersistentAccountDAO(Context context){
        db = new DBHandler(context);
    }


    @Override
    public List<String> getAccountNumbersList() {
        return db.getAllAccountNumbers();
    }

    @Override
    public List<Account> getAccountsList() {
        return db.getAllAccounts();
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        return db.getAccount(accountNo);
    }

    @Override
    public void addAccount(Account account) throws SameAccountCreationException {
        db.createAccount(account);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        db.deleteAccount(accountNo);
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException, InsufficientAccBalanceException {
        db.updateAccount(accountNo,expenseType,amount);
    }
}
