package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.ColumnInfo;

public class ProductTuple {
    @ColumnInfo(name = "product_id")
    private int productId;

    @ColumnInfo(name = "product_qty")
    private int productqty;

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

}
