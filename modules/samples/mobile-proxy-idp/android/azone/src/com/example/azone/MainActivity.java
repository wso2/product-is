package com.example.azone;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.json.JSONObject;
import org.wso2.mobile.idp.proxy.IDPProxyActivity;
import org.wso2.mobile.idp.proxy.IdentityProxy;
import org.wso2.mobile.idp.proxy.callbacks.AccessTokenCallBack;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends IDPProxyActivity implements AccessTokenCallBack {
	private static String TAG = "MainActivity";
	Button button;
	Context context;
	Menu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch(item.getItemId()){
			case R.id.action_settings :
				Log.v("Menu Clicked", "Menu Setting Clicked");
				init(OauthCostants.CLIENT_ID,OauthCostants.CLIENT_SECRET,this);
				break;
		}

		return super.onMenuItemSelected(featureId, item);
	}
	public void onTokenReceived() {
		try {
	        JSONObject mainObject = new JSONObject(IdentityProxy.getInstance().getToken().getIdToken().substring(27));
			String subject = mainObject.getString("sub");
			int index = subject.indexOf('@');
			if(index>0){
				subject = subject.substring(0, index);
			}
			Log.v("Subject",subject);
			MenuItem item = menu.findItem(R.id.action_settings);
			//item.setVisible(false);
			item.setTitle("Welcome : "+subject);
        } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (ExecutionException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (TimeoutException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}

	void setSSL(){
		InputStream inputStream = context.getResources().openRawResource(R.raw.truststore);
		//ServerUtilities.enableSSL(inputStream,OauthCostants.TRUSTSTORE_PASSWORD);

	}

}
