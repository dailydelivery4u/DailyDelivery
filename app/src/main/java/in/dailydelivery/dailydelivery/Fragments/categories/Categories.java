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

        public category(int id, int deliverySlot, String catName) {
            this.id = id;
            this.deliverySlot = deliverySlot;
            this.catName = catName;
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
    }
}
