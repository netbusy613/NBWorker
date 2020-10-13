/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netbusy613.nbworker;

/**
 *
 * @author netbusy613
 */
public class DurationTimeOutException extends Exception{
    @Override
    public String toString() {
        return "DurationTimeOutException{超过了包处理最长时间限制！}";
    }
}
