package com.o3r3.browserandroidbridge;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.util.Log;

import com.o3r3.db.LogDB;

/**
 * commands server
 * @author carvilsi
 *
 */


public class WsServer extends WebSocketServer {

	LogDB logdb;
	
	
	public WsServer(int port, Context context) throws UnknownHostException {
		super(new InetSocketAddress(port));
		logdb = new LogDB(context);
	}

	@Override
	public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) {
	}

	@Override
	public void onError(WebSocket arg0, Exception arg1) {
		Log.w(PCBridgeActivity.tag, "atenci�n: " + arg1);
		
	}

	@Override
	public void onMessage(WebSocket arg0, String arg1) {
		
		/*
		 * En cuanto conseguimos abrir el puerto con el explorador
		 * intentamos mandar todo el contenido al cliente, �ste nos
		 * manda un "OK OPEN"
		 */
		
		if (arg1.equals("OK OPEN")) {
			List<String[]> listaCompartidos = logdb.dameNoCompartidos();
			String compartidos = jsonFormat(listaCompartidos);
			if (compartidos != null) {
				sendToAll(compartidos);
			}
		}
		
		/*
		 * Recibimos por parte del cliente un "OK RECEIVED"
		 * lo que queremos es poder limpiar todos esos enlaces que tenemos en 
		 * bbdd y ya se han compartido
		 */
		if (arg1.equals("OK RECEIVED")) {
			logdb.marcarTodosNoCompartidos();
		}
		/*
		 * El cliente quiere que se le de la lista de compatidos hist�ricos
		 */
		if (arg1.equals("HIST")) {
			List<String[]> listaCompartidos = logdb.dameCompartidos();			
			String compartidos = jsonFormat(listaCompartidos);
			if (compartidos != null) {
				sendToAll(compartidos);
			}
		}
		/*
		 * El cliente quiere eliminar todos los compartidos hist�ricos
		 */
		if (arg1.equals("HIST_DEL")) {
			try {
				boolean b = logdb.eliminarLogs();
				if (!b) {
					Log.e(PCBridgeActivity.tag,"NO SE PUDO ELIMINAR LA LISTA DE HISTORICOS DE LA BBDD");
				}
			} catch (Exception e) {
				Log.e(PCBridgeActivity.tag,"NO SE PUDO ELIMINAR LA LISTA DE HISTORICOS DE LA BBDD");
			}
		}
		/*
		 * El cliente quiere eliminar los elementos seleccionados del historico
		 */
		if (arg1.contains("HIST_DEL_SEL:")) {
			try {
				String[] listaSeleccionados = arg1.split(":");
				if (listaSeleccionados.length > 1) {
					boolean b = logdb.eliminarCompartido(listaSeleccionados[1]);
					if (!b) {
						Log.e(PCBridgeActivity.tag,"NO SE PUDO ELIMINAR LA LISTA DE HISTORICOS DE LA BBDD");
					}
				} else {
					Log.w(PCBridgeActivity.tag, "ALgun problema con la recepcci�n de datos");
				}
			} catch (Exception e) {
				Log.e(PCBridgeActivity.tag,"NO SE PUDO ELIMINAR LA LISTA DE HISTORICOS DE LA BBDD");
			}
		}
		
		
	}
	
	/**
	 * formatea a json y devuelve String de lo que se le pasa de la bbdd 
	 * @param listaCompartidos
	 * @return
	 */
	private String jsonFormat(List<String[]> listaCompartidos) {
		JSONObject listaCompartidosJSON = new JSONObject();
		JSONArray listadoCompartidoJSON = new JSONArray();
		
		for (String[] compartidos : listaCompartidos) {
			JSONObject compartidoJSON = new JSONObject();
			try {
				compartidoJSON.put("id", compartidos[0]);
				compartidoJSON.put("contenido", compartidos[1]);
				compartidoJSON.put("fecha", compartidos[3]);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			listadoCompartidoJSON.put(compartidoJSON);
		}
		try {
			listaCompartidosJSON.put("listado", listadoCompartidoJSON);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			return listaCompartidosJSON.toString();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void onOpen(WebSocket arg0, ClientHandshake arg1) {
	}
	
	public void startWS() throws InterruptedException , IOException{
		this.start();
	}
	
	/**
	 * Sends <var>text</var> to all currently connected WebSocket clients.
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 */
	
	public void sendToAll( String text ) {
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				c.send( text );
			}
		}
	}

}
