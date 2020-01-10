package com.inventrax.athome_multiwh.pojos;


import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class PrinterDetailsDTO{

    @SerializedName("ClientResourceName")
    private String clientResourceName;
    @SerializedName("DeviceIP")
    private String deviceIP;


    public PrinterDetailsDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "ClientResourceName":
                    if (entry.getValue() != null) {
                        this.setClientResourceName(entry.getValue().toString());
                    }
                    break;
                case "DeviceIP":
                    if (entry.getValue() != null) {
                        this.setDeviceIP(entry.getValue().toString());
                    }
                    break;

            }
        }
    }


    public String getClientResourceName() {
        return clientResourceName;
    }

    public void setClientResourceName(String clientResourceName) {
        this.clientResourceName = clientResourceName;
    }

    public String getDeviceIP() {
        return deviceIP;
    }

    public void setDeviceIP(String deviceIP) {
        this.deviceIP = deviceIP;
    }
}
