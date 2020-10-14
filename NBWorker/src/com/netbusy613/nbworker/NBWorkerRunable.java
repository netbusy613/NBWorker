/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netbusy613.nbworker;

import java.util.Date;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class NBWorkerRunable implements Runnable {

    public NBPackManager getManager() {
        return manager;
    }

    public int getId() {
        return id;
    }

    private final NBPackManager manager;
    private final Object stateControl;
    private final Map<String, PackOP> registedOPImpl;
    private boolean running = false;
    private boolean waitting = false;
    private boolean onop = false;
    private Date bt = null;
    private int duration = 0;

    private NBpack nowPack = null;

    public void checkDuration() throws DurationTimeOutException {

        if (bt != null) {
            Date now = new Date();
            int dur = (int) (now.getTime() - bt.getTime());
            if (duration != 0 && dur > duration) {
                System.err.println("线程" + id + "处理" + nowPack.json + "超时！ 用时=" + dur + "   限时=" + duration);
                throw new DurationTimeOutException();
            }
        }
    }

    public NBpack getNowPack() {
        return nowPack;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    // 判断线程是否在等待中
    public boolean isWaitting() {
        return running && waitting;
    }
    private int id;

    // 判断线程是否已运行
    public boolean isRunning() {
        return running;
    }

    public NBWorkerRunable(NBPackManager manager, Object stateControl, int id) {
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
                System.out.println("线程 " + id + " 已启动....");
                stateControl.notify();
            }
            synchronized (manager.control) {
                waitting = true;
                manager.updateWaitStatu();
                manager.control.wait();
                waitting = false;
            }
        } catch (InterruptedException ex) {
        }
        while (manager.goon) {
            waitting = false;
            NBpack pack = manager.readPack();
            if (pack != null) {
                PackOP op = registedOPImpl.get(pack.FLAG);
                if (op != null) {
                    onop = true;
                    bt = new Date();
                    nowPack = pack;
                    duration = pack.duration;
                    op.doing(pack.json);
                    bt = null;
                    onop = false;
                } else {
                    System.err.println("不支持的PACK，未注册PACK");
                }
            } else {
                try {
                    System.out.println("线程 " + id + "休息....");
                    synchronized (manager.control) {
                        waitting = true;
                        manager.updateWaitStatu();
                        manager.control.wait();
                        waitting = false;
                    }
                } catch (InterruptedException ex) {
                }
            }
        }
        // 线程运行完毕
        running = false;
    }
}
