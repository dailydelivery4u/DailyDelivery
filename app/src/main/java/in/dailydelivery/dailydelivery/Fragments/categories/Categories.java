package in.dailydelivery.dailydelivery.Fragments.categories;

import java.util.ArrayList;
import java.util.List;

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
        public final int id;
        public final String content;

        public category(int id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
