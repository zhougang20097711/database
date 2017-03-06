package ab.umdatabaselib.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * DB OpenHelper
 * 数据库新建，版本更新
 * Created by AB051788 on 2017/2/27.
 */
public abstract class OrmDatabaseHelper<T> extends OrmLiteSqliteOpenHelper {

	private List<Class<T>> DBtables = new ArrayList<Class<T>>();

	public OrmDatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
		super(context, databaseName, factory, databaseVersion);
	}

	/**
	 * 数据库创建   建初始表
	 * @param sqLiteDatabase  SQLiteDatabase
	 * @param connectionSource  ConnectionSource
	 */
	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
		Log.i("db", "onCreate");
		try {
			DBtables.clear();
			updateTables(DBtables);
			for (Class table : DBtables) {
				TableUtils.createTable(connectionSource, table);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 *数据库版本更新
	 *新增表、删除表、更新原表结构
	 * @param sqLiteDatabase  SQLiteDatabase
	 * @param connectionSource  ConnectionSource
	 * @param i oldversion
	 * @param i1 newversion
	 */
	@Override
	public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final ConnectionSource connectionSource, int i, int i1) {
		Log.i("db", "onUpgrade");
		try {
//			long old = System.currentTimeMillis();
			TransactionManager.callInTransaction(connectionSource, new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					DBtables.clear();
					updateTables(DBtables);
					try {
						for (Class table : DBtables) {
							Dao dao = getDao(table);
							if (dao.isTableExists()) {
								UpgradeUtils.upgradeTable(sqLiteDatabase, connectionSource, table);
							} else {
								//数据原来不存在的表直接创建
								TableUtils.dropTable(connectionSource, table, true);
								TableUtils.createTable(connectionSource, table);
							}
						}
					} catch (SQLException e) {

						return false;
					}
					return true;
				}
			});
//			long now = System.currentTimeMillis();
//			Log.i("db",(now - old) + "");  //更新5w条数据用时0.041s
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

//	/**
//	 * 创建数据库时,表的集合
//	 * 初始的表
//	 *
//	 * @param tables
//	 */
//	public abstract void createTables(List<Class<T>> tables);

	/**
	 * 数据库创建和版本更新时，要更新的表的集合
	 * 升级或降级版本，新增的表，有更新字段的表
	 *
	 * @param tables
	 */
	public abstract void updateTables(List<Class<T>> tables);

	/**
	 * 数据库版本更新时,删除表的集合
	 * 不建议使用
	 */
//	public void deleteTables(List<Class<T>> tables) {
//
//	}

}

