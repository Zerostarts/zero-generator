package com.star.acm;

import java.util.Scanner;

public class MainTemplate {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            int n = scanner.nextInt();
            // 处理n行数据
            int[] arr = new int[n];

            for (int i = 0; i < n; i++) {
                arr[i] = scanner.nextInt();
            }
            int sum = 0;
            for (int num :arr) {
                sum += num;
            }

            System.out.println("Sum is " + sum);
        }
        scanner.close();
    }
}
