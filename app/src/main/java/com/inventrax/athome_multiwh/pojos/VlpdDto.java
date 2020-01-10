package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class VlpdDto {

    @SerializedName("ID")
    private String iD;

    @SerializedName("VLPDNumber")
    private String vLPDNumber;


    @SerializedName("Result")
    private String result;

    @SerializedName("PickedPalletNumber")
    private String pickedPalletNumber;

    @SerializedName("Location")
    private String location;

    @SerializedName("RSNNumber")
    private String rSNNumber;

    @SerializedName("Quantity")
    private String quantity;


    @SerializedName("Type")
    private String type;

    @SerializedName("OBDNumber")
    private String OBDNumber;
    @SerializedName("BoxNumber")
    private String BoxNumber;


    public VlpdDto() {
    }


    public VlpdDto(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "ID":
                    if (entry.getValue() != null) {
                        this.setiD(entry.getValue().toString());
                    }
                    break;

                case "VLPDNumber":
                    if (entry.getValue() != null) {
                        this.setvLPDNumber(entry.getValue().toString());
                    }
                    break;

                case "Result":
                    if (entry.getValue() != null) {
                        this.setResult(entry.getValue().toString());
                    }
                    break;

                case "PickedPalletNumber":
                    if (entry.getValue() != null) {
                        this.setPickedPalletNumber(entry.getValue().toString());
                    }
                    break;
                case "RSNNumber":
                    if (entry.getValue() != null) {
                        this.setrSNNumber(entry.getValue().toString());
                    }
                    break;

                case "Quantity":
                    if (entry.getValue() != null) {
                        this.setQuantity(entry.getValue().toString());
                    }
                    break;
                case "Type":
                    if (entry.getValue() != null) {
                        this.setType(entry.getValue().toString());
                    }
                    break;
                case "OBDNumber":
                    if (entry.getValue() != null) {
                        this.setOBDNumber(entry.getValue().toString());
                    }
                    break;
                case "BoxNumber":
                    if (entry.getValue() != null) {
                        this.setBoxNumber(entry.getValue().toString());
                    }
                    break;
            }
        }
    }

    public String getOBDNumber() {
        return OBDNumber;
    }

    public void setOBDNumber(String OBDNumber) {
        this.OBDNumber = OBDNumber;
    }

    public String getBoxNumber() {
        return BoxNumber;
    }

    public void setBoxNumber(String boxNumber) {
        BoxNumber = boxNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getiD() {
        return iD;
    }

    public void setiD(String iD) {
        this.iD = iD;
    }

    public String getvLPDNumber() {
        return vLPDNumber;
    }

    public void setvLPDNumber(String vLPDNumber) {
        this.vLPDNumber = vLPDNumber;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPickedPalletNumber() {
        return pickedPalletNumber;
    }

    public void setPickedPalletNumber(String pickedPalletNumber) {
        this.pickedPalletNumber = pickedPalletNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getrSNNumber() {
        return rSNNumber;
    }

    public void setrSNNumber(String rSNNumber) {
        this.rSNNumber = rSNNumber;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
