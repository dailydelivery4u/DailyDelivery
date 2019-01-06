package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "wallet_transaction")
public class WalletTransaction {

    @PrimaryKey(autoGenerate = true)
    private int uid;
    @ColumnInfo(name = "transaction_id")
    private int transactionId;
    @ColumnInfo(name = "transaction_type")
    private int transactionType;
    @ColumnInfo(name = "transaction_desc")
    private String transactionDesc;
    @ColumnInfo(name = "transaction_amount")
    private int transactionAmount;
    @ColumnInfo(name = "transaction_date")
    private String transactionDate;

    public WalletTransaction(int transactionId, int transactionType, String transactionDesc, int transactionAmount, String transactionDate) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.transactionDesc = transactionDesc;
        this.transactionAmount = transactionAmount;
        this.transactionDate = transactionDate;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionDesc() {
        return transactionDesc;
    }

    public void setTransactionDesc(String transactionDesc) {
        this.transactionDesc = transactionDesc;
    }

    public int getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(int transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
}
