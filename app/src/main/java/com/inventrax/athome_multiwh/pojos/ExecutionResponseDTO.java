package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class ExecutionResponseDTO {

    @SerializedName("Status")
    private Boolean status;
    @SerializedName("Message")
    private String message;
    @SerializedName("ScannedQty")
    private String scannedQty;

    public ExecutionResponseDTO(){

    }


    public ExecutionResponseDTO(Set<? extends Map.Entry<?, ?>> entries)
    {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "Status":
                    if(entry.getValue()!=null) {
                        this.setStatus(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "Message":
                    if(entry.getValue()!=null) {
                        this.setMessage(entry.getValue().toString());
                    }
                    break;
                case "ScannedQty":
                    if(entry.getValue()!=null) {
                        this.setScannedQty(entry.getValue().toString());
                    }
                    break;

            }
        }
    }

    public String getScannedQty() {
        return scannedQty;
    }

    public void setScannedQty(String scannedQty) {
        this.scannedQty = scannedQty;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
