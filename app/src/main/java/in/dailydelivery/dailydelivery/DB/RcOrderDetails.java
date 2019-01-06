package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "rc_orderdetails")
public class RcOrderDetails {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "order_id")
    private int orderId;

    @ColumnInfo(name = "product_id")
    private int productId;

    @ColumnInfo(name = "cat_id")
    private int catId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "des")
    private String des;

    @ColumnInfo(name = "price")
    private int price;

    @ColumnInfo(name = "status")
    private int status;

    @ColumnInfo(name = "delivery_slot")
    private int deliverySlot;

    @ColumnInfo(name = "start_date")
    private String startDate;

    @ColumnInfo(name = "mon")
    private int mon;
    @ColumnInfo(name = "tue")
    private int tue;
    @ColumnInfo(name = "wed")
    private int wed;
    @ColumnInfo(name = "thu")
    private int thu;
    @ColumnInfo(name = "fri")
    private int fri;
    @ColumnInfo(name = "sat")
    private int sat;
    @ColumnInfo(name = "sun")
    private int sun;

    public RcOrderDetails(int productId, int catId, String name, String des, int price, int status, int deliverySlot, String startDate, int mon, int tue, int wed, int thu, int fri, int sat, int sun) {
        this.productId = productId;
        this.catId = catId;
        this.name = name;
        this.des = des;
        this.price = price;
        this.status = status;
        this.deliverySlot = deliverySlot;
        this.startDate = startDate;
        this.mon = mon;
        this.tue = tue;
        this.wed = wed;
        this.thu = thu;
        this.fri = fri;
        this.sat = sat;
        this.sun = sun;
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

    public int getDeliverySlot() {
        return deliverySlot;
    }

    public void setDeliverySlot(int deliverySlot) {
        this.deliverySlot = deliverySlot;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public int getMon() {
        return mon;
    }

    public void setMon(int mon) {
        this.mon = mon;
    }

    public int getTue() {
        return tue;
    }

    public void setTue(int tue) {
        this.tue = tue;
    }

    public int getWed() {
        return wed;
    }

    public void setWed(int wed) {
        this.wed = wed;
    }

    public int getThu() {
        return thu;
    }

    public void setThu(int thu) {
        this.thu = thu;
    }

    public int getFri() {
        return fri;
    }

    public void setFri(int fri) {
        this.fri = fri;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }

    public int getSun() {
        return sun;
    }

    public void setSun(int sun) {
        this.sun = sun;
    }
}
