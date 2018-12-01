package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

@Dao
public interface RcOrderDetailsDao {

    @Insert
    void insertRcOrderDetails(RcOrderDetails rcOrderDetails);

}
