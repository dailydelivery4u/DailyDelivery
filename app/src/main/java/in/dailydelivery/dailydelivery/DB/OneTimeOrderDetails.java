package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "onetime_orderdetails")
public class OneTimeOrderDetails {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "order_id")
    private int orderId;

    @ColumnInfo(name = "product_id")
    private int productId;

    @ColumnInfo(name = "cat_id")
    private int catId;

    @ColumnInfo(name = "qty")
    private int qty;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "des")
    private String des;

    @ColumnInfo(name = "price")
    private int price;

    @ColumnInfo(name = "status")
    private int status;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "delivery_slot")
    private int deliverySlot;

    public OneTimeOrderDetails(int orderId, int productId, int catId, int qty, String name, String des, int price, int status, String date, int deliverySlot) {
        this.orderId = orderId;
        this.productId = productId;
        this.catId = catId;
        this.qty = qty;
        this.name = name;
        this.des = des;
        this.price = price;
        this.status = status;
        this.date = date;
        this.deliverySlot = deliverySlot;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDeliverySlot() {
        return deliverySlot;
    }

    public void setDeliverySlot(int deliverySlot) {
        this.deliverySlot = deliverySlot;
    }
}