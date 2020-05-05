/*******************************************************************************
 * Copyright 2017 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
 ******************************************************************************/
package at.rtr.rmbt.android.util;

import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.telephony.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.UUID;

import static at.rtr.rmbt.android.util.InformationCollector.NETWORK_WIFI;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CellInformationWrapper {
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public enum Technology {
        CONNECTION_2G("2G"),
        CONNECTION_3G("3G"),
        CONNECTION_4G("4G"),
        CONNECTION_5G("5G"),
        CONNECTION_WLAN("WLAN");

        private String val;

        Technology(String val) {
            this.val = val;
        }

        @JsonValue
        public String toString() {
            return val;
        }
    };

    private Technology technology;
    private CellIdentity ci;
    private CellSignalStrength cs;
    private Boolean registered;
    private Boolean active;
    private long timeStamp;
    private long fallbackTimestamp;

    //start timestamp of the test, set in InformationCollector
    private Long startTimestampNsSinceBoot;
    private Long startTimestampNsNanotime;
    private Long timeLast;


    public CellInformationWrapper(CellInfo cellInfo) {
        setRegistered(cellInfo.isRegistered());
        //a time stamp in nanos since boot according do to documentation (see below)
        this.setTimeStamp(cellInfo.getTimeStamp());

        //fallback timestamp, should be almost identical
        this.setFallbackTimestamp(SystemClock.elapsedRealtimeNanos());

        if (cellInfo.getClass().equals(CellInfoLte.class)) {
            setTechnology(Technology.CONNECTION_4G);
            this.ci = new CellIdentity(((CellInfoLte) cellInfo).getCellIdentity());
            this.cs = new CellSignalStrength(((CellInfoLte) cellInfo).getCellSignalStrength());
        }
        else if (cellInfo.getClass().equals(CellInfoWcdma.class)) {
            setTechnology(Technology.CONNECTION_3G);
            this.ci = new CellIdentity(((CellInfoWcdma) cellInfo).getCellIdentity());
            this.cs = new CellSignalStrength(((CellInfoWcdma) cellInfo).getCellSignalStrength());
        }
        else if (cellInfo.getClass().equals(CellInfoGsm.class)) {
            setTechnology(Technology.CONNECTION_2G);
            this.ci = new CellIdentity(((CellInfoGsm) cellInfo).getCellIdentity());
            this.cs = new CellSignalStrength(((CellInfoGsm) cellInfo).getCellSignalStrength());
        }
        else if (cellInfo.getClass().equals(CellInfoCdma.class)) {
            setTechnology(Technology.CONNECTION_2G);
            this.ci = new CellIdentity(((CellInfoCdma) cellInfo).getCellIdentity());
            this.cs = new CellSignalStrength(((CellInfoCdma) cellInfo).getCellSignalStrength());
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                cellInfo.getClass().equals(CellInfoNr.class)) {
            setTechnology(Technology.CONNECTION_5G);
            this.ci = new CellIdentity((CellIdentityNr) ((CellInfoNr) cellInfo).getCellIdentity());
            this.cs = new CellSignalStrength((CellSignalStrengthNr) ((CellInfoNr) cellInfo).getCellSignalStrength());
        }
    }

    public CellInformationWrapper(WifiInfo wifiInfo) {
        setTechnology(Technology.CONNECTION_WLAN);
        this.setTimeStamp(SystemClock.elapsedRealtimeNanos());
        this.setFallbackTimestamp(SystemClock.elapsedRealtimeNanos());
        setRegistered(true);

        this.ci = new CellIdentity(wifiInfo);
        this.cs = new CellSignalStrength(wifiInfo);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class CellSignalStrength {
        private Integer signal;
        private Integer timingAdvance;
        private Integer rsrp;
        private Integer rsrq;
        private Integer rssnr;
        private Integer cqi;
        private Integer bitErrorRate;
        private Integer linkSpeed;
        private Integer networkTypeId;

        private String cellUuid; //reference to a specific cell

        public CellSignalStrength(CellSignalStrengthLte ss) {
            String desc = ss.toString();
            setRsrp(getSignalStrengthValueFromDescriptionString(desc,"rsrp"));
            setRsrq(getSignalStrengthValueFromDescriptionString(desc,"rsrq"));
            setRssnr(getSignalStrengthValueFromDescriptionString(desc,"rssnr"));
            setCqi(getSignalStrengthValueFromDescriptionString(desc,"cqi"));
            setTimingAdvance(ss.getTimingAdvance());
            //setSignal(ss.getDbm());
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public CellSignalStrength(CellSignalStrengthNr ss) {
            String desc = ss.toString();
            setRsrp(ss.getSsRsrp());
            setRsrq(ss.getSsRsrq());
            setRssnr(ss.getSsSinr());
            //setSignal(ss.getDbm());
        }


        public CellSignalStrength(CellSignalStrengthWcdma ss) {
            setSignal(ss.getDbm());
            String desc = ss.toString();
            setSignal(ss.getDbm());
            setBitErrorRate(getSignalStrengthValueFromDescriptionString(desc, "ber"));
        }

        public CellSignalStrength(CellSignalStrengthCdma ss) {
            setSignal(ss.getDbm());
        }

        public CellSignalStrength(CellSignalStrengthGsm ss) {
            setSignal(ss.getDbm());
            String desc = ss.toString();
            setSignal(ss.getDbm());
            setBitErrorRate(getSignalStrengthValueFromDescriptionString(desc, "ber"));
            setTimingAdvance(getSignalStrengthValueFromDescriptionString(desc, "mTa"));
        }

        public CellSignalStrength(WifiInfo wifiInfo) {
            setSignal(wifiInfo.getRssi());
            setLinkSpeed(wifiInfo.getLinkSpeed());
            setNetworkTypeId(NETWORK_WIFI);
        }

        private Integer getSignalStrengthValueFromDescriptionString(String description, String field) {
            int index = description.indexOf(field + "=");
            if (index >= 0) {
                description = description.substring(index + field.length() + 1);
                int ret = Integer.parseInt(description.split(" ")[0]);
                return maxIntToUnknown(ret);
            }
            else {
                return null;
            }
        }

        private Integer maxIntToUnknown(int value) {
            if (objectsEquals(value, Integer.MAX_VALUE)) {
                return null;
            }
            return value;
        }

        public Integer getSignal() {
            //some devices return invalid values (#913)
            if (signal != null &&
                    (signal >= 0 || signal < -140)) {
                return null;
            }
            return signal;
        }

        public void setSignal(Integer signal) {
            this.signal = signal;
        }

        @JsonProperty("timing_advance")
        public Integer getTimingAdvance() {
            if (timingAdvance == null || objectsEquals(timingAdvance, Integer.MAX_VALUE)) {
                return null;
            }
            // plausibility check (some devices report -1 or 65535 etc)
            if (timingAdvance < 0 ||
                    timingAdvance > 2182) // validity range according to 3GPP TS 36.213
                return null;
            else
                return timingAdvance;
        }

        public void setTimingAdvance(Integer timingAdvance) {
            this.timingAdvance = timingAdvance;
        }

        @JsonProperty("lte_rsrp")
        public Integer getRsrp() {
            //some devices return invalid values (#913)
            if (rsrp != null &&
                    (rsrp >= 0 || rsrp < -140 || (rsrq != null && rsrq == -1))) {
                return null;
            }
            return rsrp;
        }

        public void setRsrp(Integer rsrp) {
            this.rsrp = rsrp;
        }

        @JsonProperty("lte_rsrq")
        public Integer getRsrq() {
            if (rsrq == null) {
                return null;
            }

            // fix invalid rsrq values (see #913)
            if (Math.abs(rsrq) > 19.5 || Math.abs(rsrq) < 3.0) {
                return null;
            }
            // fix invalid rsrq values for some devices (see #996)
            if (rsrq > 0) {
                return -rsrq;
            }
            return rsrq;
        }

        public void setRsrq(Integer rsrq) {
            this.rsrq = rsrq;
        }

        @JsonProperty("lte_rssnr")
        public Integer getRssnr() {
            return rssnr;
        }

        public void setRssnr(Integer rssnr) {
            this.rssnr = rssnr;
        }

        @JsonProperty("lte_cqi")
        public Integer getCqi() {
            return cqi;
        }

        public void setCqi(Integer cqi) {
            this.cqi = cqi;
        }

        @JsonProperty("cell_uuid")
        public String getCellUuid() {
            if (this.cellUuid != null) {
                return this.cellUuid;
            }
            else {
                return getCi().getCellUuid();
            }
        }

        public void setCellUuid(String cellUuid) {
            this.cellUuid = cellUuid;
        }

        @JsonProperty("bit_error_rate")
        public Integer getBitErrorRate() {
            if (objectsEquals(bitErrorRate,99)) {
                return null;
            }
            return bitErrorRate;
        }

        public void setBitErrorRate(Integer bitErrorRate) {
            this.bitErrorRate = bitErrorRate;
        }

        @JsonProperty("wifi_link_speed")
        public Integer getLinkSpeed() {
            return linkSpeed;
        }

        public void setLinkSpeed(Integer linkSpeed) {
            this.linkSpeed = linkSpeed;
        }

        @JsonProperty("time_ns")
        public Long getTimeStampNs() {
            return CellInformationWrapper.this.getTimeStampNs();
        }

        @JsonProperty("time_ns_last")
        public Long getTimeStampNsLast() {
            return CellInformationWrapper.this.getTimeStampLast();
        }

        @JsonProperty("network_type_id")
        public Integer getNetworkTypeId() {
            return networkTypeId;
        }

        public void setNetworkTypeId(Integer networkTypeId) {
            this.networkTypeId = networkTypeId;
        }

        //@JsonProperty("wifi_rssi")
        //@TODO: Wifi_rssi

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !obj.getClass().equals(this.getClass())) {
                return super.equals(obj);
            }

            CellSignalStrength ss = (CellSignalStrength) obj;

            boolean equals = true;
            equals &= objectsEquals(this.getSignal(), ss.getSignal());
            equals &= objectsEquals(this.getBitErrorRate(), ss.getBitErrorRate());
            equals &= objectsEquals(this.getCqi(), ss.getCqi());
            equals &= objectsEquals(this.getLinkSpeed(), ss.getLinkSpeed());
            equals &= objectsEquals(this.getRsrp(), ss.getRsrp());
            equals &= objectsEquals(this.getRsrq(), ss.getRsrq());
            equals &= objectsEquals(this.getRssnr(), ss.getRssnr());
            equals &= objectsEquals(this.getTimingAdvance(), ss.getTimingAdvance());

            return equals;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("bitErrorRate=");
            sb.append(getBitErrorRate());
            sb.append(" cqpi=");
            sb.append(getCqi());
            sb.append(" linkSpeed=");
            sb.append(getLinkSpeed());
            sb.append(" rsrp=");
            sb.append(getRsrp());
            sb.append(" rsrq=");
            sb.append(getRsrq());
            sb.append(" rssnr=");
            sb.append(getRssnr());
            sb.append(" signal=");
            sb.append(getSignal());
            sb.append(" timingA=");
            sb.append(getTimingAdvance());
            return sb.toString();
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class CellIdentity {
        private Integer channelNumber;
        private Integer mnc;
        private Integer mcc;
        private long locationId;
        private Long areaCode;
        private Integer scramblingCode;
        private String cellUuid = UUID.randomUUID().toString();


        public CellIdentity(CellIdentityGsm cellIdentity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.setChannelNumber(cellIdentity.getArfcn());
            }
            this.setMnc(cellIdentity.getMnc());
            this.setMcc(cellIdentity.getMcc());
            this.setLocationId(cellIdentity.getLac());

            /* CID Either 16-bit GSM Cell Identity described in
            * TS 27.007, 0..65535, Integer.MAX_VALUE if unknown */
            this.setAreaCode(cellIdentity.getCid());

            /* 6-bit Base Station Identity Code, Integer.MAX_VALUE if unknown */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.setScramblingCode(cellIdentity.getBsic());
            }
        }

        public CellIdentity(CellIdentityCdma cellIdentity) {
            this.setLocationId(cellIdentity.getBasestationId());
        }

        public CellIdentity(CellIdentityWcdma cellIdentity) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.setChannelNumber(cellIdentity.getUarfcn());
            }
            this.setMnc(cellIdentity.getMnc());
            this.setMcc(cellIdentity.getMcc());
            this.setLocationId(cellIdentity.getLac());
            this.setAreaCode(cellIdentity.getCid());
            this.setScramblingCode(cellIdentity.getPsc());
        }

        public CellIdentity(CellIdentityLte cellIdentity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.setChannelNumber(cellIdentity.getEarfcn());
            }
            this.setMnc(cellIdentity.getMnc());
            this.setMcc(cellIdentity.getMcc());
            this.setLocationId(cellIdentity.getTac());
            this.setAreaCode(cellIdentity.getCi());
            this.setScramblingCode(cellIdentity.getPci());
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public CellIdentity(CellIdentityNr cellIdentity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.setChannelNumber(cellIdentity.getNrarfcn());
            }

            try {
                this.setMnc(Integer.parseInt(cellIdentity.getMncString()));
                this.setMcc(Integer.parseInt(cellIdentity.getMccString()));
            } catch (NumberFormatException e) {
                //todo
            }
            this.setLocationId(cellIdentity.getNci());
            this.setAreaCode(cellIdentity.getTac());
            this.setScramblingCode(cellIdentity.getPci());
        }

        public CellIdentity(WifiInfo wifiInfo) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setChannelNumber(wifiInfo.getFrequency());
            }
        }

        @JsonProperty("channel_number")
        public Integer getChannelNumber() {
            if (objectsEquals(channelNumber,Integer.MAX_VALUE)) {
                return null;
            }
            //#883: some devices return earfcn 0/uarfcn 0 for null
            if (objectsEquals(channelNumber, 0)) {
                return null;
            }
            return channelNumber;
        }

        public void setChannelNumber(int channelNumber) {
            this.channelNumber = channelNumber;
        }

        @JsonProperty("mnc")
        public Integer getMnc() {
            if (mnc == null ||
                    objectsEquals(mnc, Integer.MAX_VALUE) ||
                    mnc <= 0 ||
                    mnc > 999) {
                return null;
            }
            return mnc;
        }

        public void setMnc(int mnc) {
            this.mnc = mnc;
        }

        @JsonProperty("mcc")
        public Integer getMcc() {
            if (mcc == null ||
                    objectsEquals(mcc, Integer.MAX_VALUE) ||
                    mcc < 0 ||
                    mcc > 999) {
                return null;
            }
            return mcc;
        }

        public void setMcc(int mcc) {
            this.mcc = mcc;
        }

        /**
         *
         * 16-bit Location Area Code, 0..65535, Integer.MAX_VALUE if unknown
         * 16-bit Tracking Area Code, Integer.MAX_VALUE if unknown
         *
         * @return
         */
        @JsonProperty("location_id")
        public Long getLocationId() {
            if (objectsEquals(locationId, Integer.MAX_VALUE)) {
                return null;
            }
            return locationId;
        }

        public void setLocationId(long locationId) {
            this.locationId = locationId;
        }

        @JsonProperty("area_code")
        public Long getAreaCode() {
            if (objectsEquals(areaCode, Integer.MAX_VALUE) ||
                    objectsEquals(areaCode, -1)) {
                return null;
            }
            return areaCode;
        }

        public void setAreaCode(long areaCode) {
            this.areaCode = areaCode;
        }

        @JsonProperty("primary_scrambling_code")
        public Integer getScramblingCode() {
            if (objectsEquals(scramblingCode, Integer.MAX_VALUE)) {
                return null;
            }
            return scramblingCode;
        }

        public void setScramblingCode(Integer scramblingCode) {
            this.scramblingCode = scramblingCode;
        }

        @JsonProperty("uuid")
        public String getCellUuid() {
            return this.cellUuid;
        }

        @JsonProperty("technology")
        public Technology getCITechnology() {
            return getTechnology();
        }

        @JsonProperty("registered")
        public Boolean isRegistered() {
            return CellInformationWrapper.this.isRegistered();
        }

        @JsonProperty
        public Boolean isActive() {
            if (objectsEquals(registered, false)) {
                return false;
            }
            return active;
        }

        @JsonIgnore
        public boolean isEmpty() {
            return this.getAreaCode() == null &&
                    this.getChannelNumber() == null &&
                    this.getCITechnology() == null &&
                    this.getLocationId() == null &&
                    this.getMcc() == null &&
                    this.getMnc() == null &&
                    this.getScramblingCode() == null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !obj.getClass().equals(this.getClass())) {
                return super.equals(obj);
            }

            CellIdentity ci = (CellIdentity) obj;

            boolean equals = true;
            equals &= objectsEquals(this.getAreaCode(), ci.getAreaCode());
            equals &= objectsEquals(this.getChannelNumber(), ci.getChannelNumber());
            equals &= objectsEquals(this.getLocationId(), ci.getLocationId());
            equals &= objectsEquals(this.getMcc(), ci.getMcc());
            equals &= objectsEquals(this.getMnc(), ci.getMnc());
            equals &= objectsEquals(this.getScramblingCode(), ci.getScramblingCode());
            equals &= objectsEquals(getTechnology(),(ci.getCITechnology()));

            return equals;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("AreaCode=");
            sb.append(getAreaCode());
            sb.append(" ChannelNumber=");
            sb.append(getChannelNumber());
            sb.append(" LocId=");
            sb.append(getLocationId());
            sb.append(" Mcc=");
            sb.append(getMcc());
            sb.append(" Mnc=");
            sb.append(getMnc());
            sb.append(" Scrambling=");
            sb.append(getScramblingCode());
            return sb.toString();
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
    }


    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public Boolean isRegistered() {
        return registered;
    }

    public Technology getTechnology() {
        return technology;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getFallbackTimestamp() {
        return fallbackTimestamp;
    }

    public void setFallbackTimestamp(long fallbackTimestamp) {
        this.fallbackTimestamp = fallbackTimestamp;
    }

    @Override
    public boolean equals(Object obj) {
        String a1 = obj.getClass().toString();
        String a2 = this.getClass().toString();
        if (obj == null || !obj.getClass().equals(this.getClass())) {
            return super.equals(obj);
        }

        CellInformationWrapper ci = (CellInformationWrapper) obj;

        boolean equals = true;
        equals &= this.getTimeStamp() == ci.getTimeStamp();
        equals &= this.getTechnology() == ci.getTechnology();
        equals &= ci.getCi().equals(this.getCi());
        equals &= ci.getCs().equals(this.getCs());

        return equals;
    }

    @JsonProperty("cellIdentity")
    public CellIdentity getCi() {
        return ci;
    }

    @JsonProperty("cellSignalStrength")
    public CellSignalStrength getCs() {
        return cs;
    }

    @JsonIgnore
    public Long getStartTimestampNsSinceBoot() {
        return startTimestampNsSinceBoot;
    }

    public void setStartTimestampNsSinceBoot(Long startTimestampNsSinceBoot) {
        this.startTimestampNsSinceBoot = startTimestampNsSinceBoot;
    }

    @JsonIgnore
    public Long getStartTimestampNsNanotime() {
        return startTimestampNsNanotime;
    }

    public void setStartTimestampNsNanotime(Long startTimestampNsNanotime) {
        this.startTimestampNsNanotime = startTimestampNsNanotime;
    }

    @JsonProperty("time_ns_last")
    public Long getTimeStampLast() {
        //always based on nanotime
        if (startTimestampNsNanotime != null && this.timeLast != null) {
            return this.timeLast - startTimestampNsNanotime;
        }
        return null;
    }

    public void setTimeStampLast(Long timeLast) {
        this.timeLast = timeLast;
    }


    /**
     * Relative timestamp compared with the beginning of the test
     * NULL if startTimestampNs is not set
     * @return
     */
    @JsonProperty("time_ns")
    public Long getTimeStampNs() {
        //in theory, timestamps for cells should always return
        //nano since boot (https://developer.android.com/reference/android/telephony/CellInfo.html#getTimeStamp())
        //however, in practice, some seem to return a nano timestamp based on System.nanoTime() in some cases

        if (startTimestampNsNanotime != null && startTimestampNsSinceBoot != null) {
            long diffBasedOnNanotime = this.timeStamp - startTimestampNsNanotime;
            long diffBasedOnNsSinceBoot = this.timeStamp - startTimestampNsSinceBoot;

            long probableTimestamp = (Math.abs(diffBasedOnNanotime) < Math.abs(diffBasedOnNsSinceBoot)) ? diffBasedOnNanotime : diffBasedOnNsSinceBoot;

            //additionally, compare to fallback timestamp based on time the EventHandler was invoked
            long diffBasedOnFallback = this.fallbackTimestamp - startTimestampNsSinceBoot;

            //if they are not within 60sec, use the "accurate" timestamp, otherwise, fall back
            if (Math.abs(Math.abs(diffBasedOnFallback) - Math.abs(probableTimestamp)) > 60*1e9) {
                return diffBasedOnFallback;
            }

            return probableTimestamp;
        }

        if (startTimestampNsSinceBoot != null) {
            return this.timeStamp - startTimestampNsSinceBoot;
        }
        return null;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTechnology());
        sb.append(" isRegistered=" + isRegistered());
        sb.append(" timeStamp=" + getTimeStamp());
        sb.append(" [cellSignal] " + getCs().toString());
        sb.append(" [cellIdentity] " + getCi().toString());
        return sb.toString();
    }


    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public static boolean objectsEquals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
