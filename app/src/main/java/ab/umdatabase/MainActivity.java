package ab.umdatabase;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ab.umdatabase.model.User;
import ab.umdatabase.ormdb.MyDao;
import ab.umdatabaselib.dao.Callback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	private TextView insert01;
	private TextView insert02;
	private TextView insert03;
	private TextView delete01;
	private TextView delete02;
	private TextView delete03;
	private TextView delete04;
	private TextView delete05;
	private TextView update01;
	private TextView update02;
	private TextView update03;
	private TextView update04;
	private TextView update05;
	private TextView update06;
	private TextView query01;
	private TextView query02;
	private TextView query03;
	private TextView goTest;
	private TextView massage;
	private RadioGroup radiogruop;
	private PackageManager packageManager;
	private PackageInfo packageInfo;
	private boolean isSync = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	private void init() {
		insert01 = (TextView) findViewById(R.id.insert01);
		insert02 = (TextView) findViewById(R.id.insert02);
		insert03 = (TextView) findViewById(R.id.insert03);
		delete01 = (TextView) findViewById(R.id.delete01);
		delete02 = (TextView) findViewById(R.id.delete02);
		delete03 = (TextView) findViewById(R.id.delete03);
		delete04 = (TextView) findViewById(R.id.delete04);
		delete05 = (TextView) findViewById(R.id.delete05);
		update01 = (TextView) findViewById(R.id.update01);
		update02 = (TextView) findViewById(R.id.update02);
		update03 = (TextView) findViewById(R.id.update03);
		update04 = (TextView) findViewById(R.id.update04);
		update05 = (TextView) findViewById(R.id.update05);
		update06 = (TextView) findViewById(R.id.update06);
		query01 = (TextView) findViewById(R.id.query01);
		query02 = (TextView) findViewById(R.id.query02);
		query03 = (TextView) findViewById(R.id.query03);
		goTest = (TextView) findViewById(R.id.goTest);
		massage = (TextView) findViewById(R.id.massage);
		radiogruop = (RadioGroup) findViewById(R.id.radiogruop);
		insert01.setOnClickListener(this);
		insert02.setOnClickListener(this);
		insert03.setOnClickListener(this);
		delete01.setOnClickListener(this);
		delete02.setOnClickListener(this);
		delete03.setOnClickListener(this);
		delete04.setOnClickListener(this);
		delete05.setOnClickListener(this);
		update01.setOnClickListener(this);
		update02.setOnClickListener(this);
		update03.setOnClickListener(this);
		update04.setOnClickListener(this);
		update05.setOnClickListener(this);
		update06.setOnClickListener(this);
		query01.setOnClickListener(this);
		query02.setOnClickListener(this);
		query03.setOnClickListener(this);
		goTest.setOnClickListener(this);
		packageManager = getPackageManager();

		try {

			packageInfo = packageManager.getPackageInfo(getPackageName(), 0);

		} catch (PackageManager.NameNotFoundException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}
		massage.setText("versionName:" + packageInfo.versionName+"\n"+"versioncode"+packageInfo.versionCode);

		radiogruop.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.sync){
					isSync = true;
					Log.i("aaa","同步");
				}else{
					isSync = false;
					Log.i("aaa","异步");
				}

			}
		});
	}

	public void insert() {
		User user = new User("李四", 25);
		try {
			MyDao dao = new MyDao(User.class);
			if(isSync){
				int affect = dao.insert(user);
				if (affect == 1) {
					massage.setText("插入一条数据:" + user.toString());
				}
			}else{
				dao.asyncInsert(user, new Callback() {
					@Override
					public void success(List list,int affect) {
						massage.setText("插入一条数据成功");
					}

					@Override
					public void failure(String msg) {
						massage.setText("插入一条数据失败");
					}
				});
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertList() {
		List<User> list = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			list.add(new User("张三", 20));
		}
		try {
			MyDao dao = new MyDao(User.class);
			if(isSync){
				int affect = dao.insert(list);
				if (affect >= 0) {
					massage.setText("插入数据:" + affect + "条");
				}
			}else{
				dao.asyncInsert(list, new Callback() {
					@Override
					public void success(List list,int affect) {
						massage.setText("插入数据成功");
					}

					@Override
					public void failure(String msg) {

					}
				});
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteList(){
		try {
			MyDao dao = new MyDao(User.class);
			Map<String,String> map = new HashMap<>();
			map.put("name","张三");
			QueryBuilder queryBuilder = dao.queryBuilder(map);
			List<User> list = dao.query(queryBuilder);
			if(isSync){
				int affect = dao.delete(list);
				massage.setText("删除"+affect+"条数据");
			}else{
				dao.asyncDelete(list, new Callback() {
					@Override
					public void success(List list,int affect) {
						massage.setText("删除"+list.size()+"条数据");
					}

					@Override
					public void failure(String msg) {

					}
				});

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void update(){
		Map<String,String> whereMap = new HashMap<>();
		whereMap.put("name","张三");
		Map<String,Integer> updateMap = new HashMap<>();
		updateMap.put("age",10);
		try {
			MyDao dao = new MyDao(User.class);
			if(isSync){
			int affect = dao.update(whereMap,updateMap);
				massage.setText("更新"+affect+"条数据");
			}else{
				dao.asyncUpdate(whereMap, updateMap, new Callback() {
					@Override
					public void success(List list,int affect) {
						massage.setText("成功");
					}

					@Override
					public void failure(String msg) {

					}
				});
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void query() {
		try {
			MyDao dao = new MyDao(User.class);
			if(isSync){
				List<User> list = dao.queryAll();
				massage.setText(list.toString());
			}else{
				dao.asyncQueryAll(new Callback() {
					@Override
					public void success(List list,int affect) {
						massage.setText(list.toString());
					}

					@Override
					public void failure(String msg) {

					}
				});
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.insert01:
				insert();
				break;
			case R.id.insert02:
				insertList();
				break;
			case R.id.insert03:

				break;
			case R.id.delete01:

				break;
			case R.id.delete02:
				deleteList();
				break;
			case R.id.delete03:

				break;
			case R.id.delete04:

				break;
			case R.id.delete05:

				break;
			case R.id.update01:

				break;
			case R.id.update02:

				break;
			case R.id.update03:
				update();
				break;
			case R.id.update04:

				break;
			case R.id.update05:

				break;
			case R.id.update06:

				break;
			case R.id.query01:

				break;
			case R.id.query02:
				query();
				break;
			case R.id.query03:

				break;
			case R.id.goTest:
startActivity(new Intent(MainActivity.this,TestActivity.class));
				break;


		}
	}
}
