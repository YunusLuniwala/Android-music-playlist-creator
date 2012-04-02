package com.trinity.stooges;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class UserInputActivity extends Activity {
	private Button btn1;
	private EditText playlistname, numsongs;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.usrinptlayout);
		
		btn1 = (Button) findViewById(R.id.button1);
		playlistname = (EditText) findViewById(R.id.playlistnametext);
		numsongs = (EditText) findViewById(R.id.numsongstext);
		btn1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String playlistName = playlistname.getText().toString();
				int numsong = new Integer(numsongs.getText().toString());
				
				Intent results = new Intent();
				results.putExtra("playlistname", playlistName);
				results.putExtra("numsongs", numsong);
				setResult(RESULT_OK, results);
				finish();
			}
		}
				);
	}
}
