/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.http.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.http.HttpConnectionFuture;
import microsoft.aspnet.signalr.client.http.HttpConnectionFuture.ResponseCallback;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.http.StreamResponse;

/**
 * Runnable that executes a network operation
 */
class NetworkRunnable implements Runnable {

    HttpURLConnection mConnection = null;
    InputStream mResponseStream = null;
    Logger mLogger;
    Request mRequest;
    HttpConnectionFuture mFuture;
    ResponseCallback mCallback;

    Object mCloseLock = new Object();

    /**
     * Initializes the network runnable
     *
     * @param logger   logger to log activity
     * @param request  The request to execute
     * @param future   Future for the operation
     * @param callback Callback to invoke after the request execution
     */
    public NetworkRunnable(Logger logger, Request request, HttpConnectionFuture future, ResponseCallback callback) {
        mLogger = logger;
        mRequest = request;
        mFuture = future;
        mCallback = callback;
    }

    @Override
    public void run() {
        try {
            int responseCode = -1;
            if (!mFuture.isCancelled()) {
                if (mRequest == null) {
                    mFuture.triggerError(new IllegalArgumentException("request"));
                    return;
                }

                mLogger.log("Execute the HTTP Request", LogLevel.Verbose);
                mRequest.log(mLogger);
                mConnection = createHttpURLConnection(mRequest);

                mLogger.log("Request executed", LogLevel.Verbose);

                responseCode = mConnection.getResponseCode();

                if (responseCode < 400) {
                    mResponseStream = mConnection.getInputStream();
                } else {
                    mResponseStream = mConnection.getErrorStream();
                }
            }

            if (mResponseStream != null && !mFuture.isCancelled()) {
                mCallback.onResponse(new StreamResponse(mResponseStream, responseCode, mConnection.getHeaderFields()));
                mFuture.setResult(null);
            }
        } catch (Throwable e) {
            if (!mFuture.isCancelled()) {
                if (mConnection != null) {
                    mConnection.disconnect();
                }

                mLogger.log("Error executing request: " + e.getMessage(), LogLevel.Critical);
                mFuture.triggerError(e);
            }
        } finally {
            closeStreamAndConnection();
        }
    }

    /**
     * Closes the stream and connection, if possible
     */
    void closeStreamAndConnection() {

        try {
            if (mConnection != null) {
                mConnection.disconnect();
            }

            if (mResponseStream != null) {
                mResponseStream.close();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Creates an HttpURLConnection
     *
     * @param request The request info
     * @return An HttpURLConnection to execute the request
     * @throws java.io.IOException
     */
    static HttpURLConnection createHttpURLConnection(Request request) throws IOException {
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

        URL url = new URL(request.getUrl());

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(15 * 1000);
        connection.setRequestMethod(request.getVerb());
        connection.setSSLSocketFactory(sslSocketFactory);

        Map<String, String> headers = request.getHeaders();

        for (String key : headers.keySet()) {
            connection.setRequestProperty(key, headers.get(key));
        }

        if (request.getContent() != null) {
            connection.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            String requestContent = request.getContent();
            out.write(requestContent);
            out.close();
        }

        return connection;
    }

}
