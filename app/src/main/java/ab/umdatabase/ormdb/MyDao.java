package ab.umdatabase.ormdb;


import java.sql.SQLException;

import ab.umdatabase.AppApplication;
import ab.umdatabaselib.dao.OrmDaoUtils;
import ab.umdatabaselib.helper.OrmDatabaseHelper;


public class MyDao extends OrmDaoUtils {
    public MyDao(Class cls) throws SQLException {
        super(cls);
    }

    @Override
    protected OrmDatabaseHelper getHelper() {
        return MyDbHelper.getInstance(AppApplication.context);
    }
}
