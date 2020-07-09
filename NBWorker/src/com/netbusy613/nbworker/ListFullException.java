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
public class ListFullException extends Exception{

    @Override
    public String toString() {
        return "ListFullException{超过了缓存区的最大数量限制，抛弃！}";
    }
    
}
