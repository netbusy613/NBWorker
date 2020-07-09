/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netbusy613.nbworker.packop;

import com.netbusy613.nbworker.PackOP;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class PackOPImpl1 extends PackOP{

    @Override
    public void doing(String json) {
        Object o = new Date();
        synchronized(o){
            try {
                o.wait(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PackOPImpl1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("pack op doing "+json);
    }
    
}
