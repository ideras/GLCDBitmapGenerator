/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package glcd.GLCDImageLoader;

import java.util.ArrayList;

/**
 *
 * @author ideras
 */
public class RawImageData {
    private ArrayList<Integer> rawData;
    private int size;
    private String name;

    public RawImageData() {
        this.rawData = new ArrayList<Integer>();
        this.size = 0;
        this.name = "";
    }

    
    public RawImageData(ArrayList<Integer> rawData, int size, String name) {
        this.rawData = rawData;
        this.size = size;
        this.name = name;
    }

    public ArrayList<Integer> getRawData() {
        return rawData;
    }

    public void setRawData(ArrayList<Integer> rawData) {
        this.rawData = rawData;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
