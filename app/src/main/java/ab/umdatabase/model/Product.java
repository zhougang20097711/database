package ab.umdatabase.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by AB051788 on 2017/2/20.
 */
@DatabaseTable(tableName = "product")
public class Product {
	@DatabaseField(generatedId = true , columnName = "id")
	private int id;

	@DatabaseField(columnName = "product_name")
	private String productName;

//	@DatabaseField(columnName = "Product_dsc")
//	private String ProductDsc;

	@DatabaseField(columnName = "price")
	private float price;

//	@DatabaseField(columnName = "sell")
//	private int sell;
//
		@DatabaseField(columnName = "star")
	private String star;
	public Product() {
	}

	public Product(String productName, String productDsc, float price) {
		this.productName = productName;
//		ProductDsc = productDsc;
		this.price = price;
	}


	public String getStar() {
		return star;
	}

	public void setStar(String star) {
		this.star = star;
	}

//		public int getSell() {
//		return sell;
//	}
//
//	public void setSell(int sell) {
//		this.sell = sell;
//	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

//	public String getProductDsc() {
//		return ProductDsc;
//	}
//
//	public void setProductDsc(String productDsc) {
//		ProductDsc = productDsc;
//	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}



	@Override
	public String toString() {
		return "Product{" +
				"id=" + id +
				", productName='" + productName + '\'' +
				", ProductDsc='" + "ProductDsc" + '\'' +
				", price=" + price +
				'}';
	}
}
