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


package at.rtr.rmbt.db;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RadioCell {

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

        @JsonCreator
        public static Technology forValue(String value) {
            switch (value) {
                case "2G":
                    return CONNECTION_2G;
                case "3G":
                    return CONNECTION_3G;
                case "4G":
                    return CONNECTION_4G;
                case "5G":
                    return CONNECTION_5G;
                case "WLAN":
                    return CONNECTION_WLAN;
                default:
                    return null;
            }
        }

        @Override
        @JsonValue
        public String toString() {
            return val;
        }
    };

    private Integer mnc;
    private Integer mcc;

    private Long locationId;
    private Integer areaCode;

    private Integer primaryScramblingCode;
    private Integer channelNumber;
    private UUID uuid;

    private Technology technology;
    private Boolean registered;
    private Boolean active;

    private UUID openTestUuid;

    public Integer getMnc() {
        return mnc;
    }

    public void setMnc(Integer mnc) {
        this.mnc = mnc;
    }

    public Integer getMcc() {
        return mcc;
    }

    public void setMcc(Integer mcc) {
        this.mcc = mcc;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Integer getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Integer areaCode) {
        this.areaCode = areaCode;
    }

    public Integer getPrimaryScramblingCode() {
        return primaryScramblingCode;
    }

    public void setPrimaryScramblingCode(Integer primaryScramblingCode) {
        this.primaryScramblingCode = primaryScramblingCode;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Technology getTechnology() {
        return technology;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }

    public Boolean isRegistered() {
        return registered;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public UUID getOpenTestUuid() {
        return openTestUuid;
    }

    public void setOpenTestUuid(UUID openTestUuid) {
        this.openTestUuid = openTestUuid;
    }

    public Integer getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(Integer channelNumber) {
        this.channelNumber = channelNumber;
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
        sb.append(getPrimaryScramblingCode());
        return sb.toString();
    }
}
