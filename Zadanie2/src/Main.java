import java.util.concurrent.locks.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;

class Bank {
    public static final int N = 10;
    
    private int [] Balances = new int[N];
    private Lock [] Locks = new Lock[N];
    
    public Bank() 
    {
        for (int i = 0; i < Locks.length; ++i) {
            Locks[i] = new ReentrantLock();
        }
    }
    
    public void deposit(int accountId, int amount) 
    {
        Balances[accountId] += amount;
    }
    
    public int getBalance(int accountId) 
    {
        return Balances[accountId]; 
    }
    
    public boolean transfer(int fromAccount, int toAccount, int amount) 
    {
        Locks[fromAccount].lock();
        try {
            Balances[fromAccount] -= amount;
        } finally {
            Locks[fromAccount].unlock();
        }
        
        Locks[toAccount].lock();
        try {
            Balances[toAccount] += amount;
        } finally {
            Locks[toAccount].unlock();
        }
        
        return true;
    }
    
    public void equalize(int accountA, int accountB) 
    {
        boolean LockA = false;
        boolean LockB = false;
        
        while(true)
        {
            LockA = Locks[accountA].tryLock();
            LockB = Locks[accountB].tryLock();
            
            try {
                if(LockA && LockB) 
                {
                    try {
                        int total = Balances[accountA] + Balances[accountB];
                        Balances[accountA] = total / 2 + total % 2;
                        Balances[accountB] = total / 2;
                    } finally {
                        Locks[accountA].unlock();
                        Locks[accountB].unlock();
                    }
                    break;
                }
                else
                {
                    if(LockA)
                    {
                        Locks[accountA].unlock();
                    }
                    if(LockB)
                    {
                        Locks[accountB].unlock();
                    }
                }
            } finally {
                Thread.yield();
            }
        }
    }
}

class Accountant extends Thread 
{
    Bank bank;
    public Accountant(Bank bank) 
    { 
        this.bank = bank; 
    }
    
    @Override
    public void run() 
    {
        Random rng = ThreadLocalRandom.current();
        for (int i = 0; i < 1000; ++i) 
        {
            // Try to transfer a random amount between a pair of accounts
            // The accounts numbers (ids) are also selected randomly
            int fromAccount = rng.nextInt(Bank.N-1);
            int toAccount = rng.nextInt(Bank.N-1);
            while (toAccount == fromAccount) 
            { // Source should differ from
                // the target
                toAccount = rng.nextInt(Bank.N-1); // Try again
            }
            
            if (rng.nextBoolean()) 
            { 
                // 50% of the time we transfer...
                bank.transfer(fromAccount, toAccount, rng.nextInt(100));
            } 
            else 
            { 
                // ...the remaining 50% of the time we equalize
                bank.equalize(fromAccount, toAccount);
            }
        }
    }
}

class Banking 
{
    public static void main(String [] args) throws InterruptedException {
        
        Bank bank = new Bank();
        for (int i = 0; i < Bank.N; ++i) 
        {
            bank.deposit(i, 100);
        }
        
        Thread [] threads = new Thread[10];
        for (int i = 0; i < threads.length; ++i) 
        {
            threads[i] = new Accountant(bank);
        }
        
        for (Thread t : threads) 
        {
            t.start(); 
        }
        for (Thread t : threads) {
            t.join(); 
        }
        
        int total = 0;
        for (int i = 0; i < Bank.N; ++i) 
        {
            int b = bank.getBalance(i);
            total += b;
            System.out.printf("Account [%d] balance: %d\n", i, b);
        }
        
        System.out.printf("Total balance is %d\tvalid value is %d\n",
                total, Bank.N * 100);
    }
}