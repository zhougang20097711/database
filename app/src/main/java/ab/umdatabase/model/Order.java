package ab.umdatabase.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by AB051788 on 2017/2/20.
 */
@DatabaseTable(tableName = "order")
public class Order {
	@DatabaseField(generatedId = true , columnName = "id")
	private int id;
	@DatabaseField(columnName = "order_sn")
	private String orderSn;

	public Order() {
	}

	public Order(int id, String orderSn) {
		this.id = id;
		this.orderSn = orderSn;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOrderSn() {
		return orderSn;
	}

	public void setOrderSn(String orderSn) {
		this.orderSn = orderSn;
	}
}
