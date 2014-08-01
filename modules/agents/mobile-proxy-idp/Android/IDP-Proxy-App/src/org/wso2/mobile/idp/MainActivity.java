package org.wso2.mobile.idp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.wso2.mobile.idp.filebrowser.FileDialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * This activity will be fired to user to insert his profile image. The idea of inserting a profile image is avoid phishing attacks
 * 
 *
 */
public class MainActivity extends Activity{
	private static final String TAG = "MainActivity";
	public static int imageFileSize = 0;
	private FileInputStream fis;
	private Button btnSave, btnContinue;
	private EditText txtServer, txtPort;
	private ImageView imgProfilePic;
	Context context;
	boolean firstLogin = false;
	private String clientID = null;
	SharedPreferences sharedPreferences; 
	private OauthEndPoints oauthEndPoints = null;
	/* Initialize two buttons, one for go forward to web view and other button for upload profile picture  
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button loginButton = (Button) findViewById(R.id.btnLogin);//login button to go to web view
		
		oauthEndPoints = OauthEndPoints.getInstance();
		btnSave = (Button)findViewById(R.id.btnSave);
		btnContinue = (Button)findViewById(R.id.btnContinue);
		txtServer = (EditText)findViewById(R.id.txtServer);
		txtPort = (EditText)findViewById(R.id.txtPort);
		imgProfilePic = (ImageView)findViewById(R.id.prof_pic);
		context = MainActivity.this;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey("first_launch")) {
				firstLogin = true;
			}
			
			if (extras.containsKey("client_id")) {
				clientID = extras.getString("client_id");
			}

		}
		
		String hostnew = sharedPreferences.getString("edittext_preference_host", null);//read host from configurations
		String portnew = sharedPreferences.getString("edittext_preference_port", null);//read port from configurations 
		
		if(hostnew !=null){
			txtServer.setText(hostnew);
		}
		
		if(portnew != null){
			txtPort.setText(portnew);
		}
		
		if(firstLogin){
			btnContinue.setVisibility(View.GONE);
		}else{
			btnContinue.setVisibility(View.VISIBLE);
		}
		
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {			
				Editor editor = sharedPreferences.edit();
				if(txtServer.getText()!=null && txtPort.getText()!=null && !txtServer.getText().toString().trim().equals("") && !txtPort.getText().toString().trim().equals("")){
					editor.putString("edittext_preference_host", txtServer.getText().toString().trim());
					editor.putString("edittext_preference_port", txtPort.getText().toString().trim());
					editor.commit();
					
					String host = sharedPreferences.getString("edittext_preference_host", "10.100.0.189");//read host from configurations
					String port = sharedPreferences.getString("edittext_preference_port", "9763");//read port from configurations 
					oauthEndPoints.setEndPointURLs(host, port);//set port & host
					Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(context, "Please set server and the port to continue", Toast.LENGTH_LONG).show();
				}
			}

		});
		
		btnContinue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {			
				Intent i = new Intent(MainActivity.this, WebViewActivity.class);
				i.putExtra("client_id", clientID);
				startActivity(i);
				finish();
			}

		});
		
		imgProfilePic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {			
				Intent intent = new Intent(getBaseContext(), FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, "/sdcard");
				intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
				intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "jpg" });
				try {
					startActivityForResult(intent, 23);//launch FileDialog activity to select a image from SD card 
				} catch (NullPointerException e) {
					Log.d(TAG, e.toString());
				}
			}

		});
		
		showImage();
		
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(MainActivity.this, WebViewActivity.class);
				i.putExtra("self_login", "self_login");
				startActivity(i);
				finish();
			}

		});
		Button imageUploadButton = (Button) findViewById(R.id.btnUploadImage);//button to upload profile picture
		imageUploadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(getBaseContext(), FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, "/sdcard");
				intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
				intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "jpg" });
				try {
					startActivityForResult(intent, 23);//launch FileDialog activity to select a image from SD card 
				} catch (NullPointerException e) {
					Log.d(TAG, e.toString());
				}
			}
		});
	}
	
	/**
	 * display profile picture
	 */
	void showImage() {
		FileInputStream readFile;
		try {
			readFile = openFileInput("profile_pic.png");
			byte[] fileInputArray = new byte[MainActivity.imageFileSize];
			readFile.read(fileInputArray);
			readFile.close();
			Bitmap myBitmap =
			                  BitmapFactory.decodeByteArray(fileInputArray, 0,
			                                                fileInputArray.length);

			imgProfilePic.setImageBitmap(myBitmap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			imgProfilePic.setBackgroundResource(R.drawable.profile_pic_blank);
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			imgProfilePic.setBackgroundResource(R.drawable.profile_pic_blank);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	/* launch configuration activity to set hostname and port
	 * (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				Log.d(TAG, "Menu Setting Clicked");
				Intent intent = new Intent(this, Configuration.class);
				startActivity(intent);
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o);

		final int REQUIRED_SIZE = 100;

		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o2);
	}
	
	/* Save captured image from SD card to application private data 
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 23) {
			Log.d(TAG, "Saving...");
			try {
				String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
				File file = new File(filePath);
				Log.v("File Selected", filePath);
				fis = new FileInputStream(file);
				imageFileSize = (int) file.length();
				byte[] fileArray = new byte[imageFileSize];
				fis.read(fileArray);
				FileOutputStream outputStream =
						openFileOutput("profile_pic.png",
						               Context.MODE_PRIVATE);
				outputStream.write(fileArray);
				outputStream.flush();
				outputStream.close();
				Uri uri = Uri.fromFile(file);
				imgProfilePic.setImageBitmap(decodeUri(uri));
				super.onActivityResult(requestCode, resultCode, data);
			} catch (FileNotFoundException e) {
				Log.d(TAG, e.toString());
			} catch (Exception e) {
				Log.d(TAG, e.toString());
			}
		}
	}
	
}
