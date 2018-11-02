package in.dailydelivery.dailydelivery.DB;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface CartDao {

    @Query("SELECT * FROM cart")
    List<Cart> getAll();

    @Query("SELECT COUNT(*) FROM cart")
    int count();

    @Query("SELECT uid FROM cart WHERE product_id = :pId")
    int getId(int pId);

    @Query("SELECT product_id,product_qty FROM cart WHERE cat_id = :catId")
    List<ProductTuple> getpIdofCatId(int catId);

    @Query("SELECT product_ddprice,product_qty FROM cart")
    List<PriceTuple> getprices();

    @Query("UPDATE cart SET product_qty = :qty WHERE product_id = :pId AND cat_id= :catId")
    void updateQty(int pId, int catId, int qty);

    @Query("DELETE FROM cart WHERE product_id = :pId AND cat_id = :catId")
    void deleteProd(int pId, int catId);

    @Insert
    void insertCart(Cart cart);

}
