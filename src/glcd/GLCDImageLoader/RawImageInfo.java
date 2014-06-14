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
public class RawImageInfo {

    private ArrayList<Integer> data;
    private int size;
    private String name;

    public RawImageInfo() {
        this.data = new ArrayList<Integer>();
        this.size = 0;
        this.name = "";
    }

    public RawImageInfo(ArrayList<Integer> data, int size, String name) {
        this.data = data;
        this.size = size;
        this.name = name;
    }

    public int[] getDataAsArray(int startIndex) {
        int arraySize = data.size() - startIndex;
        int[] rd = new int[arraySize];

        for (int i = startIndex; i < data.size(); i++) {
            rd[i-startIndex] = data.get(i).intValue();
        }

        return rd;
    }

    public ArrayList<Integer> getData() {
        return data;
    }

    public void setData(ArrayList<Integer> rawData) {
        this.data = rawData;
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
