package com.trinity.stooges;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class playlistActivity extends ListActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist_activity_layout);
		
		Intent activityIntent = getIntent();
		int playlistId = (int) activityIntent.getLongExtra("playlist_id", -1);
		
		if(playlistId == -1)
		{
			Toast.makeText(getApplicationContext(), "Error: playlist not found", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		Cursor trackListCursor = StoogesActivity.databaseHelper.getTracksInPlaylist(playlistId);
		startManagingCursor(trackListCursor);
		int numtracks = trackListCursor.getCount();
		//Toast.makeText(getApplicationContext(), , duration)
		String[] trackTitleList = new String[numtracks];
		trackListCursor.moveToFirst();
		SongEntity track;
		for(int counter = 0; counter<numtracks; counter++)
		{
			int trackId = trackListCursor.getInt(trackListCursor.getColumnIndex("trackid"));			
			trackTitleList[counter] = StoogesActivity.databaseHelper.getTrackTitle(trackId);
			trackListCursor.moveToNext();
		}
		setListAdapter(new ArrayAdapter<String>(this, R.layout.tracklistrow, trackTitleList));
	}
	
}
