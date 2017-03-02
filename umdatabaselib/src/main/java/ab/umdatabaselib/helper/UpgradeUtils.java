package ab.umdatabaselib.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * 数据库版本更新表结构的更换和数据迁移
 * 支持新增列，删除列，修改列名（等同于新增），不支持修改原来列的数据类型、
 *
 * 数据表结构的升级和数据迁移的大概思路：
 * 重命名旧表->创建新表->获取新旧表字段->获取新旧表共同的字段
 * ->将共同字段数据从旧表复制到新表->删除旧表
 * Created by AB051788 on 2017/2/27.
 */
public class UpgradeUtils {

	/**
	 * 表结构的升级或者降级
	 * 支持新增列，删除列，修改列名（等同于新增），不支持修改原来列的数据类型
	 *
	 * 数据表结构的升级和数据迁移的大概思路：
	 * 重命名旧表->创建新表->获取新旧表字段->获取新旧表共同的字段
	 * ->将共同字段数据从旧表复制到新表->删除旧表
	 * @param db SQLiteDatabase
	 * @param connectionSource connectionSource
	 * @param clazz  表对应的类
	 */
	public static <T> void upgradeTable(SQLiteDatabase db, ConnectionSource connectionSource, Class<T> clazz) {
		String tableName = getTableName(clazz);

		db.beginTransaction();
		try {

			//重命名旧表
			String tempTableName = tableName + "_temp";
			String sql = "ALTER TABLE " + tableName + " RENAME TO " + tempTableName;
			db.execSQL(sql);

			//创建新表
			try {
				sql = TableUtils.getCreateTableStatements(connectionSource, clazz).get(0);
				db.execSQL(sql);
			} catch (Exception e) {
				e.printStackTrace();
				TableUtils.createTable(connectionSource, clazz);
			}

			//获取新旧表共同字段
			String columns = getCommonCol(getColumnName(db, tempTableName), getColumnName(db, tableName));
			Log.i("db",columns);
			//把数据从旧表复制到新表
			if (columns != null) {
				sql = "INSERT INTO " + tableName +
						" (" + columns + ") " +
						" SELECT " + columns + " FROM " + tempTableName;
				db.execSQL(sql);
			}
			//删除旧表
			sql = "DROP TABLE IF EXISTS " + tempTableName;
			db.execSQL(sql);

			db.setTransactionSuccessful();
		} catch (Exception e) {
//			e.printStackTrace();
			//表更新出现异常，就直接删除表后再创建新表
			try {
				TableUtils.dropTable(connectionSource, clazz, true);
				TableUtils.createTable(connectionSource, clazz);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * 获取类对应的表名
	 * @param clz 类
	 * @return tableNmae
	 */
	public static <T> String getTableName(Class<T> clz) {
		DatabaseTable databaseTable = clz.getAnnotation(DatabaseTable.class);
		String name;
		if (databaseTable != null && databaseTable.tableName() != null && databaseTable.tableName().length() > 0) {
			name = databaseTable.tableName();
		} else {
			name = clz.getSimpleName().toLowerCase();
		}
		return name;
	}

	/**
	 * 获取表的字段名集合
	 * @param db SQLiteDatabase
	 * @param tableName 表名
	 * @return 字段名集合
	 */
	public static String[] getColumnName(SQLiteDatabase db, String tableName) {
		String[] columnNames = null;
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
			if (cursor != null) {
				int columnIndex = cursor.getColumnIndex("name");
				if (columnIndex == -1) {
					return null;
				}

				int index = 0;
				columnNames = new String[cursor.getCount()];
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					columnNames[index] = cursor.getString(columnIndex);
					index++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return columnNames;
	}

	/**
	 * 获取新旧表共同字段
	 * @param old 旧表字段集合
	 * @param news  新表字段集合
	 * @return 共同字段
	 */
	public static String getCommonCol(String[] old, String[] news) {
		if (old == null || old.length == 0 || news == null || news.length == 0) {
			return null;
		}
		int countOld = old.length;
		int countNews = news.length;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < countOld; i++) {
			for (int j = 0; j < countNews; j++) {
				//只有是新旧表字段名相同的就存下来
				if (old[i].equals(news[j])) {
					buffer.append(old[i] + ',');
				}
			}
		}
		if (buffer.length() == 0) {
			return null;
		}
		return buffer.substring(0, buffer.length() - 1).toString();
	}

}
