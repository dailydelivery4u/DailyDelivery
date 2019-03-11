package in.dailydelivery.dailydelivery.Fragments.categories;

import java.util.ArrayList;
import java.util.List;


public class Categories {

    public static List<category> ITEMS = new ArrayList<category>();

    public static void addItem(category item) {
        ITEMS.add(item);
    }

    public static class category {
        private int id;
        private int deliverySlot;
        private String catName;
        private String pic;

        public category(int id, int deliverySlot, String catName, String pic) {
            this.id = id;
            this.deliverySlot = deliverySlot;
            this.catName = catName;
            this.pic = pic;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getDeliverySlot() {
            return deliverySlot;
        }

        public void setDeliverySlot(int deliverySlot) {
            this.deliverySlot = deliverySlot;
        }

        public String getCatName() {
            return catName;
        }

        public void setCatName(String catName) {
            this.catName = catName;
        }

        public String getPic() {
            return pic;
        }

        public void setPic(String pic) {
            this.pic = pic;
        }
    }
}
