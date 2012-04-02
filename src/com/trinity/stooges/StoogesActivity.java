package com.trinity.stooges;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class StoogesActivity extends ListActivity {
	public static MusicDbAdapter databaseHelper;
	private Cursor playlist_list_cursor, songlistCursor;
	private ContentResolver mContentResolver;
	private Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	private EchoNest echonestHelper;
	int numTracksInDevice;
	public static final int newPlaylistRequestCode = 7;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		databaseHelper = new MusicDbAdapter(this);
		databaseHelper.open();
		mContentResolver = getContentResolver();

		try {
			echonestHelper = new EchoNest();
		} catch (Exception e) {
		}
		
		// display list of playlists in activity
		playlist_list_cursor = databaseHelper.fetchAllPlaylists();
		startManagingCursor(playlist_list_cursor);
		String from[] = new String[] { MusicDbAdapter.playlist_table_namecol };
		int[] to = new int[] { R.id.list_row };
		SimpleCursorAdapter playlistAdapter = new SimpleCursorAdapter(this,
				R.layout.listrow, playlist_list_cursor, from, to);
		setListAdapter(playlistAdapter);		
		
		// check if number of tracks has changed. if so, update database
		int numTracksInDb;// = databaseHelper.getNumTracks();
		Cursor testc = databaseHelper.fetchSongs();
		numTracksInDb = testc.getCount();

		songlistCursor = mContentResolver.query(musicUri, new String[] {
				android.provider.MediaStore.Audio.AudioColumns.ARTIST,
				android.provider.MediaStore.MediaColumns.TITLE },
				"IS_MUSIC<>0", null, null);
		startManagingCursor(songlistCursor);
		Toast.makeText(this, songlistCursor.getCount() + "", Toast.LENGTH_SHORT)
				.show();
		numTracksInDevice = songlistCursor.getCount();
		if (numTracksInDevice != numTracksInDb) {
			// number of tracks changed. delete all current database info and
			// repopulate
			// TODO: change this to a better implementation where only new
			// tracks are added
			databaseHelper.deleteAllTrackInfo();
			SongEntity songinfo = new SongEntity();
			int songCount = 0, counter;
			songlistCursor.moveToFirst();
			while (!songlistCursor.isAfterLast()) {
				String artist = songlistCursor
						.getString(songlistCursor
								.getColumnIndex(android.provider.MediaStore.Audio.AudioColumns.ARTIST));
				String title = songlistCursor
						.getString(songlistCursor
								.getColumnIndex(android.provider.MediaStore.MediaColumns.TITLE));

				try {
					// if((artist != null) &&
					// (!artist.toLowerCase().contains("unknown")))
					songinfo = echonestHelper.get_song_details(artist, title);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// passing in artist and title retrieved from phone's database
				// in case echonest doesn't find a match
				databaseHelper.addTrackInfo(songinfo, artist, title);
				songlistCursor.moveToNext();
				songCount++;
				Toast.makeText(this, songCount + "", Toast.LENGTH_SHORT).show();
				try {
					Thread.sleep(2200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}	
		
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainactvty_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.check_actvty_menu:
			Toast.makeText(this, numTracksInDevice + "", Toast.LENGTH_SHORT)
					.show();
			Intent intent = new Intent(this, testactivity.class);
			startActivity(intent);
			return true;

		case R.id.add_playlist_menu:
			Intent intent1 = new Intent(this, UserInputActivity.class);
			startActivityForResult(intent1, newPlaylistRequestCode);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// databaseHelper.close();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		switch(requestCode)
		{
		case newPlaylistRequestCode:
		{
			String playlistname = data.getStringExtra("playlistname");
			int numsongs = data.getIntExtra("numsongs", 1);
			SongEntity e1 = new SongEntity();

			try {
				e1 = echonestHelper.get_similar_song(playlistname);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			KNN k = new KNN();

			//SongEntity songlist[] = databaseHelper.fetchAllSongs();
			ArrayList<Integer> trackidarraylist = k.closestNeighbour(e1,
					databaseHelper.fetchAllSongs(), numsongs);			
			int[] trackIds = new int[trackidarraylist.size()];
			
			//createPlaylist accepts an array of integers, so need to convert
			for(int counter = 0; counter<trackIds.length; counter++ )
			{ trackIds[counter] = trackidarraylist.get(counter).intValue(); }
			trackidarraylist = null;
			
			numsongs = trackIds.length;
			String songListTableName = playlistname + "_tracklist";
			Playlist newPlaylist = new Playlist(playlistname, numsongs, songListTableName);
			databaseHelper.createPlaylist(newPlaylist, trackIds);
		}
		break;
		
		default:;
		}

	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		//id somehow automatically refers to the database id for that item. Atleast it seems to be that way.
		Intent displayPlaylistIntent = new Intent(this, playlistActivity.class);
		displayPlaylistIntent.putExtra("playlist_id", id);
		startActivity(displayPlaylistIntent);
	}
}