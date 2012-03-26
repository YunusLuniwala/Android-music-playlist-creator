package com.trinity.stooges;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class testactivity extends ListActivity {
public void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.testactvty);
		
	Cursor playlist_list_cursor = StoogesActivity.databaseHelper.fetchSongs();
	startManagingCursor(playlist_list_cursor);
	String from[] = new String[] { "name", "artist", "songid" };
	int[] to = new int[] { R.id.txt1, R.id.txt2, R.id.txt3 };
	SimpleCursorAdapter playlistAdapter = new SimpleCursorAdapter(this,
			R.layout.tstactvtyrow, playlist_list_cursor, from, to);
	setListAdapter(playlistAdapter);
}
}
