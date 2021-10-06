package com.example.wifi_nsd_test;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class NSDHelper {


    private final String TAG = NSDHelper.class.getSimpleName();

    private final Context context;
    private final NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;


    public interface Callback{
        void onServiceFound(NsdServiceInfo service);
        void onServiceResolved(NsdServiceInfo serviceInfo);
    }

    private Callback callback;
    public void registerCallback(Callback callback){
        this.callback = callback;
    }

    public NSDHelper(Context context){
        this.context = context;

        initializeDiscoveryListener();
        initializeResolveListener();

        nsdManager =  (NsdManager) context.getSystemService(Context.NSD_SERVICE);

    }

    private final String SERVICE_TYPE = "_http._tcp.";
    private final String serviceName = "ESP32-WebServer";

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success: " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(serviceName)) {
//                    // The name of the service tells the user what they'd be
//                    // connecting to. It could be "Bob's Chat App".
//                    Log.d(TAG, "Same machine: " + serviceName);
//                } else if (service.getServiceName().contains("NsdChat")){
                    nsdManager.resolveService(service, resolveListener);
                }

                if (callback != null)
                    callback.onServiceFound(service);

            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
                Log.e(TAG, "Resolve failed: " + errorCode);
                Log.e(TAG, "Resolve failed, NsdServiceInfo: " + serviceInfo);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                Log.e(TAG, "Service name: " + serviceInfo.getServiceName());
                Log.e(TAG, "Service ip: " + serviceInfo.getHost().getHostAddress());
                Log.e(TAG, "Service port: " + serviceInfo.getPort());

                if (callback != null)
                    callback.onServiceResolved(serviceInfo);

//                if (serviceInfo.getServiceName().equals(serviceName)) {
//                    Log.d(TAG, "Same IP.");
//                    return;
//                }
//                mService = serviceInfo;
//                int port = mService.getPort();
//                InetAddress host = mService.getHost();
            }
        };
    }


    public void discoverServices(){
        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    // NsdHelper's tearDown method
    public void tearDown() {
        //nsdManager.unregisterService(registrationListener);
        try {
            nsdManager.stopServiceDiscovery(discoveryListener);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
