/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netbusy613.nbworker;

import java.util.Map;

/**
 *
 * @author Administrator
 */
public class NBWorkerRunable implements Runnable {

    private final NBPackManager manager;
    private final Object stateControl;
    private final Map<String, PackOP> registedOPImpl;
    private boolean running = false;
    private int id;

    public boolean isRunning() {
        return running;
    }

    public NBWorkerRunable(NBPackManager manager, Object stateControl,int id) {
        this.manager = manager;
        registedOPImpl = manager.registedOPImpl();
        this.stateControl = stateControl;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            synchronized (stateControl) {
                running = true;
                System.out.println("线程 "+id + " 已启动....");
                stateControl.notify();
            }
            synchronized (manager.control) {
                manager.control.wait();
            }
        } catch (InterruptedException ex) {
        }
        while (true) {
            NBpack pack = manager.readPack();
            if (pack != null) {
                PackOP op = registedOPImpl.get(pack.FLAG);
                if (op != null) {
                    op.doing(pack.json);
                } else {
                    System.err.println("不支持的PACK，未注册PACK");
                }
            } else {
                try {
                    System.out.println("线程 "+id + "休息....");
                    synchronized (manager.control) {
                        manager.control.wait();
                    }
                } catch (InterruptedException ex) {
                }
            }
        }
    }

}
