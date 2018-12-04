package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.ColumnInfo;

public class ProductTuple {
    @ColumnInfo(name = "product_id")
    private int productId;

    @ColumnInfo(name = "product_qty")
    private int productqty;

    @ColumnInfo(name = "delivery_slot")
    private int delivery_slot;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getProductqty() {
        return productqty;
    }

    public void setProductqty(int productqty) {
        this.productqty = productqty;
    }

    public int getDelivery_slot() {
        return delivery_slot;
    }

    public void setDelivery_slot(int delivery_slot) {
        this.delivery_slot = delivery_slot;
    }
}
