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

    @Query("DELETE FROM onetime_orderdetails WHERE order_id = :orderId")
    void deleteByOrderId(int orderId);

    @Query("UPDATE onetime_orderdetails SET qty = :qty WHERE order_id = :orderId")
    void updateOrder(int qty, int orderId);

    @Query("UPDATE onetime_orderdetails SET status = :status WHERE order_id = :orderId")
    void updateStatus(int status, int orderId);

}
