package com.o3r3.browserandroidbridge;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.o3r3.db.LogDB;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


//import whitebyte.wifihotspotutils.WIFI_AP_STATE;
import whitebyte.wifihotspotutils.WifiApManager;
import static com.o3r3.browserandroidbridge.R.*;
import static com.o3r3.browserandroidbridge.R.string.app_name;

public class PCBridgeActivity extends ActionBarActivity {

	private TextView textIP, textExplanation;
	public static final String tag = PCBridgeActivity.class.getName();
	private int port = 8887;
	public static final String PREFS_BAB_POC = "BAB";
	private LogDB logdb;
	private ListView listView;
	private ArrayAdapter<String> listAdapter;
	public View row;
	private boolean mostrado = false;
	private myReceiverLocal receiverWIFI, receiverAP;
	private IntentFilter filterWIFI, filterAP;
	private Context context;
	private List<String[]> listaCompartidos;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout.activity_bapoc);
		context = this;
	

		/*
		 * database be prepare
		 */

		logdb = new LogDB(this);

		/*
		 * calls to textView
		 */

		textExplanation = (TextView) findViewById(id.explicacionTextView);
		textIP = (TextView) findViewById(id.ipTextView);

		miraLaConexiOn();

		

		/*
		 * Preparamos la cosa de la lista le aNYadimos un listener para poder
		 * eliminar una entrada de la lista mostrando un dialogo al usuario,
		 * para confirmar la acciOn
		 */

		listView = (ListView) findViewById(id.listView1);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
					long arg3) {

				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						final int pos = arg2;
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								context);
						// set title
						alertDialogBuilder.setTitle(app_name);
						// set dialog message
						alertDialogBuilder
								.setMessage(string.borrarCompartidoMensaje)
								.setCancelable(false)
								.setPositiveButton(string.si,
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int id) {
												PCBridgeActivity.this
														.eliminarContenido(pos);
											}
										})
								.setNegativeButton(string.no,
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});
						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();
						// show it
						alertDialog.show();
						
					}
				});
			}
		});

        /**
         * 
         * To copy the content of shared element to clipboard
         * Waiting for long click
         * 
         * Para poder copiar el compartido al portapapeles,
         * se espera un click largo
         */


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                String cosa = listAdapter.getItem(position);
                if (setClipboardContent(cosa)) {
                    Toast.makeText(context, string.copied_clipboard, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, string.copied_clipboard, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });


        WsServer wsServer = null;
		try {
			wsServer = new WsServer(port, this);
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
		}

		/*
		 * Intentar llamar al intent del servicio
		 */

		Intent intent = getIntent();

		// get the action
		String receivedAction = intent.getAction();
		// find out what we are dealing with
		String receivedType = intent.getType();
		// @todo esto hay que meterlo en un hilo separado
		// make sure it's an action and type we can handle
		if (receivedAction.equals(Intent.ACTION_SEND)) {
			// Ponemos un gran try-catch para poder gestionar los posibles
			// errores a la hora de compartir
			// @todo se podr�a mostrar una actividad que sugiriera al usuario el
			// mandarnos un aviso, indicando
			// con que aplicaci�n en concreto se ha jodido la cosa.

			try {
				if (receivedType.startsWith("text/")) {
					String receivedText = intent
							.getStringExtra(Intent.EXTRA_TEXT);
					/*
					 * Intentamos eliminar todas las "" para evitar problemas en
					 * el parseo del cliente
					 */
					String contenido = receivedText.replace("\"", "");
					nuevoCompartidoOCambia(contenido);
					setContenidoListView();
				}
                /*
                En el caso de que recibo una imagen suelta
                 */
				if (receivedType.startsWith("image/")) {
					Uri imageURI = (Uri) intent
							.getParcelableExtra(Intent.EXTRA_STREAM);
					String imagePath = getRealPathFromUri(this, imageURI);
					if (imagePath != null) {
						nuevoCompartidoOCambia(imagePath);
					} else {
						String rutaCompartido = imageURI.toString();
						if (rutaCompartido != null && rutaCompartido.length() != 0 && rutaCompartido.indexOf("file:///") != -1) {
							String ruta = rutaCompartido.substring(rutaCompartido.indexOf("file:///") + 7, rutaCompartido.length());
							nuevoCompartidoOCambia(ruta);
						}
					}
					
					setContenidoListView();
				}
                /*
                En el caso de que recibo un video suelto
                 */
                if (receivedType.startsWith("video/")) {
                    Uri imageURI = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    String imagePath = getRealPathFromUri(this, imageURI);
                    if (imagePath != null) {
                        nuevoCompartidoOCambia(imagePath);
                    } else {
                        String rutaCompartido = imageURI.toString();
                        if (rutaCompartido != null && rutaCompartido.length() != 0 && rutaCompartido.indexOf("file:///") != -1) {
                            String ruta = rutaCompartido.substring(rutaCompartido.indexOf("file:///") + 7, rutaCompartido.length());
                            nuevoCompartidoOCambia(ruta);
                        }
                    }

                    setContenidoListView();
                }/*
                En el caso de que recibo un audio suelto
                 */
                if (receivedType.startsWith("audio/")) {
                    Uri imageURI = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    String imagePath = getRealPathFromUri(this, imageURI);
                    if (imagePath != null) {
                        nuevoCompartidoOCambia(imagePath);
                    } else {
                        String rutaCompartido = imageURI.toString();
                        if (rutaCompartido != null && rutaCompartido.length() != 0 && rutaCompartido.indexOf("file:///") != -1) {
                            String ruta = rutaCompartido.substring(rutaCompartido.indexOf("file:///") + 7, rutaCompartido.length());
                            nuevoCompartidoOCambia(ruta);
                        }
                    }

                    setContenidoListView();
                }

				// content share message/rfc822 kind
				if (receivedType.startsWith("message/rfc822")) {
					Bundle bundle = intent.getExtras();
					if (bundle != null) {
						// comprobar el contenido del bundle en busca de los
						// keys TEXT y SUBJECT
						// esto es para TripAdvisor
						StringBuilder builder = new StringBuilder();
						if (bundle.keySet() != null) {
							for (String key : bundle.keySet()) {
								if (key.equals(Intent.EXTRA_TEXT)) {
									if (bundle.containsKey(Intent.EXTRA_TEXT)) {
										SpannableString cadena = (SpannableString) bundle
												.get(Intent.EXTRA_TEXT);
										SpannableStringBuilder spannBuilder = new SpannableStringBuilder();
										spannBuilder.append(cadena);
										builder.append(spannBuilder.toString());
									}
								} else if (key.equals(Intent.EXTRA_SUBJECT)) {
									builder.append(bundle
											.getString(Intent.EXTRA_SUBJECT));
								}
							}
						}
						if (!builder.toString().isEmpty()) {
							nuevoCompartidoOCambia(builder.toString());
						}

					}
				}
			}
			// Esta es la excepciOn de un al compartido
			catch (Exception ex) {
				Toast.makeText(this, string.noCompartible, Toast.LENGTH_SHORT)
						.show();
			}
		} else if (receivedAction.equals(Intent.ACTION_MAIN)) {
			setContenidoListView();
		}
		/*
		 * Para los intents que me mandan varias cosas, como por ejemplo la cosa
		 * de la galerIa
		 */
		else if (receivedAction.equals(Intent.ACTION_SEND_MULTIPLE)) {
            /*
            En el caso de que reciba una serie de imágenes
             */
			if (receivedType.startsWith("image/")) {
				ArrayList<Uri> imageURIList = new ArrayList<Uri>();
				imageURIList = intent
						.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
				for (Uri imageURI : imageURIList) {
					String imagePath = getRealPathFromUri(this, imageURI);
					if (imagePath != null) {
						nuevoCompartidoOCambia(imagePath);
					} else {
						String rutaCompartido = imageURI.toString();
						if (rutaCompartido != null && rutaCompartido.length() != 0 && rutaCompartido.indexOf("file:///") != -1) {
							String ruta = rutaCompartido.substring(rutaCompartido.indexOf("file:///") + 7, rutaCompartido.length());
							nuevoCompartidoOCambia(ruta);
						}
					}
				}
				setContenidoListView();
			}
            /*
            En el caso de que reciba una serie de vIdeos
             */
            if (receivedType.startsWith("video/")) {
				ArrayList<Uri> imageURIList = new ArrayList<Uri>();
				imageURIList = intent
						.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
				for (Uri imageURI : imageURIList) {
					String imagePath = getRealPathFromUri(this, imageURI);
					if (imagePath != null) {
						nuevoCompartidoOCambia(imagePath);
					} else {
						String rutaCompartido = imageURI.toString();
						if (rutaCompartido != null && rutaCompartido.length() != 0 && rutaCompartido.indexOf("file:///") != -1) {
							String ruta = rutaCompartido.substring(rutaCompartido.indexOf("file:///") + 7, rutaCompartido.length());
							nuevoCompartidoOCambia(ruta);
						}
					}
				}
				setContenidoListView();
			}/*
            En el caso de que reciba una serie de audio
             */
            if (receivedType.startsWith("audio/")) {
				ArrayList<Uri> imageURIList = new ArrayList<Uri>();
				imageURIList = intent
						.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
				for (Uri imageURI : imageURIList) {
					String imagePath = getRealPathFromUri(this, imageURI);
					if (imagePath != null) {
						nuevoCompartidoOCambia(imagePath);
					} else {
						String rutaCompartido = imageURI.toString();
						if (rutaCompartido != null && rutaCompartido.length() != 0 && rutaCompartido.indexOf("file:///") != -1) {
							String ruta = rutaCompartido.substring(rutaCompartido.indexOf("file:///") + 7, rutaCompartido.length());
							nuevoCompartidoOCambia(ruta);
						}
					}
				}
				setContenidoListView();
			}
		}

		/*
		 * Escuchar el cambio en la WIFI
		 */
		daemonWIFIConnectivity(true);
        daemonAPConnectivity(true);

		/*
		 * Try Start de WebSocket Server
		 */
		try {
			wsServer.startWS();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// StartWebServer
		NanoWebServer server = new NanoWebServer(this);
		try {
			server.start();
		} catch (IOException e) {
			Log.w(tag, e.toString());
			// @todo en este caso es necesario avisar al usuario de que se baje
			// el cliente
		}
		
	}

	private void miraLaConexiOn() {
		/*
		 * Intentar mostrar nuestra IP siempre y cuando estemos conectados a la
		 * WIFI
		 *
		 * en caso contrario rellenamos el texto con un mensaje que indique al
		 * usuario la necesidad de conectarse a la WIFI
		 */
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

   
        String ip = null;
        WifiApManager wifiApManager = new WifiApManager(this);
//        WIFI_AP_STATE state = wifiApManager.getWifiApState();
        if (mWifi.isConnected()) {
//            Log.i(tag, "SI QUE TENGO LA WIFI CONECTADA!!!");
			try {
				ip = Utils.getIPAddress(true);
				if (ip != null) {
					textIP.setText(" http://" + ip + ":8585");
					textExplanation.setText(getResources().getString(
							string.use));
				}
			} catch (Exception e) {
				Log.e(tag, "Problema a la hora de conocer la IP");
			}
		} else {

            if (wifiApManager.isWifiApEnabled()) {
                try {
                    ip = Utils.getIPAddress(true);
                    if (ip != null) {
                        textIP.setText(" http://" + ip + ":8585");
                        textExplanation.setText(getResources().getString(
                                string.use));
                    }
                } catch (Exception e) {
                    Log.e(tag, "Problema a la hora de conocer la IP");
                }
            } else {
                textExplanation.setText(getResources().getString(string.explicacionNoWifi));
                textIP.setText(getResources().getString(string.explicacionNoWifi1));
            }
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mostrado) {
			setContenidoListView();
		}
	}

	/*
	 * Esto revisa la disponibilidad de la red WIFI
	 */

	private void daemonWIFIConnectivity(boolean b) {
		if (b) {
			receiverWIFI = new myReceiverLocal();
			filterWIFI = new IntentFilter(ConnectivityActionReceiver.WIFI_STATE);
			// Register mMessageReceiver to receive messages.
			LocalBroadcastManager.getInstance(this).registerReceiver(receiverWIFI, filterWIFI);
		} else {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverWIFI);
		}
	}

    private void daemonAPConnectivity(boolean b) {
		if (b) {
            filterAP = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
			receiverAP = new myReceiverLocal();
			// Register mMessageReceiver to receive messages.
			LocalBroadcastManager.getInstance(this).registerReceiver(receiverAP, filterAP);
		} else {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverAP);
		}
	}

	/*
	 * Esto permite eliminar un contenido de compartidos que ha sido
	 * seleccionado desde la lista.
	 */

	private void eliminarContenido(int pos) {
		if (listaCompartidos != null) {
			String[] elementos = listaCompartidos.get(pos);
			logdb.eliminarCompartido(Integer.valueOf(elementos[0]));
			if (elementos[2].equals(String.valueOf(1))) {
				setContenidoListViewHistorico();
			} else {
				setContenidoListView();
			}
		}
	}

	/*
	 * Esto permite eliminar el historial de compartidos
	 */

	private void eliminarContenidoHistorial() {
		try {
			boolean b = logdb.eliminarLogs();
			if (!b) {
				Log.e(PCBridgeActivity.tag,
						"NO SE PUDO ELIMINAR LA LISTA DE HISTORICOS DE LA BBDD");
			}
		} catch (Exception e) {
			Log.e(PCBridgeActivity.tag,
					"NO SE PUDO ELIMINAR LA LISTA DE HISTORICOS DE LA BBDD");
		}
	}

	/*
	 * Muestra la serie de elementos no compartidos en tabla
	 */

	private void setContenidoListView() {
		listaCompartidos = logdb.dameNoCompartidos();
		ArrayList<String> lista = new ArrayList<String>();
		listAdapter = new ArrayAdapter<String>(this, layout.filacompartidos,
				lista);
		if (listaCompartidos != null) {
			for (String[] strings : listaCompartidos) {
				listAdapter.add(strings[1]);
			}
			listView.setAdapter(listAdapter);
		}
		mostrado = true;
	}

    private void nuevoCompartidoOCambia(String contenido) {
        if (logdb.creaCompartido(contenido, 0) == 0) {
            logdb.marcarComoNoCompartido(contenido);
        }
    }

	/*
	 * Muestra la serie de elementos si compartidos en tabla,
	 * es decir el histOrico
	 */
	
	private void setContenidoListViewHistorico() {
		listaCompartidos = logdb.dameCompartidos();
		ArrayList<String> lista = new ArrayList<String>();
		listAdapter = new ArrayAdapter<String>(this, layout.filacompartidos,
				lista);
		if (listaCompartidos.size() != 0) {
			for (String[] strings : listaCompartidos) {
				listAdapter.add(strings[1]);
			}
			listView.setAdapter(listAdapter);
		} 
		// Para cuando no hay nada que mostrar en el hist�rico
		else {
			Toast.makeText(getApplicationContext(),
					string.noContentHistory, Toast.LENGTH_SHORT)
					.show();
		}
	}
	
	/**
	 * Para saber si hay historicos en el compartido
	 * @return 
	 * 		true si hay 
	 * 		false si no hay
	 */
	private boolean isThereHistory () {
		if (logdb.dameCompartidos().size() != 0) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Por ahora dejamos sin preferencias la aplicacioncita, manias que tiene
	 * uno (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.bapoc, menu);
	// return true;
	// }


	/*
	 * Con esto pillamos cosas del portapapeles El caso es que hay que tener OJO
	 * con el tema del ClipBoard desde API 11 ya no se usa la cosa del
	 * antdoid.text, en su defecto usamos android.content pero para dispositivos
	 * con APIs viejunas hay que mirar el caso y aplicar una u otra. (de ahI el
	 * warning de deprecated)
	 * 
	 * @todo hay que probar este m�todo en android >= 11
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private String getClipboardConten() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			if (clipBoard.hasPrimaryClip()) {
				ClipData data = clipBoard.getPrimaryClip();
				ClipDescription desc = data.getDescription();
				if (desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
						|| desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
						|| desc.hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT)) {
					ClipData.Item item = data.getItemAt(0);
					return item.coerceToText(this).toString();
				} else {
					return null;
				}
			} else {
				return null;
			}
		} else {
			android.text.ClipboardManager clipBoard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			if (clipBoard != null && clipBoard.hasText()) {
				return (String) clipBoard.getText().toString();
			} else {
				return null;
			}
		}
	}

    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
    private boolean setClipboardContent(String data) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("conten_PCBridge", data);
                clipBoard.setPrimaryClip(clip);
            } else {
                android.text.ClipboardManager clipBoard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipBoard.setText(data.toString());
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

	@Override
	public void onDestroy() {
		daemonWIFIConnectivity(false);
		daemonAPConnectivity(false);
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		daemonWIFIConnectivity(false);
        daemonAPConnectivity(false);
		super.onStop();
	}

	@Override
	public void onStart() {
		daemonWIFIConnectivity(true);
        daemonAPConnectivity(true);
		miraLaConexiOn();
		setContenidoListView();
		super.onStart();
	}

	public static String getRealPathFromUri(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} catch (Exception ex) {
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

    /**
     * Esta clase permite escuchar cambios en la WIFI
     */
	private class myReceiverLocal extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			miraLaConexiOn();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.bar_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case id.action_paste:
			if (getClipboardConten() != null) {
				nuevoCompartidoOCambia(getClipboardConten());
				setContenidoListView();
			} else {
				Toast.makeText(getApplicationContext(),
						string.noContent, Toast.LENGTH_SHORT)
						.show();
			}
			return true;
			 case id.action_delete:
				 eliminarHistorialMensaje();
			 return true;
			 case id.action_history:
				 // Aqui el codigo para mostrar el historial
				 setContenidoListViewHistorico();
			 return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void eliminarHistorialMensaje() {
		if (isThereHistory()) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);
			// set title
			alertDialogBuilder.setTitle(app_name);
			// set dialog message
			alertDialogBuilder
					.setMessage(string.borrarCompartidoHistorialMensaje)
					.setCancelable(false)
					.setPositiveButton(string.si,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									PCBridgeActivity.this.eliminarContenidoHistorial();
									 setContenidoListView();
								}
							})
					.setNegativeButton(string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							});
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
		} else {
			Toast.makeText(getApplicationContext(),
					string.noContentHistory, Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 
	 * @todo implements httpS
	 * @todo meter filtro de fechas en cliente
	 */
}
