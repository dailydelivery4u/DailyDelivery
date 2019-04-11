package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;


@Database(entities = {Cart.class, OneTimeOrderDetails.class, RcOrderDetails.class, Vacation.class, WalletTransaction.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE rc_orderdetails ADD COLUMN frequency INTEGER DEFAULT 1 NOT NULL");
            database.execSQL("ALTER TABLE rc_orderdetails ADD COLUMN day1_qty INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE rc_orderdetails ADD COLUMN day2_qty INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE rc_orderdetails ADD COLUMN date_of_month INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE rc_orderdetails ADD COLUMN date_of_month INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE rc_orderdetails ADD COLUMN qty_des STRING DEFAULT '' NOT NULL");
            database.execSQL("ALTER TABLE cart ADD COLUMN product_qty_des STRING DEFAULT '' NOT NULL");
            database.execSQL("ALTER TABLE onetime_orderdetails ADD COLUMN des_qty STRING DEFAULT '' NOT NULL");
        }
    };
    private static AppDatabase INSTANCE;

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "dd-database")
                            .addMigrations(MIGRATION_1_2)
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


