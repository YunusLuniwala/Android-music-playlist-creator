package com.trinity.stooges;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MusicDbAdapter {
	private class column {
		public String name;
		public String type;

		public column(String nm, String typ) {
			name = nm;
			type = typ;
		}
	}

	private static final String music_data_database = "music_data";
	private static final int music_data_database_version = 1;
	public static final String playlist_table = "playlists";
	public static final String playlist_table_namecol = "name";
	public static final String tracks_table = "tracks";
	public static final String playlist_table_create_string = "create table "
			+ playlist_table
			+ " (_id integer primary key autoincrement, name text not null, numsongs integer not null, songlisttablename text);";
	public static final String tracks_table_create_string = "create table "
			+ tracks_table
			+ " (_id integer primary key autoincrement, name text not null, artist text, energy float, tempo float, danceability float, duration float, songid text not null);";
	public static final String app_table = "appdata";
	public static final String app_table_create_string = "create table " + app_table + " (numtracks integer);";
	public static final String app_table_init_string = "insert into " + app_table + " values (0);";
	private MusicDbHelper musicDbHelper;
	private Context mContext;
	private SQLiteDatabase musicDatabase;
	

	public MusicDbAdapter(Context context) {
		mContext = context;
	}

	private static class MusicDbHelper extends SQLiteOpenHelper {
		public MusicDbHelper(Context context) {
			super(context, music_data_database, null,
					music_data_database_version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO create tables here
			// create: playlist table, tracks table
			try
			{
			db.beginTransaction();			
			db.execSQL(playlist_table_create_string);
			db.execSQL(tracks_table_create_string);
			db.execSQL(app_table_create_string);
			db.execSQL(app_table_init_string);
			db.setTransactionSuccessful();
			}
			finally
			{
			db.endTransaction();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO upgrade database to newer version
		}
	}

	public void open() {
		musicDbHelper = new MusicDbHelper(mContext);
		musicDatabase = musicDbHelper.getWritableDatabase();
	}
	
	public void openReadOnly() {
		musicDbHelper = new MusicDbHelper(mContext);
		musicDatabase = musicDbHelper.getReadableDatabase();
	}

	public void close() {
		musicDbHelper.close();
	}

	public Cursor fetchAllPlaylists() {
		return musicDatabase.query(playlist_table, new String[] { "_id",
				playlist_table_namecol }, null, null, null, null, null);
	}

	public Cursor fetchSongs()
	{
		return musicDatabase.query(tracks_table, new String[] {
				"_id", "name", "artist", "songid" }, null, null, null, null,
				null);
	}
	
	public SongEntity[] fetchAllSongs() {
		Cursor songCursor = musicDatabase.query(tracks_table, new String[] {
				"_id", "name", "artist", "energy", "tempo", "danceability",
				"duration", "songid" }, null, null, null, null,
				null);
		
		ArrayList<SongEntity> listOfSongs = new ArrayList<SongEntity>();
		
		songCursor.moveToFirst();
		SongEntity song;
		
		while(! songCursor.isAfterLast())
		{
			song = new SongEntity();
			song.set_id(songCursor.getInt(songCursor.getColumnIndex("_id")));
			song.set_title(songCursor.getString(songCursor.getColumnIndex("name")));
			song.set_artist(songCursor.getString(songCursor.getColumnIndex("artist")));
			song.set_energy(songCursor.getFloat(songCursor.getColumnIndex("energy")));
			song.set_danceability(songCursor.getFloat(songCursor.getColumnIndex("danceability")));
			song.set_duration(songCursor.getFloat(songCursor.getColumnIndex("duration")));
			song.set_tempo(songCursor.getFloat(songCursor.getColumnIndex("tempo")));
			song.set_echonest_id(songCursor.getString(songCursor.getColumnIndex("songid")));
			listOfSongs.add(song);
			songCursor.moveToNext();
		}
		
		return (SongEntity[]) listOfSongs.toArray(new SongEntity[0]);
	}

	public SongEntity getSong(String echoNestId)
	{
		SongEntity song = new SongEntity();
		Cursor songCursor = musicDatabase.query(tracks_table, new String[] {
				"name", "artist", "energy", "tempo", "danceability",
				"duration", "songid" }, "songid="+echoNestId, null, null, null,
				null);
			
		songCursor.moveToFirst();
		song.set_title(songCursor.getString(songCursor.getColumnIndex("name")));
		song.set_artist(songCursor.getString(songCursor.getColumnIndex("artist")));
		song.set_energy(songCursor.getFloat(songCursor.getColumnIndex("energy")));
		song.set_danceability(songCursor.getFloat(songCursor.getColumnIndex("danceability")));
		song.set_duration(songCursor.getFloat(songCursor.getColumnIndex("duration")));
		song.set_tempo(songCursor.getFloat(songCursor.getColumnIndex("tempo")));
		song.set_echonest_id(songCursor.getString(songCursor.getColumnIndex("songid")));
		return song;
	}
	
	public int getNumTracks()
	{
		Cursor appdataCursor = musicDatabase.query(app_table, null, null, null, null, null, null);
		appdataCursor.moveToFirst();
		return appdataCursor.getInt(appdataCursor.getColumnIndex("numtracks"));
	}
	
	public void setNumTracks(int num)
	{
		ContentValues cv = new ContentValues();
		cv.put("numtracks", num);
		try
		{
		musicDatabase.beginTransaction();		
		musicDatabase.insert(app_table, null, cv);
		musicDatabase.setTransactionSuccessful();		
		}
		finally
		{ musicDatabase.endTransaction(); }
	}
	
	public void deleteAllTrackInfo()
	{
		try
		{
		musicDatabase.beginTransaction();
		musicDatabase.delete(tracks_table, null, null);
		musicDatabase.setTransactionSuccessful();		
		}
		finally
		{ musicDatabase.endTransaction(); }		
	}
	
	public void addTrackInfo(SongEntity song)
	{
		/*if(song == null) //TODO: change so that boolean is returned or exception is raised
		{ return; }*/
		ContentValues cv = new ContentValues();
		cv.put("songid", song.get_echonest_id());
		cv.put("artist", song.get_artist());
		cv.put("name", song.get_title());
		cv.put("energy", song.get_energy());
		cv.put("tempo", song.get_tempo());
		cv.put("danceability", song.get_danceability());
		cv.put("duration", song.get_duration());
		try
		{
		musicDatabase.beginTransaction();		
		musicDatabase.insert(tracks_table, null, cv);
		musicDatabase.setTransactionSuccessful();		
		}
		finally
		{ musicDatabase.endTransaction(); }
	}
	
	public void addTrackInfo(SongEntity song, String artist, String title)
	{
		if(song == null) //TODO: change so that boolean is returned or exception is raised
		{ return; }
		ContentValues cv = new ContentValues();
		cv.put("songid", song.get_echonest_id());
		//if artist field is empty use the value of argument 'artist' instead so that some valid data is there
		if(song.get_artist().length() == 0)
		{ song.set_artist(artist); }
		cv.put("artist", song.get_artist());
		//if title field is empty use the value of argument 'title' instead so that some valid data is there
		if(song.get_title().length() == 0)
		{ song.set_title(title); }
		cv.put("name", song.get_title());
		cv.put("energy", song.get_energy());
		cv.put("tempo", song.get_tempo());
		cv.put("danceability", song.get_danceability());
		cv.put("duration", song.get_duration());
		try
		{
		musicDatabase.beginTransaction();		
		musicDatabase.insert(tracks_table, null, cv);
		musicDatabase.setTransactionSuccessful();
		}
		finally
		{ musicDatabase.endTransaction(); }
	}
	
	public void createPlaylist(Playlist playlist, int[] trackIDs)
	{
		//TODO: must change return type to boolean and return true if playlist was added and false otherwise
		
		//TODO: throw exception or return false
		if(playlist == null)
		{ return; }
		
		//make sure there are no spaces in the table name for the playlist's tracklist. otherwise, screws things while creating the table and making queries
		playlist.setSongListTableName(playlist.getSongListTableName().replace(" ", "_"));
		
		ContentValues cv = new ContentValues();
		cv.put("name", playlist.getName());
		cv.put("numsongs", playlist.getNumSongs());
		cv.put("songlisttablename", playlist.getSongListTableName());
		
		
		try
		{
		musicDatabase.beginTransaction();
		//TODO: create table (name is playlist.getSongListTableName()) and add track ID's to it		
		
		
		try
		{
			String createTrackListTableSQLStatement = "create table " + playlist.getSongListTableName() + " (trackid integer not null primary key);";
			musicDatabase.execSQL(createTrackListTableSQLStatement);
		}
		catch(SQLException excptn)
		{
			//check for errors and fix appropriately.
		}
		musicDatabase.insert(playlist_table, null, cv);		
		for(int counter=0; counter<trackIDs.length; counter++)
		{
			ContentValues values = new ContentValues();
			values.put("trackid", trackIDs[counter]);
			musicDatabase.insert(playlist.getSongListTableName(), null, values);
			values = null;
		}	
		
		musicDatabase.setTransactionSuccessful();
		}
		finally
		{ musicDatabase.endTransaction(); }
	}
	
	public Cursor getTracksInPlaylist(long playlistId)
	{
		Cursor playlistItem = musicDatabase.query(playlist_table, new String[] {"songlisttablename"}, "_id = " + playlistId, null, null, null, null);
		playlistItem.moveToFirst();
		Cursor playlistTracksCursor = musicDatabase.query(playlistItem.getString(playlistItem.getColumnIndex("songlisttablename")), new String[] {"trackid"}, null, null, null, null, null);
		return playlistTracksCursor;
	}
	
	public String getTrackTitle(int trackId)
	{
		Cursor trackCursor = musicDatabase.query(tracks_table, new String[] {"name"}, "_id=" + trackId, null, null, null, null);
		trackCursor.moveToFirst();
		return trackCursor.getString(trackCursor.getColumnIndex("name"));
	}	
	
	public int getNumTracksInPlaylist(int playlistId)
	{
		Cursor playlistItem = musicDatabase.query(playlist_table, new String[] {"numsongs"}, "_id = " + playlistId, null, null, null, null);
		playlistItem.moveToFirst();
		return playlistItem.getInt(playlistItem.getColumnIndex("numsongs"));
	}
}
