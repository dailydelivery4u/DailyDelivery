package in.dailydelivery.dailydelivery.DB;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "vacation")
public class Vacation {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "vac_id")
    private int vacId;

    @ColumnInfo(name = "start_date")
    private String startDate;

    @ColumnInfo(name = "end_date")
    private String endDate;

    public Vacation(int vacId, String startDate, String endDate) {
        this.vacId = vacId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getVacId() {
        return vacId;
    }

    public void setVacId(int vacId) {
        this.vacId = vacId;
    }
}
