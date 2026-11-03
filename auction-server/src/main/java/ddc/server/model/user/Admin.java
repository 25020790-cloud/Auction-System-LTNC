package ddc.server.model.user;

public class Admin extends User {
    // thuoc tinh dac thu cua quan tri vien 
    private String permission;

    public Admin(String userId, String userName, String password, String email, String permission) {
        super(userId, userName, password, email);
        this.permission = permission;
    }
    
    // getters and setters for permission
    public String getPermission() {
        return permission;
    }
    public void setPermission(String permission) {
        this.permission = permission;
    }
}
