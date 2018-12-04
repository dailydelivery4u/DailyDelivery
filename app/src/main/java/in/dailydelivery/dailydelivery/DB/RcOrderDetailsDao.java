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

}
