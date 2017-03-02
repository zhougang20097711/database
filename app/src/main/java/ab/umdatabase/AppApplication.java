package ab.umdatabase;

import android.app.Application;
import android.content.Context;

/**
 * Created by AB051788 on 2017/2/14.
 */
public class AppApplication extends Application {
	public static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;

	}
}
