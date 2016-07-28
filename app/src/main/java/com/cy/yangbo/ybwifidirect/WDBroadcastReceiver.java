package com.cy.yangbo.ybwifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by Administrator on 2016/7/28.
 */
public class WDBroadcastReceiver extends BroadcastReceiver {

    private WifiDirectListener wifiDirectListener;

    public WDBroadcastReceiver(WifiDirectListener wifiDirectListener) {
        this.wifiDirectListener = wifiDirectListener;
    }

    public IntentFilter getIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        return intentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();

        switch (intentAction){
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                if(wifiDirectListener != null){
                    wifiDirectListener.p2pStateChanged(intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1));
                }
                break;
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                if(wifiDirectListener != null){
                    wifiDirectListener.p2pPeersChanged();
                }
                break;
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                if(wifiDirectListener != null){
                    wifiDirectListener.p2pConnectionChanged();
                }
                break;
            case WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION:
                if(wifiDirectListener != null){
                    wifiDirectListener.p2pDiscoveryChanged(intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1));
                }
                break;
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                if(wifiDirectListener != null){
                    wifiDirectListener.p2pDeviceChanged();
                }
                break;

        }
    }

    public interface WifiDirectListener{
        void p2pStateChanged(int state);
        void p2pPeersChanged();
        void p2pConnectionChanged();
        void p2pDiscoveryChanged(int state);
        void p2pDeviceChanged();
    }
}
