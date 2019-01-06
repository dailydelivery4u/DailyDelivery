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

    @Query("UPDATE rc_orderdetails SET mon = :mon, tue = :tue, wed = :wed, thu = :thu, fri =:fri, sat=:sat, sun=:sun WHERE order_id = :orderId")
    void updateOrder(int mon, int tue, int wed, int thu, int fri, int sat, int sun, int orderId);

    @Query("UPDATE rc_orderdetails SET status = :status WHERE order_id = :orderId")
    void updateStatus(int status, int orderId);

    @Query("DELETE FROM rc_orderdetails WHERE order_id = :orderId")
    void deleteByOrderId(int orderId);
}
