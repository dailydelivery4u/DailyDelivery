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

    @Query("UPDATE vacation SET start_date = :sd, end_date = :ed WHERE vac_id = :id")
    void updateVac(String sd, String ed, int id);

    @Query("DELETE FROM vacation WHERE vac_id = :id")
    void deleteVac(int id);
}
