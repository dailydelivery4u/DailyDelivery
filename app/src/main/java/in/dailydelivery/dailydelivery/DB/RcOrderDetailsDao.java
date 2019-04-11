package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface RcOrderDetailsDao {

    @Insert
    void insertRcOrderDetails(RcOrderDetails rcOrderDetails);

    @Query("SELECT * FROM rc_orderdetails")
    List<RcOrderDetails> getRcOrders();

    @Query("UPDATE rc_orderdetails SET mon = :mon, tue = :tue, wed = :wed, thu = :thu, fri =:fri, sat=:sat, sun=:sun, day1_qty =:day1Qty, day2_qty = :day2Qty, date_of_month = :dayOfMonth WHERE order_id = :orderId")
    void updateOrder(int mon, int tue, int wed, int thu, int fri, int sat, int sun, int orderId, int day1Qty, int day2Qty, int dayOfMonth);

    @Query("UPDATE rc_orderdetails SET status = :status WHERE order_id = :orderId")
    void updateStatus(int status, int orderId);

    @Query("UPDATE rc_orderdetails SET status = 2 WHERE order_id = :orderId")
    void deleteByOrderId(int orderId);
}
