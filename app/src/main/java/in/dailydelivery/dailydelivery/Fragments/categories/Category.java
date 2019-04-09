package in.dailydelivery.dailydelivery.Fragments.categories;

public class Category {

    private int id;
    private int deliverySlot;
    private String catName;
    private String pic;
    private int catTypeId;

    public Category(int id, int deliverySlot, String catName, String pic, int catTypeId) {
        this.id = id;
        this.deliverySlot = deliverySlot;
        this.catName = catName;
        this.pic = pic;
        this.catTypeId = catTypeId;
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

    public int getCatTypeId() {
        return catTypeId;
    }

    public void setCatTypeId(int catTypeId) {
        this.catTypeId = catTypeId;
    }
}
