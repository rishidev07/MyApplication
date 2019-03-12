package com.example.www.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneURL;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;
import com.neovisionaries.bluetooth.ble.advertising.TxPowerLevel;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter ba;
    ScanCallback cb;
    String bus_name="OnBoard_";
    int route_no=1;
    String message="";
    int i=49;
    ArrayAdapter<String> adapter;
    ArrayList<String> devices=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ba=BluetoothAdapter.getDefaultAdapter();
        final BluetoothLeScanner bluetoothLeScanner=ba.getBluetoothLeScanner();


        adapter =new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,devices);
        ListView list=findViewById(R.id.list);
        list.setAdapter(adapter);

        Button btn=findViewById(R.id.button);

        cb =new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Log.d("TAG", ""+result.getDevice().getName());
                //if(result.getDevice().getName()!=null) {
                    //if (result.getDevice().getName().equals(bus_name + route_no)) {
                        List<ADStructure> structures =
                                ADPayloadParser.getInstance().parse(result.getScanRecord().getBytes());
                        for (ADStructure structure : structures) {
                            EddystoneURL es = null;
                            if (structure.getClass().equals(EddystoneURL.class)) {
                                es = (EddystoneURL) structure;
                                URL url = es.getURL();
                                String s = url.toString();
                              if((int)s.charAt(8)==i) {
                                  message=message+s;
                                  Log.d("TAG", "message: "+message + i);
                                  i++;
                                  if(i==52){
                                      Log.d("TAG", "message: " + message);
                                      String str="";
                                      for (int j=9;j<message.length();){
                                          if(!(message.charAt(j)=='.')){
                                              str=str+message.charAt(j);
                                              j++;
                                          }
                                          if(message.charAt(j)=='.'){
                                              j=j+12;
                                          }
                                      }
                                      bluetoothLeScanner.stopScan(cb);
                                  }
                              }
                            }
                        }

//                    }
//                }
                devices.add(result.getDevice().getName());
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devices.clear();
                adapter.notifyDataSetChanged();
                bus_name=bus_name+route_no;
                checkBTPermissions();
                bluetoothLeScanner.startScan(cb);
            }
        });

    }

    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.d("TAG", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
}
