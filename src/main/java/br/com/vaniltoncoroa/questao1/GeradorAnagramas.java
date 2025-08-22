package br.com.vaniltoncoroa.questao1;

import java.util.*;

public class GeradorAnagramas {

    public static List<String> gerarAnagramas(char[] letras) {
        validarEntrada(letras);

        List<String> resultado = new ArrayList<>();
        boolean[] usado = new boolean[letras.length];
        StringBuilder atual = new StringBuilder();

        construirAnagramas(letras, usado, atual, resultado);
        return resultado;
    }

    private static void construirAnagramas(char[] letras, boolean[] usado,
                                           StringBuilder atual, List<String> resultado) {
        if (atual.length() == letras.length) {
            resultado.add(atual.toString());
            return;
        }

        for (int i = 0; i < letras.length; i++) {
            if (!usado[i]) {
                usado[i] = true;
                atual.append(letras[i]);

                construirAnagramas(letras, usado, atual, resultado);

                atual.deleteCharAt(atual.length() - 1);
                usado[i] = false;
            }
        }
    }

    private static void validarEntrada(char[] letras) {
        if (letras == null || letras.length == 0) {
            throw new IllegalArgumentException("Entrada não pode ser nula ou vazia");
        }

        for (char c : letras) {
            if (!Character.isLetter(c)) {
                throw new IllegalArgumentException("Apenas letras são permitidas: " + c);
            }
        }

        Set<Character> letrasUnicas = new HashSet<>();
        for (char c : letras) {
            if (!letrasUnicas.add(Character.toLowerCase(c))) {
                throw new IllegalArgumentException("Letras devem ser distintas: " + c);
            }
        }
    }

    public static void main(String[] args) {
        char[] letras = {'a', 'b', 'c'};
        List<String> anagramas = gerarAnagramas(letras);

        System.out.println("Anagramas de " + Arrays.toString(letras) + ":");
        anagramas.forEach(System.out::println);
    }
}