package ab.umdatabaselib.dao;


import android.os.Handler;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ab.umdatabaselib.helper.OrmDatabaseHelper;

/**
 * 数据操作对OrmDao封装的Dao类，
 * async开头的方法都是异步调用方法
 * Created by AB051788 on 2017/2/14.
 */
public abstract class OrmDaoUtils<T> {
	private static final int INSERT = 1;   //事务 新增
	private static final int UPDATE = 2;   //事务 新增
	private static final int DELETE = 3;   //事务 新增
	private OrmDatabaseHelper mHelper;     //DBHleper
	private Dao mDao;                      //Dao类
	private Handler handler;               //Handler 用于异步回调
	private static ExecutorService mExecutorService;  //线程池

	/**
	 * 构造方法，创建工作
	 *
	 * @param cls 数据库操作的类
	 * @throws SQLException 数据库异常
	 */
	public OrmDaoUtils(Class cls) throws SQLException {
		if (mHelper == null) {
			mHelper = getHelper();
		}
		this.mDao = mHelper.getDao(cls);
		if (mExecutorService == null || mExecutorService.isShutdown() || mExecutorService.isTerminated()) {
			mExecutorService = Executors.newCachedThreadPool();
		}
		Log.i("aaa", Thread.currentThread().getId() + "");
		handler = new Handler();
	}

	/**
	 * 获取DBHelper
	 *
	 * @return DBHelper
	 */
	protected abstract OrmDatabaseHelper getHelper();

	/**
	 * 新增数据
	 *
	 * @param obj
	 * @throws SQLException 数据库异常
	 */
	public int insert(T obj) throws SQLException {
		return mDao.create(obj);
	}

	/**
	 * 批量添加数据
	 *
	 * @param objs
	 * @return
	 * @throws SQLException
	 */
	public int insert(Collection<T> objs) throws SQLException {
		return mDao.create(objs);
	}

	/**
	 * 使用事务批量添加数据
	 *
	 * @param objs
	 * @return
	 * @throws SQLException
	 */
	public boolean insertInTransaction(final List<T> objs) throws SQLException {
		return baseTransaction(INSERT, objs);
	}

	/**
	 * 删除数据
	 *
	 * @param obj
	 * @throws SQLException
	 */
	public int delete(T obj) throws SQLException {
		return mDao.delete(obj);
	}

	/**
	 * 批量删除数据
	 *
	 * @param objs
	 * @return
	 * @throws SQLException
	 */
	public int delete(Collection<T> objs) throws SQLException {
		return mDao.delete(objs);
	}

	/**
	 * 使用事务批量删除事务（实体）
	 *
	 * @param objs
	 * @return
	 * @throws SQLException
	 */
	public boolean deleteInTransaction(final List<T> objs) throws SQLException {
		return baseTransaction(DELETE, objs);
	}

	/**
	 * 通过ID 删除数据
	 *
	 * @param objId
	 * @throws SQLException
	 */
	public int deleteById(Integer objId) throws SQLException {
		return  mDao.deleteById(objId);
	}

	/**
	 * 使用事务批量删除事务（ID）
	 *
	 * @param objIds
	 * @return
	 * @throws SQLException
	 */
	public boolean deleteInTransactionById(final List<Integer> objIds) throws SQLException {
		Boolean b = TransactionManager.callInTransaction(mHelper.getConnectionSource(), new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try {
					for (Integer objId : objIds) {
						mDao.deleteById(objId);
					}
				} catch (SQLException e) {
					return false;
				}
				return true;
			}
		});
		return b;
	}

	/**
	 * 修改数据
	 *
	 * @param obj
	 * @return
	 * @throws SQLException
	 */
	public int update(T obj) throws SQLException {
		return mDao.update(obj);
	}

	/**
	 * 条件修改数据
	 *
	 * @param whereMap    修改条件
	 * @param columnName  修改字段名
	 * @param columnValue 修改内容
	 * @return
	 * @throws SQLException
	 */
	public int update(Map<String, Object> whereMap, String columnName, Object columnValue) throws SQLException {
		Map<String, Object> updateValue = new HashMap<String, Object>();
		updateValue.put(columnName, columnValue);
		return this.update(whereMap, updateValue);

	}

	/**
	 * 条件修改数据
	 *
	 * @param whereMap    修改条件
	 * @param updateValue 修改内容
	 * @return
	 * @throws SQLException
	 */
	public int update(Map<String, Object> whereMap, Map<String, Object> updateValue) throws SQLException {
		UpdateBuilder updateBuilder = mDao.updateBuilder();
		if (updateValue != null && !updateValue.isEmpty()) {
			Set<String> valueSet = updateValue.keySet();
			for (String valueKey : valueSet) {
				updateBuilder.updateColumnValue(valueKey, updateValue.get(valueKey));
			}
		} else {
			return -1;
		}

		if (whereMap != null && !whereMap.isEmpty()) {
			Where<T, Long> where = updateBuilder.where();
			Set<String> whereKeySet = whereMap.keySet();
			List<String> keys = new ArrayList<String>();
			keys.addAll(whereKeySet);
			for (int i = 0; i < keys.size(); i++) {
				if (i == 0) {
					where.eq(keys.get(i), whereMap.get(keys.get(i)));
				} else {
					where.and().eq(keys.get(i), whereMap.get(keys.get(i)));
				}
			}
		}
		return mDao.update(updateBuilder.prepare());
	}

	/**
	 * 在事务中批量修改数据
	 *
	 * @param objs
	 * @return
	 * @throws SQLException
	 */
	public boolean updateInTransaction(final List<T> objs) throws SQLException {
		return baseTransaction(UPDATE, objs);
	}

	/**
	 * 创建或修改数据
	 * <br/> 如果没有数据就添加，如果数据存在就修改
	 *
	 * @param obj
	 * @return
	 * @throws SQLException
	 */
	public Dao.CreateOrUpdateStatus createOrUpdate(T obj) throws SQLException {
		return mDao.createOrUpdate(obj);
	}

	/**
	 * 批量创建或修改数据
	 * <br/> 如果没有数据就添加，如果数据存在就修改
	 *
	 * @param objs
	 * @return
	 * @throws SQLException
	 */
	public Dao.CreateOrUpdateStatus createOrUpdate(Collection<T> objs) throws SQLException {
		return mDao.createOrUpdate(objs);
	}


	/**
	 * 查询，通过ID
	 *
	 * @param objId 查询数据的ID
	 * @return 返回查询 Bean 数据，如果未查询到则返回 null
	 * @throws SQLException
	 */
	public T queryById(Integer objId) throws SQLException {
		return (T) mDao.queryForId(objId);
	}


	/**
	 * 查询指定 Bean 所有数据
	 *
	 * @return 返回查询 Bean 数据，如果未查询到则返回 null
	 * @throws SQLException
	 */
	public List<T> queryAll() throws SQLException {
		return mDao.queryForAll();
	}

	/**
	 * 通过QueryBuilder查询
	 *
	 * @param builder 查询条件
	 * @return
	 * @throws SQLException
	 */
	public List<T> query(QueryBuilder builder) throws SQLException {
		PreparedQuery<T> preparedQuery = builder.prepare();
		return mDao.query(preparedQuery);
	}

	/**
	 * 查询，条件查询  构建QueryBuilder
	 * <br/> 说明： 该方法要和 {@link OrmDaoUtils#query(QueryBuilder)} 方法组合使用
	 * <br/> 在查询操作中，查询的条件最后都要 通过 {@link OrmDaoUtils#query(QueryBuilder)} 方法执行查询
	 * <br/> 此方法 对应SQL ：SELECT * FROM `t_person` WHERE `id` = 2
	 *
	 * @param whereMap
	 * @return whereMap
	 * @throws SQLException
	 */
	public QueryBuilder queryBuilder(Map<String, Object> whereMap) throws SQLException {
		QueryBuilder<T, Integer> queryBuilder = mDao.queryBuilder();
		if (whereMap != null && !whereMap.isEmpty()) {
			Where<T, Integer> wheres = queryBuilder.where();
			Set<String> keys = whereMap.keySet();
			ArrayList<String> keyss = new ArrayList<String>();
			keyss.addAll(keys);
			for (int i = 0; i < keyss.size(); i++) {
				if (i == 0) {
					wheres.eq(keyss.get(i), whereMap.get(keyss.get(i)));
				} else {
					wheres.and().eq(keyss.get(i), whereMap.get(keyss.get(i)));
				}
			}
		}
		return queryBuilder;
	}

	/**
	 * 升序或降序  构建QueryBuilder
	 *
	 * @param orderColumn 升序、降序字段
	 * @param isASC       true 升序 ，false 降序
	 * @return
	 * @throws SQLException
	 */
	public QueryBuilder queryOrderBy(String orderColumn, boolean isASC) throws SQLException {
		return queryOrderBy(null, orderColumn, isASC);
	}


	/**
	 * 带条件 升序或降序查询  构建QueryBuilder
	 *
	 * @param queryBuilder 查询条件
	 * @param orderColumn  升序、降序字段
	 * @param isASC        true 升序 ，false 降序
	 * @return
	 * @throws SQLException
	 */
	public QueryBuilder queryOrderBy(QueryBuilder queryBuilder, String orderColumn, boolean isASC) throws SQLException {
		if (queryBuilder == null) {
			queryBuilder = mDao.queryBuilder();
		}
		if (orderColumn != null) {
			queryBuilder.orderBy(orderColumn, isASC);
		}
		return queryBuilder;
	}

	/**
	 * 事务批量处理
	 *
	 * @param type 数据库操作的类型
	 * @param objs 操作的对象集合
	 * @return
	 * @throws SQLException
	 */
	private boolean baseTransaction(final int type, final List<T> objs) throws SQLException {
		Boolean b = TransactionManager.callInTransaction(mHelper.getConnectionSource(), new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try {

					switch (type) {
						case INSERT:
						case UPDATE:
							for (T obj : objs) {
								mDao.createOrUpdate(obj);
							}
							break;

						case DELETE:
							for (T obj : objs) {
								mDao.delete(obj);
							}
							break;
					}
				} catch (SQLException e) {
					return false;
				}
				return true;
			}
		});
		return b;
	}

	//.................异步操作...................

	/**
	 * 数据库操作成功
	 *
	 * @param list     影响的集合
	 * @param callback 回调方法
	 */
	private void handleSuccess(final List<T> list, final int affect, final Callback callback) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (callback != null) {
					callback.success(list,affect);
				}
			}
		});
	}

	/**
	 * 数据库操作失败
	 *
	 * @param msg      失败信息
	 * @param callback 回调方法
	 */
	private void handleFailure(String msg, final Callback callback) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (callback != null) {
					callback.failure("失败");
				}
			}
		});
	}

	/**
	 * 新增数据
	 *
	 * @param obj
	 */
	public void asyncInsert(final T obj, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					int affect = insert(obj);
					if (affect == 1) {
						handleSuccess(null,affect, callback);
					} else {
						handleFailure("数据库插入失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}

	/**
	 * 批量添加数据
	 *
	 * @param objs
	 */
	public void asyncInsert(final Collection<T> objs, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					int affect = insert(objs);
					if (affect >= 0) {
						handleSuccess(null,affect, callback);
					} else {
						handleFailure("数据操作失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}

	/**
	 * 使用事务批量添加数据
	 *
	 * @param objs
	 */
	public void asyncInsertInTransaction(final List<T> objs, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					boolean affect = insertInTransaction(objs);
					if (affect) {
						handleSuccess(null,objs.size(), callback);
					} else {
						handleFailure("插入失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}

	/**
	 * 删除数据
	 *
	 * @param obj
	 */
	public void asyncDelete(final T obj, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					int affect = delete(obj);
					if (affect == 1) {
						handleSuccess(null,affect, callback);
					} else {
						handleFailure("数据操作失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}

	/**
	 * 批量删除数据
	 *
	 * @param objs
	 */
	public void asyncDelete(final Collection<T> objs, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					int affect = delete(objs);
					if (affect >= 0) {
						handleSuccess(null,affect, callback);
					} else {
						handleFailure("数据操作失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});

	}

	/**
	 * 使用事务批量删除事务（实体）
	 *
	 * @param objs
	 */
	public void asyncDeleteInTransaction(final List<T> objs, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					boolean affect = deleteInTransaction(objs);
					if (affect) {
						handleSuccess(null,objs.size(), callback);
					} else {
						handleFailure("数据操作失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}

	/**
	 * 通过ID 删除数据
	 *
	 * @param objId
	 */
	public void asyncDeleteById(final Integer objId, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					int affect = deleteById(objId);
					handleSuccess(null, affect,callback);
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}

	/**
	 * 使用事务批量删除事务（ID）
	 *
	 * @param objIds
	 */
	public void asyncDeleteInTransactionById(final List<Integer> objIds, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					boolean affect = deleteInTransactionById(objIds);
					if (affect) {
						handleSuccess(null,objIds.size(), callback);
					} else {
						handleFailure("数据操作失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}

	/**
	 * 修改数据
	 *
	 * @param obj
	 */
	public void asyncUpdate(final T obj, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					int affect = update(obj);
					if (affect == 1) {
						handleSuccess(null,affect, callback);
					} else {
						handleFailure("数据操作失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}

	/**
	 * 条件修改数据
	 *
	 * @param whereMap    修改条件
	 * @param columnName  修改字段名
	 * @param columnValue 修改内容
	 */
	public void asyncUpdate(Map<String, Object> whereMap, String columnName, Object columnValue, final Callback callback) {
		Map<String, Object> updateValue = new HashMap<String, Object>();
		updateValue.put(columnName, columnValue);
		this.asyncUpdate(whereMap, updateValue, callback);
	}

	/**
	 * 条件修改数据
	 *
	 * @param whereMap    修改条件
	 * @param updateValue 修改内容
	 */
	public void asyncUpdate(final Map<String, Object> whereMap, final Map<String, Object> updateValue, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					int affect = update(whereMap, updateValue);
					if (affect >= 0) {
						handleSuccess(null,affect, callback);
					} else {
						handleFailure("数据操作失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}

	/**
	 * 在事务中批量修改数据
	 *
	 * @param objs
	 */
	public void asyncUpdateInTransaction(final List<T> objs, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					boolean affect = updateInTransaction(objs);
					if (affect) {
						handleSuccess(null,objs.size(), callback);
					} else {
						handleFailure("数据操作失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});

	}

	/**
	 * 创建或修改数据
	 * <br/> 如果没有数据就添加，如果数据存在就修改
	 *
	 * @param obj
	 */
	public void asyncCreateOrUpdate(final T obj, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					Dao.CreateOrUpdateStatus affect = createOrUpdate(obj);
					if (affect.getNumLinesChanged() >= 0) {
						handleSuccess(null,affect.getNumLinesChanged(), callback);
					} else {
						handleFailure("数据操作失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}

	/**
	 * 批量创建或修改数据
	 * <br/> 如果没有数据就添加，如果数据存在就修改
	 *
	 * @param objs
	 */
	public void asyncCreateOrUpdate(final Collection<T> objs, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					Dao.CreateOrUpdateStatus affect = createOrUpdate(objs);
					if (affect.getNumLinesChanged() >= 0) {
						handleSuccess(null,affect.getNumLinesChanged(), callback);
					} else {
						handleFailure("数据操作失败", callback);
					}
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});

	}

	/**
	 * 查询，通过ID
	 *
	 * @param objId 查询数据的ID
	 */
	public void asyncQueryById(final Integer objId, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					T t = queryById(objId);
					List<T> list = new ArrayList<T>();
					if(t!=null){
						list.add(t);
					}
					handleSuccess(list,list.size(),callback);
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});

	}

	/**
	 * 查询指定 Bean 所有数据
	 */
	public void asyncQueryAll(final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					List<T> list = queryAll();
					int affect = 0;
					if(list!=null){
						affect =list.size();
					}
					handleSuccess(list,affect, callback);
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}

	/**
	 * 通过QueryBuilder查询
	 *
	 * @param builder 查询条件
	 */
	public void asyncQuery(final QueryBuilder builder, final Callback callback) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				Log.i("async", Thread.currentThread().getId() + "");
				try {
					List<T> list = query(builder);
					int affect = 0;
					if(list!=null){
						affect =list.size();
					}
					handleSuccess(list, affect,callback);
				} catch (SQLException e) {
					Log.e("SQL", e.toString());
					handleFailure("SQLException:" + e.toString(), callback);
				}
			}
		});
	}
}
