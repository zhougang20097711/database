package ab.umdatabase.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by AB051788 on 2017/2/20.
 */
@DatabaseTable(tableName = "user")
public class User {
	@DatabaseField(generatedId = true , columnName = "id")
	private int id;

	@DatabaseField(columnName = "name")
	private String name;

	@DatabaseField(columnName = "age")
	private int age;

	@DatabaseField(columnName = "address")
	private String address;
//
//	@DatabaseField(columnName = "hehe")
//	private String hehe;
	public User() {
	}

	public User( String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
//
//	public String getHehe() {
//		return hehe;
//	}
//
//	public void setHehe(String hehe) {
//		this.hehe = hehe;
//	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", name='" + name + '\'' +
				", age=" + age +
				'}';
	}
}
