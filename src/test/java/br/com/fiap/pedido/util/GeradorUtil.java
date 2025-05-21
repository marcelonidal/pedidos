package br.com.fiap.pedido.util;

import java.util.Random;

public class GeradorUtil {

    public static String gerarCpfValido() {
        Random r = new Random();
        int n1 = r.nextInt(10);
        int n2 = r.nextInt(10);
        int n3 = r.nextInt(10);
        int n4 = r.nextInt(10);
        int n5 = r.nextInt(10);
        int n6 = r.nextInt(10);
        int n7 = r.nextInt(10);
        int n8 = r.nextInt(10);
        int n9 = r.nextInt(10);

        int d1 = n9*2 + n8*3 + n7*4 + n6*5 + n5*6 + n4*7 + n3*8 + n2*9 + n1*10;
        d1 = 11 - (d1 % 11);
        if (d1 >= 10) d1 = 0;

        int d2 = d1*2 + n9*3 + n8*4 + n7*5 + n6*6 + n5*7 + n4*8 + n3*9 + n2*10 + n1*11;
        d2 = 11 - (d2 % 11);
        if (d2 >= 10) d2 = 0;

        return "" + n1 + n2 + n3 + n4 + n5 + n6 + n7 + n8 + n9 + d1 + d2;
    }

    public static String gerarNumeroCartaoValido() {
        Random random = new Random();
        int[] digits = new int[16];

        // Gera os 15 primeiros dígitos aleatoriamente (exceto o último)
        for (int i = 0; i < 15; i++) {
            digits[i] = random.nextInt(10);
        }

        // Calcula o dígito verificador (Luhn)
        int soma = 0;
        for (int i = 0; i < 15; i++) {
            int num = digits[14 - i];
            if (i % 2 == 0) {
                num *= 2;
                if (num > 9) num -= 9;
            }
            soma += num;
        }

        digits[15] = (10 - (soma % 10)) % 10;

        // Concatena os 16 dígitos em uma String
        StringBuilder sb = new StringBuilder();
        for (int d : digits) {
            sb.append(d);
        }

        return sb.toString();
    }

}
