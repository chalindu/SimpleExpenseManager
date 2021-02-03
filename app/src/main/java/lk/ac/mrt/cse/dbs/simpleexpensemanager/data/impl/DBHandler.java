package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InsufficientAccBalanceException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.SameAccountCreationException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class DBHandler extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DB_NAME = "180325B";

    // Table Names
    private static final String TABLE_ACCOUNT = "account";
    private static final String TABLE_TRANSACTION = "transactions";

    // Common column names
    private static final String KEY_ACCOUNT_NO = "account_no";

    // Account Table - column names
    private static final String KEY_BANK_NAME = "bank_name";
    private static final String KEY_ACCOUNT_HOLDER_NAME = "account_holder_name";
    private static final String KEY_BALANCE = "balance";

    // Transaction Table - column names
    private static final String KEY_ID = "id";
    private static final String KEY_EXPENSE_TYPE = "expense_type";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_DATE = "date";

    // Table Create Statements
    // Account table create statement
    private static final String CREATE_TABLE_ACCOUNT = "CREATE TABLE " + TABLE_ACCOUNT +
            "("
            + KEY_ACCOUNT_NO + " TEXT PRIMARY KEY,"
            + KEY_BANK_NAME + " TEXT,"
            + KEY_ACCOUNT_HOLDER_NAME + " TEXT,"
            + KEY_BALANCE + " REAL" +
            ");";

    // Transaction table create statement
    private static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE " + TABLE_TRANSACTION +
            "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ACCOUNT_NO + " TEXT,"
            + KEY_EXPENSE_TYPE + " TEXT,"
            + KEY_DATE + " TEXT,"
            + KEY_AMOUNT + " REAL" +
            ");";

    // Constructor
    public DBHandler(@Nullable Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_ACCOUNT);
        db.execSQL(CREATE_TABLE_TRANSACTION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);

        // create new tables
        onCreate(db);
    }

    //create an account
    public void createAccount(Account account) throws SameAccountCreationException {
        //get access to write
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ACCOUNT_NO, account.getAccountNo());
        values.put(KEY_BANK_NAME, account.getBankName());
        values.put(KEY_ACCOUNT_HOLDER_NAME, account.getAccountHolderName());
        values.put(KEY_BALANCE, account.getBalance());
        //check for same account creation
        Account checkAccount = null;
        try {
            checkAccount = getAccount(account.getAccountNo());
        } catch (InvalidAccountException e) {
            // insert row
            db.insert(TABLE_ACCOUNT, null, values);
        }
        String msg = "You have created this account number " + account.getAccountNo() + " before. Please enter another account number";
        if (checkAccount != null) throw new SameAccountCreationException(msg);

    }

    //get an account
    public Account getAccount(String account_no) throws InvalidAccountException{
        SQLiteDatabase db = this.getReadableDatabase();

            String selectQuery = "SELECT  * FROM " + TABLE_ACCOUNT +
                " WHERE " + KEY_ACCOUNT_NO + " = ?" ;
        //run query
        Cursor c = db.rawQuery(selectQuery, new String[] {account_no});

        if (c != null){
            c.moveToFirst();

            Account account = new Account(null, null,null, 0);
            account.setAccountNo(c.getString(c.getColumnIndex(KEY_ACCOUNT_NO)));
            account.setBankName((c.getString(c.getColumnIndex(KEY_BANK_NAME))));
            account.setAccountHolderName(c.getString(c.getColumnIndex(KEY_ACCOUNT_HOLDER_NAME)));
            account.setBalance(c.getDouble(c.getColumnIndex(KEY_BALANCE)));

            return account;
        }
        String msg = "Account " + account_no + " is invalid.";
        throw new InvalidAccountException(msg);
    }

    //get all accounts
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<Account>();
        String selectQuery = "SELECT  * FROM " + TABLE_ACCOUNT;

        //get access to read
        SQLiteDatabase db = this.getReadableDatabase();
        //run query
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Account account = new Account(null, null,null, 0);
                account.setAccountNo(c.getString((c.getColumnIndex(KEY_ACCOUNT_NO))));
                account.setBankName((c.getString(c.getColumnIndex(KEY_BANK_NAME))));
                account.setAccountHolderName(c.getString(c.getColumnIndex(KEY_ACCOUNT_HOLDER_NAME)));
                account.setBalance(c.getDouble(c.getColumnIndex(KEY_BALANCE)));

                // adding to account list
                accounts.add(account);
            } while (c.moveToNext());
        }

        return accounts;
    }

    //get all account numbers list
    public List<String> getAllAccountNumbers() {
        List<String> accountNumbers = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT "+ KEY_ACCOUNT_NO +" FROM " + TABLE_ACCOUNT;
        Log.e(LOG, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                accountNumbers.add(c.getString(c.getColumnIndex(KEY_ACCOUNT_NO)));
            } while (c.moveToNext());
        }
        return accountNumbers;
    }

    //Update an account
    public void updateAccount(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException, InsufficientAccBalanceException {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT "+ KEY_BALANCE +" FROM " + TABLE_ACCOUNT + " WHERE "
                + KEY_ACCOUNT_NO + " = ?";
        Log.e(LOG, selectQuery);
        Cursor c = db.rawQuery(selectQuery, new String[]{accountNo});

        if (c != null) {
            c.moveToFirst();
            Double balance = c.getDouble(c.getColumnIndex(KEY_BALANCE));
            SQLiteDatabase dbWrite = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            switch (expenseType) {
                case EXPENSE:
                    String msg = "Your current balance is "+balance+". It is not sufficient do this transaction";
                    if(balance-amount<0)throw new InsufficientAccBalanceException(msg);
                    values.put(KEY_BALANCE, balance - amount);
                    break;
                case INCOME:
                    values.put(KEY_BALANCE,  balance + amount);
                    break;
            }
            // updating row
            dbWrite.update(TABLE_ACCOUNT, values, KEY_ACCOUNT_NO + " = ?",
                    new String[] { accountNo });
        }else{
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }

    }

    //Delete an account
    public void deleteAccount(String account_no) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACCOUNT, KEY_ACCOUNT_NO + " = ?",
                new String[] { String.valueOf(account_no) });
    }

    //create an transaction
    public void createTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH🇲🇲ss'Z'");
        String strDate = dateFormat.format(date);

        ContentValues values = new ContentValues();
        values.put(KEY_ACCOUNT_NO, accountNo);
        values.put(KEY_EXPENSE_TYPE, expenseType.toString());
        values.put(KEY_AMOUNT, amount);
        values.put(KEY_DATE, strDate);

        // insert row
        db.insert(TABLE_TRANSACTION, null, values);

    }

    //get all transactions
    public List<Transaction> getAllTransactionLogs() {
        List<Transaction> transactionsList = new ArrayList<Transaction>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_TRANSACTION;
        Log.e(LOG, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH🇲🇲ss'Z'");


        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Date date = null;
                try {
                    date = dateFormat.parse(c.getString(c.getColumnIndex(KEY_DATE)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String expenseTypeAsString = c.getString(c.getColumnIndex(KEY_EXPENSE_TYPE));
                ExpenseType expenseTypeEnum = null;
                switch (expenseTypeAsString) {
                    case "EXPENSE":
                        expenseTypeEnum = ExpenseType.EXPENSE;
                        break;
                    case "INCOME":
                        expenseTypeEnum = ExpenseType.INCOME;
                        break;
                }
                Transaction transaction = new Transaction(
                        date,
                        c.getString(c.getColumnIndex(KEY_ACCOUNT_NO)),
                        expenseTypeEnum,
                        c.getDouble(c.getColumnIndex(KEY_AMOUNT))
                );
                // adding to accountNumbers list
                transactionsList.add(transaction);
            } while (c.moveToNext());
        }
        return transactionsList;
    }

    //get all transactions within limit
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        List<Transaction> transactionsList = getAllTransactionLogs();

        int size = transactionsList.size();
        if (size <= limit) {
            return transactionsList;
        }
        // return the last <code>limit</code> number of transaction logs
        return transactionsList.subList(size - limit, size);
    }



}
