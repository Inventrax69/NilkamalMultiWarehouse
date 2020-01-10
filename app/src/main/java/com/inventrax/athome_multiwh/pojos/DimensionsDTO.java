package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class DimensionsDTO {


    @SerializedName("Length")
    private String length;
    @SerializedName("Breadth")
    private String breadth;
    @SerializedName("Height")
    private String height;
    @SerializedName("Weight")
    private String weight;


    public DimensionsDTO(){

    }

    public DimensionsDTO(Set<? extends Map.Entry<?, ?>> entries)
    {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "Length":
                    if(entry.getValue()!=null) {
                        this.setLength(entry.getValue().toString());
                    }
                    break;
                case "Breadth":
                    if(entry.getValue()!=null) {
                        this.setBreadth(entry.getValue().toString());
                    }
                    break;
                case "Height":
                    if(entry.getValue()!=null) {
                        this.setHeight(entry.getValue().toString());
                    }
                    break;
                case "Weight":
                    if(entry.getValue()!=null) {
                        this.setWeight(entry.getValue().toString());
                    }
                    break;

            }
        }
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getBreadth() {
        return breadth;
    }

    public void setBreadth(String breadth) {
        this.breadth = breadth;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
