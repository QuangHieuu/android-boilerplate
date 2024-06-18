/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import microsoft.aspnet.signalr.client.Connection;
import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.Constants;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;
import microsoft.aspnet.signalr.client.http.HttpConnection;

/**
 * Implements the WebsocketTransport for the Java SignalR library
 * Created by stas on 07/07/14.
 */
public class WebsocketTransport extends HttpClientTransport {

    private String mPrefix;
    private static final Gson gson = new Gson();
    WebSocketClient mWebSocketClient;
    private UpdateableCancellableFuture<Void> mConnectionFuture;

    public WebsocketTransport(Logger logger) {
        super(logger);
    }

    public WebsocketTransport(Logger logger, HttpConnection httpConnection) {
        super(logger, httpConnection);
    }

    @Override
    public String getName() {
        return "webSockets";
    }

    @Override
    public boolean supportKeepAlive() {
        return true;
    }

    @Override
    public SignalRFuture<Void> start(ConnectionBase connection, ConnectionType connectionType, final DataResultCallback callback) {
        final String connectionString = connectionType == ConnectionType.InitialConnection ? "connect" : "reconnect";

        final String transport = getName();
        final String connectionToken = connection.getConnectionToken();
        final String connectionData = connection.getConnectionData() != null ? connection.getConnectionData() : "";
        final String bearerToken = connection.getBearerToken() != null ? connection.getBearerToken() : "";

        String url;
        try {
            url = connection.getUrl() + connectionString + '?'
                + "transport=" + URLEncoder.encode(transport, Constants.UTF8_NAME)
                + "&clientProtocol=" + URLEncoder.encode(Connection.PROTOCOL_VERSION.toString(), Constants.UTF8_NAME)
                + "&BearerToken=" + URLEncoder.encode(bearerToken, Constants.UTF8_NAME)
                + "&connectionToken=" + URLEncoder.encode(connectionToken, Constants.UTF8_NAME)
                + "&connectionData=" + URLEncoder.encode(connectionData, Constants.UTF8_NAME)
                + "&tid=0";
        } catch (UnsupportedEncodingException e) {
            url = "";
            e.printStackTrace();
        }

        mConnectionFuture = new UpdateableCancellableFuture<>(null);

        URI uri = null;
        try {
            if (!url.isEmpty()) {
                if (url.startsWith("https://")) {
                    uri = new URI(url.replace("https://", "wss://"));
                } else {
                    if (url.startsWith("http://")) {
                        uri = new URI(url.replace("http://", "ws://"));
                    } else {
                        uri = new URI(url);
                    }
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            mConnectionFuture.triggerError(e);
            return mConnectionFuture;
        }

        SSLSocketFactory sslSocketFactory;
        try {
            SSLContext ssl_ctx = SSLContext.getInstance("TLS");
            @SuppressWarnings("CustomX509TrustManager")
            TrustManager[] trust_mgr = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @SuppressWarnings("TrustAllX509TrustManager")
                    public void checkClientTrusted(X509Certificate[] certs, String t) {
                    }

                    @SuppressWarnings("TrustAllX509TrustManager")
                    public void checkServerTrusted(X509Certificate[] certs, String t) {
                    }
                }
            };
            ssl_ctx.init(null, trust_mgr, new SecureRandom());
            sslSocketFactory = ssl_ctx.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            sslSocketFactory = null;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                mConnectionFuture.setResult(null);
            }

            @Override
            public void onMessage(String s) {
                callback.onData(s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                mWebSocketClient.close();
                mConnectionFuture.triggerError(new Throwable(s));
            }

            @Override
            public void onError(Exception e) {
                mWebSocketClient.close();
                mConnectionFuture.triggerError(e);
            }
        };
        mWebSocketClient.setSocketFactory(sslSocketFactory);
        mWebSocketClient.connect();

        connection.closed(new Runnable() {
            @Override
            public void run() {
                mWebSocketClient.close();
            }
        });

        return mConnectionFuture;
    }

    @Override
    public SignalRFuture<Void> send(ConnectionBase connection, String data, DataResultCallback callback) {
        try {
            mWebSocketClient.send(data);
        } catch (WebsocketNotConnectedException e) {
            mConnectionFuture.triggerError(e);
        }
        return new UpdateableCancellableFuture<Void>(null);
    }

    private boolean isJSONValid(String test) {
        try {
            gson.fromJson(test, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }
}