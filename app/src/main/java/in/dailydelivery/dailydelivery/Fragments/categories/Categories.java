package in.dailydelivery.dailydelivery.Fragments.categories;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.dailydelivery.dailydelivery.CreateOrderActivity;
import in.dailydelivery.dailydelivery.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Categories {

    public static List<category> ITEMS = new ArrayList<category>();

    public static void addItem(category item) {
        ITEMS.add(item);
    }

    public static class category {
        public final String id;
        public final String content;

        public category(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
