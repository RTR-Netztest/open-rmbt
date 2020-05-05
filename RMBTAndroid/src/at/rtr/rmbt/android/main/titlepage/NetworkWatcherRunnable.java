/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
 * Copyright 2015 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package at.rtr.rmbt.android.main.titlepage;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import at.rtr.rmbt.android.util.Helperfunctions;
import at.rtr.rmbt.android.util.net.NetworkUtil;

public class NetworkWatcherRunnable implements Runnable {

	public static interface OnActiveNetworkChangeListener {
		void onChange(final Set<InetAddress> oldInterfaceSet, final Set<InetAddress> newInterfaceSet, final String oldSsid, final String newSsid);
	}
	
	private final Activity activity;
	private NetworkInfo activeNetworkInfo;
	private String ssid;
	private Set<InetAddress> ipSet;
	
	private final List<OnActiveNetworkChangeListener> listenerList = new ArrayList<OnActiveNetworkChangeListener>();
	
	
	public NetworkWatcherRunnable(final Activity activity) {
		this.activity = activity;
	}
	
	public void addListener(final OnActiveNetworkChangeListener listener) {
		if (!listenerList.contains(listener)) {
			listenerList.add(listener);
		}
	}
	
	/**
	 * true if the list was modified, otherwise false
	 * @param listener
	 * @return
	 */
	public boolean removeListener(final OnActiveNetworkChangeListener listener) {
		return listenerList.remove(listener);
	}
	
	@Override
	public void run() {
		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		String currentSsid = null;
		
		if (ni != null) {

			if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
				TelephonyManager telManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
				currentSsid = telManager.getNetworkOperatorName();
			}
			else {
				WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();

				//Android 8.1 will return dummy values if location access is not enabled
				//see https://developer.android.com/reference/android/net/wifi/WifiManager#getConnectionInfo()
				if (wifiInfo.getSSID() != null &&
						wifiInfo.getBSSID() != null &&
						wifiInfo.getSSID().equals("<unknown ssid>") &&
						wifiInfo.getBSSID().equals("02:00:00:00:00:00")) {
					return;
				}
				else {
					currentSsid = String.valueOf(Helperfunctions.removeQuotationsInCurrentSSIDForJellyBean(wifiInfo.getSSID()));
				}
			}
		}
		
		Set<InetAddress> currentIpSet = null;
				
		try {
			currentIpSet = NetworkUtil.getAllInterfaceIpAddresses();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if ((activeNetworkInfo == null && ni != null) || (activeNetworkInfo != null && ni == null) 
				|| (activeNetworkInfo != null && !activeNetworkInfo.equals(ni)) || (currentSsid != null && !currentSsid.equals(ssid)) 
				|| (currentIpSet != null && !currentIpSet.equals(ipSet))) {
			
			if (ssid == null || !ssid.equals(currentSsid)) {			
				//active network has changed
				activeNetworkInfo = ni;
				if (activeNetworkInfo != null && activeNetworkInfo.isConnected() && currentSsid != null) {
					for (OnActiveNetworkChangeListener listener : listenerList) {
						listener.onChange(ipSet, currentIpSet, ssid, currentSsid);
					}
				}
				
				ipSet = currentIpSet;
				ssid = currentSsid;
			}
		}
	}
}
