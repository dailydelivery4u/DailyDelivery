package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


@Database(entities = {Cart.class, OneTimeOrderDetails.class, RcOrderDetails.class, Vacation.class, WalletTransaction.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "dd-database")
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public abstract CartDao userDao();

    public abstract OneTimeOrderDetailsDao oneTimeOrderDetailsDao();

    public abstract RcOrderDetailsDao rcOrderDetailsDao();

    public abstract VacationDao vacationDao();

    public abstract WalletTransactionDao walletTransactionDao();
}


