package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface WalletTransactionDao {

    @Query("SELECT * FROM wallet_transaction ORDER BY uid DESC")
    List<WalletTransaction> getAll();

    @Insert
    void insertWalletTransaction(WalletTransaction walletTransaction);

}
