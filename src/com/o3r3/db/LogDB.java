package com.o3r3.db;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Button;



/**
 * For writing on database
 * 
 * @author @carvilsi
 *
 */


public class LogDB {

	private final static String tag = LogDB.class.getSimpleName();
	Button botonOnOff;
	Intent intent;
	DBAdapter dbAdapter;
	SQLiteDatabase db;
	Context context;
	File logFile = null;
	String columnas = null;

	public LogDB(Context context) {
		super();
		this.context = context;
		// database		
		dbAdapter = new DBAdapter(context);
	}
	
	/*
	 * Mete un nuevo compartido en la bbdd
	 * 
	 * New shared item into database
	 */
	
	@SuppressLint("InlinedApi")
	public long creaCompartido(String contenido, int compartido) {
		db = dbAdapter.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBAdapter.contenido, contenido);
		values.put(DBAdapter.compartido, compartido);
		long res = 0;
		try {
			 res = db.insertWithOnConflict(DBAdapter.tablaLog, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		} catch (SQLiteException sqlie) {
			return -1;
		}
		db.close();
		return res;
	}
	

	/*
	 * Lista todos las entradas en bbdd que no se han compartido a�n 
	 * 
	 * List all records on db pending of sharing 
	 * 
	 */
	
	public List<String[]> dameNoCompartidos() {
		List<String[]> logs = new ArrayList<String[]>();
		List<String> loglog = new ArrayList<String>();
		db = dbAdapter.getReadableDatabase();
		String selectQuery = "SELECT  * FROM " + DBAdapter.tablaLog + " WHERE " + DBAdapter.compartido + "='0'";
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if (cursor.moveToFirst()) {
			do {
				String[] log = new String[4];
				int i = 0;
				log[i] = String.valueOf(cursor.getInt(i));
				log[++i] = cursor.getString(i);
				log[++i] = cursor.getString(i);
				log[++i] = cursor.getString(i);
				logs.add(log);
				loglog.add(log[1]);
			} while (cursor.moveToNext());
		}
		cursor.close();	
		db.close();
		return logs;
	}
	
	/*
	 * Lista todos las entradas en bbdd que se han compartido
	 * 
	 *  List all records on db already shared
	 *  
	 */
	
	public List<String[]> dameCompartidos() {
		List<String[]> logs = new ArrayList<String[]>();
		List<String> loglog = new ArrayList<String>();
		db = dbAdapter.getReadableDatabase();
		String selectQuery = "SELECT  * FROM " + DBAdapter.tablaLog + " WHERE " + DBAdapter.compartido + "='1'";
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if (cursor.moveToFirst()) {
			do {
				String[] log = new String[4];
				int i = 0;
				log[i] = String.valueOf(cursor.getInt(i));
				log[++i] = cursor.getString(i);
				log[++i] = cursor.getString(i);
				log[++i] = cursor.getString(i);
				logs.add(log);
				loglog.add(log[1]);
			} while (cursor.moveToNext());
		}
		cursor.close();	
		db.close();
		return logs;
	}


	/*
	 * Para poder dejar todos los elementos no compartidos 
	 * como compartidos.
	 * T�pico uso para cuando se recibe desde el cliente
	 * un "ya me han llegado los enlaces"
	 */
	
	public boolean marcarTodosNoCompartidos() {
		db = dbAdapter.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBAdapter.compartido, 1);
		int numRows = db.update(DBAdapter.tablaLog, values, DBAdapter.compartido + "='0'", null);
		db.close();
		if (numRows != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * Para poder cambiar a no compartido cuando
	 * se quiere compartir de nuevo el mismo contenido por 
	 */
	
	public boolean marcarComoNoCompartido (String contenido) {
		db = dbAdapter.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBAdapter.compartido, 0);
		int numRows = db.update(DBAdapter.tablaLog, values, DBAdapter.contenido + "=\"" + contenido + "\"", null);
		db.close();
		if (numRows != 0) {
			return true;
		} else {
			return false;
		}
		
	}

	 /*
	  * Para eliminar todas las entradas de la bbdd
	  * Como un resetear historia
	  */
	
	
		public boolean eliminarLogs(){
			try {
				db = dbAdapter.getWritableDatabase();
				db.execSQL("delete from " + DBAdapter.tablaLog); // drops database
				db.close();
				return true;
			} catch (Exception e){
				Log.e(tag, "No se pudo eliminar la tabla Log " + e);
				return false;
			}
		}
		
		/*
		 *  Para eliminar una entrada en concreto
		 */
		public boolean eliminarCompartido(int id) {
			try {
				db = dbAdapter.getWritableDatabase();
				db.execSQL("delete from " + DBAdapter.tablaLog + " where " + DBAdapter.logID + "='" + String.valueOf(id) + "'"); 
				db.close();
				return true;
			} catch (Exception e) {
				return false;
			}
			
		}
		
		/*
		 *  Para eliminar una serie de entradas
		 */
		public boolean eliminarCompartido(String lista) {
			try {
				db = dbAdapter.getWritableDatabase();
				db.execSQL(String.format("DELETE FROM " + DBAdapter.tablaLog + " WHERE " + DBAdapter.logID + " IN (%s);", lista));
				db.close();
				return true;
			} catch (Exception e) {
				Log.e(tag, "No se pudo eliminar elemento de la tabla Log " + e);
				return false;
			}
			
		}
}