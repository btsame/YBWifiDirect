package com.cy.yangbo.ybwifidirect;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pUpnpServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WDBroadcastReceiver.WifiDirectListener,
        View.OnClickListener{

    public static final int START_SCAN = 1;
    public static final int STOP_SCAN = 2;
    public static final int CREATE_GROUP = 3;
    public static final int REMOVE_GROUP = 4;
    public static final int CHECK_GROUP_INFO = 5;
    public static final int SCAN_TIME = 10000;

    Context mContext;

    Toolbar toolbar;
    RecyclerView rvDevice;
    View toolbarBg;
    Button btnDiscover, btnCreateGroup, btnAddService, btnDiscoverService;

    DeviceAdapter deviceAdapter;
    List<WifiP2pDevice> deviceList;

    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel wifiP2pChannel;

    WDBroadcastReceiver wdBroadcastReceiver;

    Handler handler;

    //是否已经创建组
    boolean groupCreated;
    String groupName, groupPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        initView();
        setListener();

        wdBroadcastReceiver = new WDBroadcastReceiver(this);
        wifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        wifiP2pChannel = wifiP2pManager.initialize(this, Looper.myLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                toastMessage("channel disconnected!!!");
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case START_SCAN:
                        deviceList.clear();
                        wifiP2pManager.discoverPeers(wifiP2pChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(int i) {

                            }
                        });
                        handler.sendEmptyMessageDelayed(STOP_SCAN, SCAN_TIME);
                        break;
                    case STOP_SCAN:
                        wifiP2pManager.stopPeerDiscovery(wifiP2pChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(int i) {

                            }
                        });
                        break;
                    case CREATE_GROUP:
                        if(msg.obj == null) sendEmptyMessageDelayed(CHECK_GROUP_INFO, 1000);
                        WifiP2pGroup wifiP2pGroup = (WifiP2pGroup) msg.obj;
                        groupCreated = true;
                        groupName = wifiP2pGroup.getNetworkName();
                        groupPass = wifiP2pGroup.getPassphrase();
                        changeBtnCreateGroupState(groupCreated);
                        deviceAdapter.notifyDataSetChanged();
                        break;
                    case REMOVE_GROUP:
                        groupCreated = false;
                        groupName = null;
                        groupPass = null;
                        changeBtnCreateGroupState(groupCreated);
                        deviceAdapter.notifyDataSetChanged();
                        break;
                    case CHECK_GROUP_INFO:
                        isOwnerGroupCreated();
                        break;
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wdBroadcastReceiver, wdBroadcastReceiver.getIntentFilter());
        isOwnerGroupCreated();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wdBroadcastReceiver);
    }

    private void initView(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Direct Wifi");
        setSupportActionBar(toolbar);

        rvDevice = (RecyclerView) findViewById(R.id.rv_wifi_direct);
        deviceList = new ArrayList<WifiP2pDevice>();
        deviceAdapter = new DeviceAdapter();
        rvDevice.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        rvDevice.setAdapter(deviceAdapter);

        toolbarBg = findViewById(R.id.view_title_bg);

        btnDiscover = (Button) findViewById(R.id.btn_discover);
        btnCreateGroup = (Button) findViewById(R.id.btn_create_group);
        btnAddService = (Button) findViewById(R.id.btn_add_service);
        btnDiscoverService = (Button) findViewById(R.id.btn_discover_service);


    }

    private void setListener(){
        btnDiscover.setOnClickListener(this);
        btnCreateGroup.setOnClickListener(this);
        btnAddService.setOnClickListener(this);
        btnDiscoverService.setOnClickListener(this);
    }

    private void changeBtnCreateGroupState(boolean hasCreated){
        if(hasCreated){
            btnCreateGroup.setText("消　组");
        }else{
            btnCreateGroup.setText("建　组");
        }
    }

    private void toastMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 判断作为owner创建了group
     * @return
     */
    private void isOwnerGroupCreated(){
        wifiP2pManager.requestGroupInfo(wifiP2pChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                if(wifiP2pGroup != null && wifiP2pGroup.isGroupOwner()){
                    handler.sendMessage(handler.obtainMessage(CREATE_GROUP, wifiP2pGroup));
                }
            }
        });
    }

    @Override
    public void p2pStateChanged(int state) {
        if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
            toastMessage("p2pWifi enabled");
        }else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
            toastMessage("p2pWifi disabled");
        }
    }

    @Override
    public void p2pPeersChanged() {
        wifiP2pManager.requestPeers(wifiP2pChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                toastMessage("peersAvailable");
                deviceList.clear();
                deviceList.addAll(wifiP2pDeviceList.getDeviceList());
                deviceAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void p2pConnectionChanged() {

    }

    @Override
    public void p2pDiscoveryChanged(int state) {
        if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED){
            toastMessage("开始扫描");
        }else if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED){
            toastMessage("结束扫描");
        }
    }

    @Override
    public void p2pDeviceChanged() {

    }

    @Override
    public void onClick(View view) {
        if(view == btnDiscover){
            handler.sendEmptyMessage(START_SCAN);
        }else if(view == btnCreateGroup){
            if(groupCreated){
                wifiP2pManager.removeGroup(wifiP2pChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        toastMessage("消组成功");
                        handler.sendMessage(handler.obtainMessage(REMOVE_GROUP));
                    }

                    @Override
                    public void onFailure(int i) {

                    }
                });
            }else{
                wifiP2pManager.createGroup(wifiP2pChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        toastMessage("建组成功");
                        handler.sendEmptyMessageDelayed(CHECK_GROUP_INFO, 1000);
                    }

                    @Override
                    public void onFailure(int i) {
                        toastMessage("建组失败：" + i);
                    }
                });
            }
        }else if(view == btnAddService){
            wifiP2pManager.addLocalService(wifiP2pChannel,
                    WifiP2pDnsSdServiceInfo.newInstance("transfer", "_transfer_tcp", null), new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int i) {

                }
            });


            wifiP2pManager.addLocalService(wifiP2pChannel,
                    WifiP2pUpnpServiceInfo.newInstance(), new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure(int i) {

                        }
                    });

        }else if(view == btnDiscoverService){

        }
    }

    public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>{

        @Override
        public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DeviceViewHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.item_device, null)
            );
        }

        @Override
        public void onBindViewHolder(DeviceViewHolder holder, int position) {
            if(position == 0){
                if(groupCreated){
                    holder.tvDevice.setText("groupName:" + groupName + "(" + groupPass + ")");
                    return;
                } else if(deviceList.size() == 0){
                    holder.tvDevice.setText("请扫描设备");
                    return;
                }
            }

            holder.tvDevice.setText(deviceList.get(position).deviceName);
            holder.tvDevice.setTag(deviceList.get(position));
            holder.tvDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return deviceList.size() + 1;
        }

        public class DeviceViewHolder extends RecyclerView.ViewHolder{

            TextView tvDevice;

            public DeviceViewHolder(View itemView) {
                super(itemView);
                tvDevice = (TextView) itemView.findViewById(R.id.tv_device);
            }
        }
    }
}
