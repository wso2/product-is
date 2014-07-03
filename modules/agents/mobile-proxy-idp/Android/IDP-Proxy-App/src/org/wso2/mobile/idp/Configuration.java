package org.wso2.mobile.idp;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

/**
 * 
 * Once you go to setting top left corner, this activity will be fired
 *
 */

public class Configuration extends Activity {
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);
		getFragmentManager().beginTransaction().add(R.id.prefcontent, new PrefsFragment()).commit();
	}
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.configuration, menu);
		return true;
	}
}
