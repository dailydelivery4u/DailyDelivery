package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface VacationDao {

    @Insert
    void addVacation(Vacation vacation);

    @Query("SELECT * FROM vacation")
    List<Vacation> getAll();

}
