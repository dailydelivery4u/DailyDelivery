package in.dailydelivery.dailydelivery.DB;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "vacation")
public class Vacation {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "start_date")
    private String startDate;

    @ColumnInfo(name = "end_date")
    private String endDate;

    public Vacation(String startDate, String endDate) {
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
}
