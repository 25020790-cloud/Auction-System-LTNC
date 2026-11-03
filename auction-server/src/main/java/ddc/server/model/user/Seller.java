package ddc.server.model.user;

import java.util.ArrayList;
import java.util.List;

public class Seller extends User {
    // Thuoc tinh dac thu cua nguoi ban: danh sach cac san pham dang ban
    private List<String> postedItemIds;

    public Seller(String userId, String userName, String password, String email) {
        super(userId, userName, password, email);
        this.postedItemIds = new ArrayList<>();
    }

    // getters and setters for postedItemIds
    public List<String> getPostedItemIds() {
        return postedItemIds;
    }
    public void setPostedItemIds(List<String> postedItemIds) {
        this.postedItemIds = postedItemIds;
    }

    // ham tien ich : them mot ma san pham moi vao danh sach cac san pham dang ban
    public void addPostedItemId(String itemId) {
        if (itemId != null && !itemId.isEmpty()) {
            this.postedItemIds.add(itemId);
        }
    }
}
