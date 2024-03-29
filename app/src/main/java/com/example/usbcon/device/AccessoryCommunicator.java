package com.example.usbcon.device;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.usbcon.UsbUtils.AccessoryUtils;
import com.example.usbcon.UsbUtils.LogUtils;
//import com.example.usbcon.host.HostChatActivity.CommunicationRunnable;

public abstract class AccessoryCommunicator {

    private Context context;
    private final AtomicBoolean running = new AtomicBoolean(true);
    CommunicationThread thread;
    
    private AccessoryUtils accessoryUtils;

    public AccessoryCommunicator(final Context mContext) {
        this.context = mContext;
        accessoryUtils = new AccessoryUtils(context);
        thread = new CommunicationThread();        
//        if(accessoryUtils.openAccessory(accessoryUtils.searchAccessory())) {
//        	onConnected();
//        	thread.start();        	
//        } else {
//        	onError("cann't connect,\nplease exit host apk and device apk and then open it again");
//        	onDisconnected();
//        }
        searchDevice();
        	
    }
    
    public void searchDevice(){
    	new Thread(){
			@Override
			public void run() {
				while(true){
					if(accessoryUtils.openAccessory(accessoryUtils.searchAccessory())) {
			        	onConnected();
			        	thread.start();
			        	return;
			        } else {
//			        	onError("cann't connect,\nplease exit host apk and device apk and then open it again");
			        	onDisconnected();
			        }
					try {
						Thread.sleep(3*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} 
			}
    		
    	}.start();
    	
    }
    
    public void send(byte[] payload) {
        accessoryUtils.sendData(payload);
    }
    public abstract void onReceive(final byte[] payload, final int length);
    public abstract void onError(String msg);
    public abstract void onConnected();
    public abstract void onDisconnected();
    
    public void stop() {
    	running.set(false);
    	accessoryUtils.closeAccessory();
    	LogUtils.d("stop");
    }

    private class CommunicationThread extends Thread {
        @Override
        public void run() {
            while (running.get()) {
            	byte[] bytes = accessoryUtils.receiveData();
            	if(bytes != null) {
            		onReceive(bytes, bytes.length);
            	}
            }
        }
    }
}