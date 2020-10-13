/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netbusy613.nbworker;


/**
 *
 * @author Administrator
 */
public class NBpack {

    protected final String FLAG;

    public String getFLAG() {
        return FLAG;
    }
    protected final String json;
    protected final int duration;
    
    /*
    @param flag Package FLAG
    @param json Package param
    */
    public NBpack(String flag,String json,int duration) {
        this.FLAG = flag;
        this.json = json;
        this.duration = duration;
    }
    
}
