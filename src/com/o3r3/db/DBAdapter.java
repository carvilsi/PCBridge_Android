package com.o3r3.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



/**
 * 
 * @author carvilsi
 * 
 */

public class DBAdapter extends SQLiteOpenHelper {
	public static final String tag = DBAdapter.class.getSimpleName();
	static final String DB_NAME = "bab.db";
	static final int DB_VERSION = 4; //1


	/* LOGS TABLEs */
	
	public static final String tablaLog = "babdb";
	public static final String logID = "id";
	public static final String contenido = "contenido";
	public static final String compartido = "compartido";
	public static final String momento = "momento";

	public DBAdapter(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	// Called only once, first time the DB is created
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		
//		----------------------------- TABLA LOG Ver2 --------------------------
		
//		String sql2 = "create table " + tablaLog + " (" + logID +  " int primary key, " + 
//				contenido + " varchar not null unique, " + compartido + " int)";

//		----------------------------- TABLA LOG Ver3 --------------------------
		
		String sql1 = "create table " + tablaLog + " (" + logID +  " integer primary key, " + 
//				contenido + " varchar not null unique, " + compartido + " int, " + momento + " text default datetime('now'))";
//				contenido + " varchar not null unique, " + compartido + " int, " + momento + " datetime default CURRENT_TIMESTAMP)";
//				contenido + " varchar not null unique, " + compartido + " int, " + momento + " TEXT DEFAULT (datetime('now', 'localtime'))";
				contenido + " varchar not null unique, " + compartido + " int, " + momento + " TEXT DEFAULT (datetime('now', 'localtime')))";
		
		
		
		db.execSQL(sql1); 
		
		Log.d(tag, "onCreated sql: " + sql1);
				
	}

	
	/*
	 * Al LORO por que en producciOn no puedo hacer drop tan alegremente, al menos con la tabla configuraciOn
	 *
	 * Called whenever newVersion != oldVersion
	 * take care about droping the db
	 * 
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String tablaLogTemp = "tablaLogTemp";
		String sql2 = "create table " + tablaLogTemp + " (" + 
		contenido + " varchar not null unique, " + compartido + " int)";
		db.execSQL(sql2);
		sql2 = "insert into " + tablaLogTemp + "( " + contenido + ", " + compartido + ") select " + contenido + ", " + compartido + " from " + tablaLog; 
		db.execSQL(sql2);
		db.execSQL("drop table if exists " + tablaLog); // drops database
		onCreate(db); 
		sql2 = "insert into " + tablaLog + "( " + contenido + ", " + compartido + ") select " + contenido + ", " + compartido + " from " + tablaLogTemp;
		db.execSQL(sql2);
		Log.d(tag, "onUpdated");
	}
	
}
