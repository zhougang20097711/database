package ab.umdatabase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ab.umdatabase.model.Product;
import ab.umdatabase.ormdb.MyDao;
import ab.umdatabaselib.dao.Callback;

/**
 * Created by AB051788 on 2017/2/22.
 */
public class TestActivity extends AppCompatActivity implements View.OnClickListener {
	private TextView massage;
	private TextView btn1;
	private TextView btn2;
	private TextView btn3;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		initView();
	}

	private void initView() {
		massage = (TextView) findViewById(R.id.massage);
		btn1 = (TextView) findViewById(R.id.btn1);
		btn2 = (TextView) findViewById(R.id.btn2);
		btn3 = (TextView) findViewById(R.id.btn3);
		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
		btn3.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn1:
				insert(100);
				break;
			case R.id.btn2:
				update();
				break;
			case R.id.btn3:
				test();
				break;
		}

	}

	//测试异步插入100条
	public void insert(int size) {
		List<Product> list = new ArrayList<>();
		Product product;
		for (int i = 0; i < size; i++) {
			product = new Product("AB", "安邦保险", 100.00f);


			list.add(product);
		}
		try {
			MyDao dao = new MyDao(Product.class);
			dao.asyncInsert(list, new Callback() {
				@Override
				public void success(List list, int affect) {
					massage.setText("插入数据" + affect + "条成功");
				}

				@Override
				public void failure(String msg) {
					massage.setText("插入数据失败");
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//测试更新
	public void update() {
		try {
			MyDao dao = new MyDao(Product.class);
			Map<String, String> map = new HashMap<>();
			map.put("product_name", "AB");
			dao.asyncUpdate(map, "product_name", "ab", new Callback() {
				@Override
				public void success(List list, int affect) {
					massage.setText("更新数据" + affect + "条成功");
				}

				@Override
				public void failure(String msg) {
					massage.setText("更新数据失败");
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void test() {
		MyDao dao = null;
		try {
			dao = new MyDao(Product.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 100; i++) {
			if (i % 10 == 0) {
				try {
					dao.insert(new Product("AB", "sync", 109.99f));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (i % 2 == 0) {
				insert(100);
			} else {
				update();
			}
		}
	}

}
