/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netbusy613.nbworker;

import com.netbusy613.nbworker.packop.PackOPImpl1;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class Example {

    public static void main(String[] args) {
        NBPackManager manager = new NBPackManager();
        manager.setCheckduration(10000);
        manager.setCOUNT_MAXPACKS(50);
        manager.setCOUNT_THREADS(10);
        manager.start();
        while (true) {
            for (int i = 0; i < 50; i++) {
                NBpack pBpack = new NBpack(PackOPImpl1.class.getName(), String.valueOf(i),5000);
                try {
                    manager.addPack(pBpack);
                } catch (ListFullException ex) {
                    Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("--------------------new around!");
            synchronized (manager.waitControl) {
                try {
                    manager.waitControl.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
    }
}
