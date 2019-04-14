package in.dailydelivery.dailydelivery.Fragments.categories;

import java.util.ArrayList;

public class CatDataModel {
    private String catTypeHeader;
    private ArrayList<Category> allCategoriesInSection;

    public CatDataModel(String catType, ArrayList<Category> allCategoriesInSection) {
        this.catTypeHeader = catType;
        this.allCategoriesInSection = allCategoriesInSection;
    }

    public String getCatTypeHeader() {
        return catTypeHeader;
    }

    public void setCatTypeHeader(String catTypeHeader) {
        this.catTypeHeader = catTypeHeader;
    }

    public ArrayList<Category> getAllCategoriesInSection() {
        return allCategoriesInSection;
    }

    public void setAllCategoriesInSection(ArrayList<Category> allCategoriesInSection) {
        this.allCategoriesInSection = allCategoriesInSection;
    }
}
