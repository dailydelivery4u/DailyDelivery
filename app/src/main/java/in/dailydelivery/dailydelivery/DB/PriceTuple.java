package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.ColumnInfo;

public class PriceTuple {

    @ColumnInfo(name = "product_ddprice")
    private int productDdPrice;

    @ColumnInfo(name = "product_qty")
    private int productqty;

    public int getProductDdPrice() {
        return productDdPrice;
    }

    public void setProductDdPrice(int productDdPrice) {
        this.productDdPrice = productDdPrice;
    }

    public int getProductqty() {
        return productqty;
    }

    public void setProductqty(int productqty) {
        this.productqty = productqty;
    }
}
