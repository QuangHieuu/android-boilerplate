/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.hubs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.Connection;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.InvalidStateException;
import microsoft.aspnet.signalr.client.Logger;

/**
 * Represents a SignalRConnection that implements the Hubs protocol
 */
public class HubConnection extends Connection {

    private final Map<String, Action<HubResult>> mCallbacks = Collections.synchronizedMap(new HashMap<String, Action<HubResult>>());
    private final Map<String, HubProxy> mHubs = Collections.synchronizedMap(new HashMap<String, HubProxy>());
    private Integer mCallbackId = 0;

    /**
     * Initializes the connection
     *
     * @param url           The connection URL
     * @param queryString   The connection query string
     * @param useDefaultUrl indicates if the default SignalR URL should be used
     * @param logger        The connection logger
     */
    public HubConnection(String url, String queryString, boolean useDefaultUrl, Logger logger) {
        super(getUrl(url, useDefaultUrl), queryString, logger);
    }

    /**
     * Initialized the connection
     *
     * @param url The connection URL
     */
    public HubConnection(String url) {
        super(getUrl(url, true));
    }

    /**
     * Initializes the connection
     *
     * @param url           The connection URL
     * @param useDefaultUrl indicates if the default SignalR URL should be used
     */
    public HubConnection(String url, boolean useDefaultUrl) {
        super(getUrl(url, useDefaultUrl));
    }

    @Override
    public void onReceived(JsonElement message) {
        super.onReceived(message);

        if (getState() == ConnectionState.Connected) {
            if (message.isJsonObject() && message.getAsJsonObject().has("I")) {
                HubResult result = mGson.fromJson(message, HubResult.class);

                String id = result.getId().toLowerCase(Locale.getDefault());

                if (mCallbacks.containsKey(id)) {
                    Action<HubResult> callback = mCallbacks.remove(id);

                    try {
                        callback.run(result);
                    } catch (Exception e) {
                        onError(e, false);
                    }
                }
            } else {
                HubInvocation invocation = mGson.fromJson(message, HubInvocation.class);
                String hubName = invocation.getHub().toLowerCase(Locale.getDefault());
                if (mHubs.containsKey(hubName)) {
                    HubProxy hubProxy = mHubs.get(hubName);
                    if (invocation.getState() != null) {
                        for (String key : invocation.getState().keySet()) {
                            JsonElement value = invocation.getState().get(key);
                            hubProxy.setState(key, value);
                        }
                    }

                    String eventName = invocation.getMethod().toLowerCase(Locale.getDefault());
                    try {
                        hubProxy.invokeEvent(eventName, invocation.getArgs());
                    } catch (Exception e) {
                        onError(e, false);
                    }
                }
            }
        }
    }

    private static String arrayToString(JsonElement[] args) {
        StringBuilder sb = new StringBuilder();

        sb.append("[");

        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(args[i].toString());
        }

        sb.append("]");

        return sb.toString();
    }

    @Override
    public String getConnectionData() {
        JsonArray jsonArray = new JsonArray();

        for (String hubName : mHubs.keySet()) {
            JsonObject element = new JsonObject();
            element.addProperty("name", hubName);
            jsonArray.add(element);
        }

        return jsonArray.toString();
    }

    @Override
    protected void onClosed() {
        clearInvocationCallbacks("Connection closed");
        super.onClosed();
    }

    private void clearInvocationCallbacks(String error) {
        HubResult result = new HubResult();
        result.setError(error);

        for (String key : mCallbacks.keySet()) {
            try {
                mCallbacks.get(key).run(result);
            } catch (Exception ignore) {
            }
        }

        mCallbacks.clear();
    }

    @Override
    protected void onReconnecting() {
        clearInvocationCallbacks("Reconnecting");
        super.onReconnecting();
    }

    /**
     * Creates a proxy for a hub
     *
     * @param hubName The hub name
     * @return The proxy for the hub
     * @throws InvalidStateException If called when not disconnected, the method will throw an
     *                               exception
     */
    public HubProxy createHubProxy(String hubName) {
        if (mState != ConnectionState.Disconnected) {
            throw new InvalidStateException(mState);
        }

        if (hubName == null) {
            throw new IllegalArgumentException("hubName cannot be null");
        }

        String hubNameLower = hubName.toLowerCase(Locale.getDefault());

        HubProxy proxy;
        if (mHubs.containsKey(hubNameLower)) {
            proxy = mHubs.get(hubNameLower);
        } else {
            proxy = new HubProxy(this, hubName, getLogger());
            mHubs.put(hubNameLower, proxy);
        }

        return proxy;
    }

    /**
     * Registers a callback
     *
     * @param callback The callback to register
     * @return The callback Id
     */
    String registerCallback(Action<HubResult> callback) {
        String id = mCallbackId.toString().toLowerCase(Locale.getDefault());
        mCallbacks.put(id, callback);
        mCallbackId++;
        return id;
    }

    /**
     * Removes a callback
     *
     * @param callbackId Id for the callback to remove
     */
    void removeCallback(String callbackId) {
        mCallbacks.remove(callbackId.toLowerCase(Locale.getDefault()));
    }

    /**
     * Generates a standarized URL
     *
     * @param url           The base URL
     * @param useDefaultUrl Indicates if the default SignalR suffix should be appended
     * @return The connection URL
     */
    private static String getUrl(String url, boolean useDefaultUrl) {
        if (!url.endsWith("/")) {
            url += "/";
        }

        if (useDefaultUrl) {
            return url + "signalr";
        }

        return url;
    }

    @Override
    protected String getSourceNameForLog() {
        return "HubConnection";
    }
}
