package com.o3r3.browserandroidbridge;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;


//public class NanoWebServer extends NanoHTTPDS {
public class NanoWebServer extends NanoHTTPD {
	@SuppressWarnings("unused")
	private Context context;
	/**
	 * Common mime types for dynamic content
	 */
	public static final String MIME_PLAINTEXT = "text/plain",
			MIME_HTML = "text/html", MIME_JS = "application/javascript",
			MIME_CSS = "text/css", MIME_PNG = "image/png",
			MIME_GIF = "image/gif", MIME_JPG = "image/jpg",
			MIME_DEFAULT_BINARY = "application/octet-stream",
			MIME_XML = "text/xml",
            MIME_VIDEO_3GP = "video/3gpp";
	private AssetManager asset = null;

	public NanoWebServer(Context context) {
        super(8585);
//        Para intentar leer el certificado y poder tener ssl
//        SSLServerSocketFactory sslF = null;
//        try {
////            AssetManager asset = context.getAssets();
////            String cert[] = asset.list("c");
////            InputStream cert = asset.open("c/mykeystore.jks");
//////            InputStream cert = getAssets().open("blabla.keystore");
////            if (cert != null) {
////                KeyStore keyStore;
////                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
////                keyStore.load(cert, "mystorepassword".toCharArray());
////                KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//////                KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
////                factory.init(keyStore, "mycert".toCharArray());
//////                SSLContext
////                SSLContext sslContext = SSLContext.getInstance("SSL");
////                sslContext.init(factory.getKeyManagers(), null, new SecureRandom());
//////                sockets = context.getServerSocketFactory();
////            }
////            NanoHTTPD.
////            super.
////            asset.
////            KeyStore keyStore = cert.
////            super.makeSecure();
//
//
//
//            String fullPath = Environment.getExternalStorageDirectory()
//                    + File.separator + Constantes.directory + File.separator
////                    + "c" + File.separator + "mykeystore.jks";
//                    + "c" + File.separator + "mycert";
////            sslF = NanoHTTPD.makeSSLSocketFactory(fullPath, "mystorepassword".toCharArray());
//            sslF = NanoHTTPDS.makeSSLSocketFactory(fullPath, "mystorepassword".toCharArray());
//
//
////            NanoHTTPD.
//
////            if (fullPath != null) {
////                super.
////            }
//
//
//
//
//        } catch (Exception e) {
//            Log.e(PCBridgeActivity.tag, "Algun problema con el ssl");
//        }
//        super.makeSecure(sslF);
//        try {
//            super.start();
//        } catch (IOException e) {
//            Log.e(PCBridgeActivity.tag, "Algun problema con el servidor");
//        }
        this.context = context;
		asset = context.getAssets();
	}

	@Override
	public Response serve(String uri, Method method,
			Map<String, String> header, Map<String, String> parameters,
			Map<String, String> files) {
		try {
			if (method.equals(Method.GET)){
//				Log.i(PCBridgeActivity.tag, "estoy en la cosa del GET");
				if (uri.contains(".jpg") || uri.contains(".JPG")) {
						return new NanoHTTPD.Response(Status.PARTIAL_CONTENT, MIME_DEFAULT_BINARY,
								readContentImage(uri.substring(1)));
				} 
				if (uri.contains(".png") || uri.contains(".PNG")) {
					if (uri.substring(1).indexOf("css/") == -1) {
						return new NanoHTTPD.Response(Status.PARTIAL_CONTENT, MIME_DEFAULT_BINARY,
								readContentImage(uri.substring(1)));
					}
				}
                if (uri.contains(".3gp") || uri.contains(".3GP")) {
                        return new NanoHTTPD.Response(Status.PARTIAL_CONTENT, MIME_DEFAULT_BINARY,
                                readContentImage(uri.substring(1)));
                }if (uri.contains(".aac") || uri.contains(".AAC")) {
                        return new NanoHTTPD.Response(Status.PARTIAL_CONTENT, MIME_DEFAULT_BINARY,
                                readContentImage(uri.substring(1)));
                }if (uri.contains(".amr") || uri.contains(".AMR")) {
                        return new NanoHTTPD.Response(Status.PARTIAL_CONTENT, MIME_DEFAULT_BINARY,
                                readContentImage(uri.substring(1)));
                }
            }
			if (uri.contains(".js")) {
//				Log.i(PCBridgeActivity.tag, MIME_JS);
				return new NanoHTTPD.Response(Status.OK, MIME_JS,
						readContent(uri.substring(1)));
			} else if (uri.contains(".css")) {
//				Log.i(PCBridgeActivity.tag, MIME_CSS);
				return new NanoHTTPD.Response(Status.OK, MIME_CSS,
						readContent(uri.substring(1)));
			} else if (uri.contains(".png")) {
//				Log.i(PCBridgeActivity.tag, MIME_PNG);
				return new NanoHTTPD.Response(Status.OK, MIME_PNG, readContent(uri.substring(1)));
			} else if (uri.contains(".html")) {
//				Log.i(PCBridgeActivity.tag, MIME_HTML);
				return new Response(Status.OK, MIME_HTML,
						readContent(uri.substring(1)));
			} else if (uri.contains(".gif")) {
//				Log.i(PCBridgeActivity.tag, MIME_GIF);
				return new NanoHTTPD.Response(Status.OK, MIME_GIF,
						readContent(uri.substring(1)));
//			} else if (uri.contains(".3gp")) {
//				Log.i(PCBridgeActivity.tag, MIME_VIDEO_3GP);
//				return new NanoHTTPD.Response(Status.OK, MIME_VIDEO_3GP,
//						readContentImage(uri.substring(1)));
			} else {
				return new Response(Status.OK, MIME_HTML,
						readContent("PCBridge.html"));
			}
		} catch (Exception ioe) {
			Log.w("Httpd", ioe.toString());
			return null;
		}
	}

	private InputStream readContent(String uri) {
		InputStream is = null;
		try {
			is = asset.open("www/" + uri);
		} catch (Exception e) {
			Log.e(PCBridgeActivity.tag, "read Content:  " + e.toString());
		}
		return is;
	}

	private InputStream readContentImage(String uri) {
		InputStream is = null;
		try {
			is = new FileInputStream(uri);
			// Log.e(PCBridgeActivity.tag, "SO YO EL QUE DICE DICE :  " + uri);
		} catch (Exception e) {
			Log.e(PCBridgeActivity.tag, "read content Image:  " + e.toString());
		}
		return is;
	}

}
