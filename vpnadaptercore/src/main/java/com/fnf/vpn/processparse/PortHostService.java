package com.fnf.vpn.processparse;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.fnf.vpn.VPNLog;
import com.fnf.vpn.nat.NatSession;
import com.fnf.vpn.nat.NatSessionManager;
import com.fnf.vpn.utils.VpnServiceHelper;

import java.util.List;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/5.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 *         管理并提供回话
 *
 */

public class PortHostService extends Service {
    private static final String ACTION = "action";
    private static final String TAG = "PortHostService";
    private static PortHostService instance;
    private boolean isRefresh = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NetFileManager.getInstance().init(getApplicationContext());
        instance = this;
    }

    public static PortHostService getInstance() {
        return instance;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public List<NatSession> getAndRefreshSessionInfo() {
        List<NatSession> allSession = NatSessionManager.getAllSession();
        refreshSessionInfo(allSession);
        return allSession;

    }

    public void refreshSessionInfo() {

        List<NatSession> allSession = NatSessionManager.getAllSession();
        refreshSessionInfo(allSession);


    }

    //刷新包名的信息的
    private void refreshSessionInfo(List<NatSession> netConnections) {
        if (isRefresh || netConnections == null) {
            return;
        }
        boolean needRefresh = false;
        for (NatSession connection : netConnections) {
            if (connection.appInfo == null) {
                needRefresh = true;
                break;
            }
        }
        if (!needRefresh) {
            return;
        }
        isRefresh = true;
        try {
            NetFileManager.getInstance().refresh();

            for (NatSession connection : netConnections) {
                if (connection.appInfo == null) {
                    int searchPort = connection.localPort & 0XFFFF;
                    Integer uid = NetFileManager.getInstance().getUid(searchPort);

                    if (uid != null) {
                        VPNLog.d(TAG, "can not find uid");
                        connection.appInfo = AppInfo.createFromUid(VpnServiceHelper.getContext(), uid);
                    }
                }
            }
        } catch (Exception e) {
            VPNLog.d(TAG,"failed to refreshSessionInfo "+e.getMessage());

        }

        isRefresh = false;

    }


    public static void startParse(Context context) {
        Intent intent = new Intent(context, PortHostService.class);
        context.startService(intent);
    }

    public static void stopParse(Context context) {
        Intent intent = new Intent(context, PortHostService.class);
        context.stopService(intent);
    }
}
