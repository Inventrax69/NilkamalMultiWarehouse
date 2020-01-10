package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class VLPDLoadingDTO {


    @SerializedName("Status")
    private Boolean status;
    @SerializedName("Message")
    private String message;
    @SerializedName("SetQty")
    private String setQty;
    @SerializedName("ScannedQty")
    private String scannedQty;
    @SerializedName("Location")
    private String location;
    @SerializedName("Type")
    private String type;
    @SerializedName("RSNNumber")
    private String RSNNumber;
    @SerializedName("Quantity")
    private String quantity;
    @SerializedName("TotalNoOfBoxes")
    private String totalNoOfBoxes;
    @SerializedName("NoofBoxesLoaded")
    private String noofBoxesLoaded;



    public VLPDLoadingDTO(){

    }


    public VLPDLoadingDTO(Set<? extends Map.Entry<?, ?>> entries)
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
                case "SetQty":
                    if(entry.getValue()!=null) {
                        this.setSetQty(entry.getValue().toString());
                    }
                    break;
                case "ScannedQty":
                    if(entry.getValue()!=null) {
                        this.setScannedQty(entry.getValue().toString());
                    }
                    break;
                case "Location":
                    if(entry.getValue()!=null) {
                        this.setLocation(entry.getValue().toString());
                    }
                    break;
                case "Type":
                    if(entry.getValue()!=null) {
                        this.setType(entry.getValue().toString());
                    }
                    break;
                case "RSNNumber":
                    if(entry.getValue()!=null) {
                        this.setRSNNumber(entry.getValue().toString());
                    }
                    break;
                case "Quantity":
                    if(entry.getValue()!=null) {
                        this.setQuantity(entry.getValue().toString());
                    }
                    break;
                case "TotalNoOfBoxes":
                    if(entry.getValue()!=null) {
                        this.setTotalNoOfBoxes(entry.getValue().toString());
                    }
                    break;
                case "NoofBoxesLoaded":
                    if(entry.getValue()!=null) {
                        this.setNoofBoxesLoaded(entry.getValue().toString());
                    }
                    break;


            }
        }
    }

    public String getNoofBoxesLoaded() {
        return noofBoxesLoaded;
    }

    public void setNoofBoxesLoaded(String noofBoxesLoaded) {
        this.noofBoxesLoaded = noofBoxesLoaded;
    }

    public String getTotalNoOfBoxes() {
        return totalNoOfBoxes;
    }

    public void setTotalNoOfBoxes(String totalNoOfBoxes) {
        this.totalNoOfBoxes = totalNoOfBoxes;
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

    public String getSetQty() {
        return setQty;
    }

    public void setSetQty(String setQty) {
        this.setQty = setQty;
    }

    public String getScannedQty() {
        return scannedQty;
    }

    public void setScannedQty(String scannedQty) {
        this.scannedQty = scannedQty;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRSNNumber() {
        return RSNNumber;
    }

    public void setRSNNumber(String RSNNumber) {
        this.RSNNumber = RSNNumber;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
