package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface OneTimeOrderDetailsDao {

    @Insert
    void insertOnetimeOrderDetails(OneTimeOrderDetails orderDetails);


    @Query("SELECT * FROM onetime_orderdetails WHERE date = :date")
    List<OneTimeOrderDetails> getOrdersForTheDay(String date);
}
