package com.stillcoolme.algorithm.divideconquer;

/**
 * @author: create by stillcoolme
 * @date: 2019/6/30 16:18
 * @description:
 *  Implement pow(x, n), which calculates x raised to the power n (xn).
 **/
public class _50_PowXN {

    /**
     * 递归的方式
     * 时间复杂度：O(logn)。因为每次 n 都被减半。
     *
     * @param x
     * @param n
     * @return
     */
    public double myPow(double x, int n) {
        // 为了处理 变态的 n = -2147483648的情况，需要将n转成long
        long N = n;
        if (N < 0) {
            x = 1 / x;
            N = -N;
        }
        return fastPow(x, N);
    }
    public double fastPow(double x, long n) {
        if(n == 0) return 1.0;
        double result = fastPow(x, n / 2);
        if(n % 2 == 0){
            return result * result;
        } else {
            return result * result * x;
        }

        // 这样写居然会溢出，把 double result = fastPow(x, n / 2); 提出来就不会了。
        /*
        if(n % 2 == 0){
            double result = fastPow(x, n / 2);
            return result * result;
        } else {
            double result = fastPow(x, (n - 1) / 2);
            return result * result * x;
        }*/
    }


    /**
     * 非递归的方式
     * @param x
     * @param n
     * @return
     */
    public double myPow2(double x, int n) {



        return 0;
    }

    public static void main(String[] args) {
        _50_PowXN powXN = new _50_PowXN();
        //double x = powXN.myPow(2, -2147483648);
        double x = powXN.myPow(2, 10);
        System.out.println(x);
    }

}
