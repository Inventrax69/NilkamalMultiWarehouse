package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class PrintResponce {

    @SerializedName("Status")
    private Boolean Status;
    @SerializedName("Message")
    private String Message;
    @SerializedName("RequestType")
    private String RequestType;

    public PrintResponce (Set<? extends Map.Entry<?, ?>> entries)
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

                case "RequestType":
                    if(entry.getValue()!=null) {
                        this.setRequestType(entry.getValue().toString());
                    }
                    break;

            }
        }
    }

    public Boolean getStatus() {
        return Status;
    }

    public void setStatus(Boolean status) {
        Status = status;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getRequestType() {
        return RequestType;
    }

    public void setRequestType(String requestType) {
        RequestType = requestType;
    }
}
