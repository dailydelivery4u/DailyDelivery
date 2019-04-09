package in.dailydelivery.dailydelivery.Fragments.products;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Products implements Serializable {

    public static final List<Product> ITEMS = new ArrayList<Product>();


    public static void addItem(Product item) {
        ITEMS.add(item);
    }

    public static class Product implements Serializable {
        private int id;
        private int cat_id;
        private String productName;
        private String productDes;
        private String productQty;
        private int mrp;
        private int ddPrice;
        private String thumbnailUrl;
        private boolean in_cart;
        private int qty;
        private int deliverySlot;

        public Product(int id, int cat_id, String productName, String productDes, String productQty, int mrp, int ddPrice, String thumbnailUrl, boolean in_cart, int qty, int deliverySlot) {
            this.id = id;
            this.cat_id = cat_id;
            this.productName = productName;
            this.productDes = productDes;
            this.productQty = productQty;
            this.mrp = mrp;
            this.ddPrice = ddPrice;
            this.thumbnailUrl = thumbnailUrl;
            this.in_cart = in_cart;
            this.qty = qty;
            this.deliverySlot = deliverySlot;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getCat_id() {
            return cat_id;
        }

        public void setCat_id(int cat_id) {
            this.cat_id = cat_id;
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

        public int getMrp() {
            return mrp;
        }

        public void setMrp(int mrp) {
            this.mrp = mrp;
        }

        public int getDeliverySlot() {
            return deliverySlot;
        }

        public void setDeliverySlot(int deliverySlot) {
            this.deliverySlot = deliverySlot;
        }

        public int getDdPrice() {
            return ddPrice;
        }

        public void setDdPrice(int ddPrice) {
            this.ddPrice = ddPrice;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }

        public boolean isIn_cart() {
            return in_cart;
        }

        public void setIn_cart(boolean in_cart) {
            this.in_cart = in_cart;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }

        public String getProductQty() {
            return productQty;
        }

        public void setProductQty(String productQty) {
            this.productQty = productQty;
        }

        @Override
        public String toString() {
            return productName;
        }
    }
}
