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
public abstract class PackOP {
    protected int duration = 60000;//60000毫秒

    public int getDuration() {
        return duration;
    }
    
    public PackOP(int duration){
        this.duration = duration;
    }
    public PackOP(){
    }
    public abstract void doing(String json);
}
