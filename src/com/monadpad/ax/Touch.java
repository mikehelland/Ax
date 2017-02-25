package com.monadpad.ax;

/**
 * User: m
 * Date: 7/1/13
 * Time: 2:29 PM
 */
public class Touch {
    int onFret;
    int onString;
    float x;
    float y;
    int id;
    int channelId;

    Touch(float x, float y, int id){
        this.x = x;
        this.y = y;
        this.id = id;
    }

    int fretMapping(int[][] fretMap) {
        return fretMapping(onString,  onFret, fretMap);
    }

    static int fretMapping(int onString, int onFret, int[][] fretMap) {
        int string =Math.max(0, Math.min(fretMap.length - 1, onString));
        int fret =Math.max(0, Math.min(fretMap[0].length - 1, onFret));
        //return 12 + fretMap[string][fret];
        return fretMap[string][fret];
    }

}
