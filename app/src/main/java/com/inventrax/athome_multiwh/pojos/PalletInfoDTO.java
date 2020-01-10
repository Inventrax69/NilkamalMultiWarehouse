package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class PalletInfoDTO {

    @SerializedName("PalletCode")
    private String palletCode;
    @SerializedName("PalletId")
    private String palletId;
    @SerializedName("PalletVolume")
    private String palletVolume;
    @SerializedName("NoOfBoxesLoaded")
    private String noOfBoxesLoaded;
    @SerializedName("LoadedVolume")
    private String loadedVolume;
    @SerializedName("PalletMaxWeight")
    private String palletMaxWeight;
    @SerializedName("LoadedWeight")
    private String loadedWeight;


    public PalletInfoDTO(){

    }


    public PalletInfoDTO(Set<? extends Map.Entry<?, ?>> entries)
    {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "PalletCode":
                    if(entry.getValue()!=null) {
                        this.setPalletCode(entry.getValue().toString());
                    }
                    break;
                case "PalletId":
                    if(entry.getValue()!=null) {
                        this.setPalletId(entry.getValue().toString());
                    }
                    break;
                case "PalletVolume":
                    if(entry.getValue()!=null) {
                        this.setPalletVolume(entry.getValue().toString());
                    }
                    break;
                case "NoOfBoxesLoaded":
                    if(entry.getValue()!=null) {
                        this.setNoOfBoxesLoaded(entry.getValue().toString());
                    }
                    break;
                case "LoadedVolume":
                    if(entry.getValue()!=null) {
                        this.setLoadedVolume(entry.getValue().toString());
                    }
                    break;
                case "PalletMaxWeight":
                    if(entry.getValue()!=null) {
                        this.setPalletMaxWeight(entry.getValue().toString());
                    }
                    break;
                case "LoadedWeight":
                    if(entry.getValue()!=null) {
                        this.setLoadedWeight(entry.getValue().toString());
                    }
                    break;

            }
        }
    }


    public String getPalletCode() {
        return palletCode;
    }

    public void setPalletCode(String palletCode) {
        this.palletCode = palletCode;
    }

    public String getPalletId() {
        return palletId;
    }

    public void setPalletId(String palletId) {
        this.palletId = palletId;
    }

    public String getPalletVolume() {
        return palletVolume;
    }

    public void setPalletVolume(String palletVolume) {
        this.palletVolume = palletVolume;
    }

    public String getNoOfBoxesLoaded() {
        return noOfBoxesLoaded;
    }

    public void setNoOfBoxesLoaded(String noOfBoxesLoaded) {
        this.noOfBoxesLoaded = noOfBoxesLoaded;
    }

    public String getLoadedVolume() {
        return loadedVolume;
    }

    public void setLoadedVolume(String loadedVolume) {
        this.loadedVolume = loadedVolume;
    }

    public String getPalletMaxWeight() {
        return palletMaxWeight;
    }

    public void setPalletMaxWeight(String palletMaxWeight) {
        this.palletMaxWeight = palletMaxWeight;
    }

    public String getLoadedWeight() {
        return loadedWeight;
    }

    public void setLoadedWeight(String loadedWeight) {
        this.loadedWeight = loadedWeight;
    }
}
