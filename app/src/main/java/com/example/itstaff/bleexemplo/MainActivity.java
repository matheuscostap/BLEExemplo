package com.example.itstaff.bleexemplo;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private ScanCallback scanCallback;
    private BluetoothGatt gatt;

    private ListView lvDevices;
    private Button btnStart;
    private Button btnStop;
    private ArrayList<BluetoothDevice> devices;
    private DeviceAdapter adapter;
    private ProgressBar progressBar;

    private Set<BluetoothDevice> btDevices;

    private static final int REQUEST_ENABLE_BLUETOOTH = 10;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE = 20;

    private static final UUID APPLE_MEDIA_SERVICES = UUID.fromString("89d3502b-0f36-433a-8ef4-c502ad55f8dc");
    private static final UUID APPLE_CONTINUITY_SERVICE = UUID.fromString("d0611e78-bbb4-4591-a5f8-487910ae4366");
    private static final UUID APPLE_NOTIFICATION_CENTER_SERVICE = UUID.fromString("7905f431-b5ce-4e99-a40f-4b1e122d00d0");


    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvDevices = (ListView) findViewById(R.id.lvDevices);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        //Handler
        handler = new Handler(getMainLooper());

        //ListView
        devices = new ArrayList<>();
        adapter = new DeviceAdapter(this,devices);
        lvDevices.setAdapter(adapter);
        lvDevices.setChoiceMode(AbsListView.CHOICE_MODE_NONE);

        btDevices = new HashSet<>();

        //Inicializando o Bluetooth Adapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //Scan Callback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallback = new ScanCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);


                    if (result.getDevice() != null){
                        /*String deviceInfo;
                        String deviceAddress = result.getDevice().getAddress();
                        String deviceName;
                        if (result.getDevice().getName() != null){
                            deviceName = result.getDevice().getName();
                        }else {
                            deviceName = "NULL";
                        }
                        deviceInfo = deviceAddress + " - " + deviceName;
                        if (!devices.contains(deviceInfo)){
                            devices.add(deviceInfo);
                            adapter.notifyDataSetChanged();
                        }*/
                        BluetoothDevice device = result.getDevice();
                        if (!devices.contains(device)) {
                            devices.add(device);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    //connect(result.getDevice());
                    Log.i("SCAN","ACHOU: " + result.getDevice().getAddress());
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    Log.i("SCAN","SCAN FAILED");
                }
            };
        }else{
            //Incompativel
        }


        //Verifica se o Bluetooth esta ativo
        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent enbaleBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enbaleBluetooth,REQUEST_ENABLE_BLUETOOTH);
        }


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });


        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScan();
            }
        });

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connect(adapter.getItem(position));
            }
        });
    }


    private void startScan(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            scanner.startScan(scanCallback);
            devices.clear();
            adapter.notifyDataSetChanged();
        }else {
            //Incompativel
        }
        progressBar.setVisibility(View.VISIBLE);
    }


    private void stopScan(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner.stopScan(scanCallback);
        }
        progressBar.setVisibility(View.INVISIBLE);
    }


    private void connect(final BluetoothDevice device){
        handler.post(new Runnable() {
            @Override
            public void run() {
                //if (device.getAddress().equals("49:75:C6:2B:D6:50")){
                    Log.i("gattCallback","Tentando conectar em: " + device.getAddress());
                    gatt = device.connectGatt(MainActivity.this,true,gattCallback);
                Log.i("gattCallback","connect getService: " + gatt.getServices().toString());
                    stopScan();
                //}
            }
        });
    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("gattCallback","Status: " + status);
            switch (newState){
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback","CONNECTED: " + newState);
                    try {Thread.sleep(500);} catch (Throwable e) {e.printStackTrace();}
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("gattCallback","DISCONNECTED: " + newState);
                    gatt.disconnect();
                    gatt.close();
                    break;
                default:
                    Log.i("gattCallback","OUTRO ESTADO: " + newState);
                    gatt.disconnect();
                    gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS){
                Log.i("BluetoothLeService","SUCCESS: " + gatt.getDevice().getAddress());
            }else{
                Log.w("BluetoothLeService","onServiceDiscovered received: " + status);
            }

            List<BluetoothGattService> services = gatt.getServices();

            Log.i("gattCallback","Services: " + services.toString());

            for (BluetoothGattService service : services){
                Log.i("gattCallback","Service uuid: " + service.getUuid().toString());
            }

            List<BluetoothGattCharacteristic> gattCharacteristic = new ArrayList<>();

            //for (int i = 0; i < services.size(); i++){
            for (int i = services.size() -1; i >= 0; i--){
                gattCharacteristic.addAll(services.get(i).getCharacteristics());
            }

            for (int i = 0; i < gattCharacteristic.size(); i++){
                gatt.readCharacteristic(gattCharacteristic.get(i));
                try {Thread.sleep(500);} catch (Throwable e) {e.printStackTrace();}
                //Log.i("gattCallback","Characteristics: " + gattCharacteristic.get(i).getValue());
            }

            //BluetoothGattCharacteristic gattCharacteristic = services.get(1).getCharacteristics().get(0);
            //gatt.readCharacteristic(services.get(0).getCharacteristics().get(0));
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            byte[] charValue = characteristic.getValue();

            String info = new String(charValue, StandardCharsets.UTF_8);
            String info2 = new String(charValue, StandardCharsets.UTF_16);
            String info3 = new String(charValue, StandardCharsets.ISO_8859_1);
            String info4 = new String(charValue, StandardCharsets.US_ASCII);
            String info5 = new String(charValue, StandardCharsets.UTF_16BE);
            String info6 = new String(charValue, StandardCharsets.UTF_16LE);

            //for (int i = 0; i < charValue.length; i++){
            //    byte flag = charValue[i];
                Log.i("gattCallback","Characteristic: " + characteristic.getUuid() + " VALUE: " + /*flag*/ info + " SERVICE: " + characteristic.getService().getUuid());
                Log.i("gattCallback","Characteristic: " + characteristic.getUuid() + " VALUE: " + /*flag*/ info2 + " SERVICE: " + characteristic.getService().getUuid());
                Log.i("gattCallback","Characteristic: " + characteristic.getUuid() + " VALUE: " + /*flag*/ info3 + " SERVICE: " + characteristic.getService().getUuid());
                Log.i("gattCallback","Characteristic: " + characteristic.getUuid() + " VALUE: " + /*flag*/ info4 + " SERVICE: " + characteristic.getService().getUuid());
                Log.i("gattCallback","Characteristic: " + characteristic.getUuid() + " VALUE: " + /*flag*/ info5 + " SERVICE: " + characteristic.getService().getUuid());
                Log.i("gattCallback","Characteristic: " + characteristic.getUuid() + " VALUE: " + /*flag*/ info6 + " SERVICE: " + characteristic.getService().getUuid());
            //}

            //gatt.disconnect();
            //gatt.close();
        }
    };
}
