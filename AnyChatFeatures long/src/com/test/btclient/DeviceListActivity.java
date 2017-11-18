/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.btclient;

import java.util.Set;

import com.example.anychatfeatures.R;

import io.vov.vitamio.utils.Log;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class DeviceListActivity extends Activity {
    // 调试用
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    // 返回时数据标签
    public static String EXTRA_DEVICE_ADDRESS = "设备地址";

    // 成员域
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    class Fathe1r_1 extends BroadcastReceiver{
        String pin = "8888";  //此处为你要连接的蓝牙设备的初始密钥，一般为1234或0000  
        public void BluetoothReceiver() {  
              
        }  
      
        //广播接收器，当远程蓝牙设备被发现时，回调函数onReceiver()会被执行   
        @Override  
        public void onReceive(Context context, Intent intent) {  
              
            String action = intent.getAction(); //得到action  
            Log.e("action1=", action);  
            BluetoothDevice btDevice=null;  //创建一个蓝牙device对象  
             // 从Intent中获取设备对象  
            btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);   
              
            if(BluetoothDevice.ACTION_FOUND.equals(action)){  //发现设备  
                Log.e("发现设备:", "["+btDevice.getName()+"]"+":"+btDevice.getAddress());  
                  
                if(btDevice.getName().contains("HC-06"))//HC-05设备如果有多个，第一个搜到的那个会被尝试。  
                {  
                    if (btDevice.getBondState() == BluetoothDevice.BOND_NONE) {    
                          
                        Log.e("ywq", "attemp to bond:"+"["+btDevice.getName()+"]");  
                        try {  
                            //通过工具类ClsUtils,调用createBond方法  
                            ClsUtils.createBond(btDevice.getClass(), btDevice);  
                        } catch (Exception e) {  
                            // TODO Auto-generated catch block  
                            e.printStackTrace();  
                        }  
                    }  
                }else  
                    Log.e("error", "Is faild");  
            }else if(action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) //再次得到的action，会等于PAIRING_REQUEST  
            {  
                Log.e("action2=", action);  
                if(btDevice.getName().contains("HC-06"))  
                {  
                    Log.e("here", "OKOKOK");  
                      
                    try {  
                          
                        //1.确认配对  
                        ClsUtils.setPairingConfirmation(btDevice.getClass(), btDevice, true);  
                        //2.终止有序广播  
                        Log.i("order...", "isOrderedBroadcast:"+isOrderedBroadcast()+",isInitialStickyBroadcast:"+isInitialStickyBroadcast());  
                        abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。  
                        //3.调用setPin方法进行配对...  
                        boolean ret = ClsUtils.setPin(btDevice.getClass(), btDevice, pin);  
                          
                    } catch (Exception e) {  
                        // TODO Auto-generated catch block  
                        e.printStackTrace();  
                    }  
                }else  
                    Log.e("提示信息", "这个设备不是目标蓝牙设备");  
                  
            }  
            }
        // 选择设备响应函数 
        private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
                // 准备连接设备，关闭服务查找
                mBtAdapter.cancelDiscovery();

                // 得到mac地址
                String info = ((TextView) v).getText().toString();
                String address = info.substring(info.length() - 17);

                // 设置返回数据
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                // 设置返回值并结束程序
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        };
        }  
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 创建并显示窗口
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  //设置窗口显示模式为窗口方式
        setContentView(R.layout.device_list);

        // 设定默认返回值为取消
        setResult(Activity.RESULT_CANCELED);

        // 设定扫描按键响应
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
               v.setVisibility(View.GONE);
            }
        });

        // 初使化设备存储数组
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // 设置已配队设备列表
        
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // 设置新查找设备列表
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // 注册接收查找到设备action接收器
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // 注册查找结束action接收器
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // 得到本地蓝牙句柄
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // 得到已配对蓝牙设备列表
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // 添加已配对设备到列表并显示 
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
       } else {
            String noDevices = "No devices have been paired";
            mPairedDevicesArrayAdapter.add(noDevices);
       }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 关闭服务查找
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // 注销action接收器
        this.unregisterReceiver(mReceiver);
    }
    
    public void OnCancel(View v){
    	finish();
    }
    /**
     * 开始服务和设备查找
     */
    private void doDiscovery() {
       //if (D) Log.d(TAG, "doDiscovery()");

        // 在窗口显示查找中信息
        setProgressBarIndeterminateVisibility(true);
        setTitle("查找设备中...");

        // 显示其它设备（未配对设备）列表
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // 关闭再进行的服务查找
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        //并重新开始
        mBtAdapter.startDiscovery();
    }

    // 选择设备响应函数 
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // 准备连接设备，关闭服务查找
            mBtAdapter.cancelDiscovery();

            // 得到mac地址
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // 设置返回数据
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // 设置返回值并结束程序
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // 查找到设备和搜索完成action监听器
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // 查找到设备action
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 得到蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 如果是已配对的则略过，已得到显示，其余的在添加到列表中进行显示
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }else{  //添加到已配对设备列表
                	mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // 搜索完成action
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle("选择要连接的设备");
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = "没有找到新设备";
                    mNewDevicesArrayAdapter.add(noDevices);
                }
             //   if(mPairedDevicesArrayAdapter.getCount() > 0)
              //  	findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            }
        }
    };


}
