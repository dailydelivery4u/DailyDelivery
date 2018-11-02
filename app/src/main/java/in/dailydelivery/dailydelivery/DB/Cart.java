package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "cart")
public class Cart {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    @ColumnInfo(name = "cat_id")
    private int catId;

    @ColumnInfo(name = "product_id")
    private int productId;

    @ColumnInfo(name = "product_name")
    private String productName;

    @ColumnInfo(name = "product_des")
    private String productDes;

    @ColumnInfo(name = "product_mrp")
    private int productMrp;

    @ColumnInfo(name = "product_ddprice")
    private int productDdprice;

    @ColumnInfo(name = "product_qty")
    private int productqty;

    @ColumnInfo(name = "product_tn")
    private String productTnUrl;

    public Cart(int catId, int productId, String productName, String productDes, int productMrp, int productDdprice, int productqty, String productTnUrl) {
        this.catId = catId;
        this.productId = productId;
        this.productName = productName;
        this.productDes = productDes;
        this.productMrp = productMrp;
        this.productDdprice = productDdprice;
        this.productqty = productqty;
        this.productTnUrl = productTnUrl;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDes() {
        return productDes;
    }

    public void setProductDes(String productDes) {
        this.productDes = productDes;
    }

    public int getProductMrp() {
        return productMrp;
    }

    public void setProductMrp(int productMrp) {
        this.productMrp = productMrp;
    }

    public int getProductDdprice() {
        return productDdprice;
    }

    public void setProductDdprice(int productDdprice) {
        this.productDdprice = productDdprice;
    }

    public int getProductqty() {
        return productqty;
    }

    public void setProductqty(int productqty) {
        this.productqty = productqty;
    }


    public String getProductTnUrl() {
        return productTnUrl;
    }

    public void setProductTnUrl(String productTnUrl) {
        this.productTnUrl = productTnUrl;
    }
}
