package com.github.thaunknown;

import android.content.Context;
import android.content.Intent;
import java.net.URISyntaxException;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.util.InternalUtils;

@CapacitorPlugin(name = "IntentUri")
public class IntentUriPlugin extends Plugin {
    private static final int INTENT_URI_REQUEST_CODE = 9001;
    private PluginCall savedCall;
    private boolean waitingForIntent = false;

    @PluginMethod
    public void openUri(PluginCall call) {
        String url = call.getString("url");
        if (url == null) {
            call.reject("Must provide a url to open");
            return;
        }

        final PackageManager manager = getContext().getPackageManager();
        Intent launchIntent;
        try {
            launchIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
        } catch (URISyntaxException e) {
            JSObject ret = new JSObject();
            ret.put("completed", false);
            ret.put("message", e.getMessage());
            call.resolve(ret);
            return;
        }

        try {
            savedCall = call;
            waitingForIntent = true;
            startActivityForResult(call, launchIntent, INTENT_URI_REQUEST_CODE);
        } catch (Exception ex) {
            try {
                launchIntent = manager.getLaunchIntentForPackage(url);
                savedCall = call;
                waitingForIntent = true;
                startActivityForResult(call, launchIntent, INTENT_URI_REQUEST_CODE);
            } catch (Exception expgk) {
                JSObject ret = new JSObject();
                ret.put("completed", false);
                ret.put("message", expgk.getMessage());
                call.resolve(ret);
            }
        }
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_URI_REQUEST_CODE && savedCall != null) {
            JSObject ret = new JSObject();
            ret.put("completed", true);
            savedCall.resolve(ret);
            savedCall = null;
            waitingForIntent = false;
        }
    }

    @Override
    protected void handleOnResume() {
        super.handleOnResume();
        // If we were waiting for an intent and the call is still pending, resolve it now
        if (waitingForIntent && savedCall != null) {
            JSObject ret = new JSObject();
            ret.put("completed", true);
            savedCall.resolve(ret);
            savedCall = null;
            waitingForIntent = false;
        }
    }
}
