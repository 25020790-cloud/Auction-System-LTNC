package ddc.server.model.user;

public class Bidder extends User {
    private double balance; // thuoc tinh dac thu ring cua nguoi mua : so du tkhoan
    
    public Bidder(String userId, String userName, String password, String email, double balance) {
        super(userId, userName, password, email);
        this.balance = balance;
    }

    // getters and setters for balance
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }

    // ham nap them tien vao tai khoan
    public void addBalance(double amount){
        if (amount > 0) {
            this.balance += amount;
        }
    }
}
