package ab.umdatabase.ormdb;

import android.content.Context;

import java.util.List;

import ab.umdatabase.model.Order;
import ab.umdatabase.model.Product;
import ab.umdatabase.model.User;
import ab.umdatabaselib.helper.OrmDatabaseHelper;


public class MyDbHelper extends OrmDatabaseHelper {

    private static final String DEF_DB_NAME = "ab_db";
    private static final int DB_VERSION = 1;//修改版本号测试数据迁移和版本升级
    private static MyDbHelper dbHelper;


    private MyDbHelper(Context context) {
        super(context, DEF_DB_NAME, null, DB_VERSION);
    }

    public static MyDbHelper getInstance(Context context){
        if(dbHelper == null){
            synchronized (MyDbHelper.class){
                if(dbHelper == null){
                    dbHelper = new MyDbHelper(context);
                }
            }
        }
        return dbHelper;
    }
    @Override
    public void createTables(List tables) {
        tables.add(User.class);
        tables.add(Product.class);

    }
    @Override
    public void updateTables(List tables) {
        tables.add(User.class);
        tables.add(Product.class);
        tables.add(Order.class);
    }


}
