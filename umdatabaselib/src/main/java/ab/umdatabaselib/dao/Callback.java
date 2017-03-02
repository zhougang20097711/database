package ab.umdatabaselib.dao;

import java.util.List;

/**
 * 异步数据库操作回调接口
 * Created by AB051788 on 2017/2/14.
 */
public interface Callback<T> {

	/**
	 * 数据库操作成功
	 * @param list 返回查询的对象集合  若查询的是单个对象集合中只有一个
	 *             若进行的是写入删除更新操作，返回集合为null
	 * @param affect 返回插入删除更新受影响条数
	 */
	void success(List<T> list, int affect);

	/**
	 *数据库操作失败
	 * @param msg 错误信息
	 */
	void failure(String msg);
}
