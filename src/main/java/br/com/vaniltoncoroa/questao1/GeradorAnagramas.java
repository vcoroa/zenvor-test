package br.com.vaniltoncoroa.questao1;

import java.util.*;

/**
 * Classe responsável por gerar todos os anagramas possíveis
 * de um conjunto de letras distintas.
 *
 * Utiliza o algoritmo de backtracking para explorar todas as
 * permutações possíveis das letras fornecidas.
 */
public class GeradorAnagramas {


    /**
     * Gera todos os anagramas possíveis de um array de letras distintas.
     *
     * Algoritmo:
     * 1. Valida a entrada para garantir que contém apenas letras distintas
     * 2. Inicializa estruturas de controle (resultado, marcadores de uso)
     * 3. Chama método recursivo para construir os anagramas
     *
     * @param letras Array de caracteres contendo letras distintas
     * @return Lista com todos os anagramas possíveis
     * @throws IllegalArgumentException se entrada for inválida
     */
    public static List<String> gerarAnagramas(char[] letras) {

        // Verifica se a entrada é válida antes de processar
        validarEntrada(letras);

        // Inicializa lista que armazenará todos os anagramas encontrados
        List<String> resultado = new ArrayList<>();

        // Array para marcar quais letras já foram usadas na construção atual
        boolean[] usado = new boolean[letras.length];

        // StringBuilder para construir o anagrama atual de forma eficiente
        StringBuilder atual = new StringBuilder();

        // Inicia o processo recursivo de construção dos anagramas
        construirAnagramas(letras, usado, atual, resultado);
        return resultado;
    }

    /**
     * Método recursivo que constrói os anagramas usando backtracking.
     *
     * Lógica do backtracking:
     * 1. Se o anagrama atual tem o tamanho esperado, foi encontrado um resultado
     * 2. Caso contrário, tenta adicionar cada letra ainda não utilizada
     * 3. Para cada letra: marca como usada, adiciona ao anagrama, faz recursão
     * 4. Após a recursão, desfaz as alterações (backtrack) para testar próxima opção
     *
     * @param letras Array original com as letras disponíveis
     * @param usado Array que marca quais letras estão sendo usadas no momento
     * @param atual StringBuilder com o anagrama sendo construído
     * @param resultado Lista onde os anagramas completos são armazenados
     */
    private static void construirAnagramas(char[] letras, boolean[] usado,
                                           StringBuilder atual, List<String> resultado) {

        // Caso base: se o anagrama atual tem o mesmo tamanho do array original,
        // significa que usamos todas as letras e encontramos um anagrama completo
        if (atual.length() == letras.length) {
            resultado.add(atual.toString());
            return;
        }

        // Percorre todas as letras disponíveis para tentar cada uma na posição atual
        for (int i = 0; i < letras.length; i++) {

            // Se a letra na posição i ainda não foi usada neste anagrama
            if (!usado[i]) {
                // Marca a letra como usada
                usado[i] = true;

                // Adiciona a letra ao anagrama atual
                atual.append(letras[i]);

                // Chamada recursiva para construir o restante do anagrama
                construirAnagramas(letras, usado, atual, resultado);

                // BACKTRACK: desfaz as alterações para testar próxima possibilidade
                // Remove a última letra adicionada
                atual.deleteCharAt(atual.length() - 1);

                // Marca a letra como não usada novamente
                usado[i] = false;
            }
        }
    }

    /**
     * Valida se a entrada atende aos requisitos:
     * - Não pode ser nula ou vazia
     * - Deve conter apenas letras
     * - Todas as letras devem ser distintas (não pode haver duplicatas)
     *
     * @param letras Array a ser validado
     * @throws IllegalArgumentException se alguma validação falhar
     */
    private static void validarEntrada(char[] letras) {

        // Verifica se a entrada é nula ou vazia
        if (letras == null || letras.length == 0) {
            throw new IllegalArgumentException("Entrada não pode ser nula ou vazia");
        }

        // Verifica se todos os caracteres são letras
        for (char c : letras) {
            if (!Character.isLetter(c)) {
                throw new IllegalArgumentException("Apenas letras são permitidas: " + c);
            }
        }

        // Verifica se todas as letras são distintas
        // Usa Set para detectar duplicatas (Set não permite elementos repetidos)
        Set<Character> letrasUnicas = new HashSet<>();
        for (char c : letras) {

            // add() retorna false se o elemento já existir no Set
            if (!letrasUnicas.add(Character.toLowerCase(c))) {
                throw new IllegalArgumentException("Letras devem ser distintas: " + c);
            }
        }
    }

    /**
     * Método principal para demonstrar o funcionamento da classe.
     * Gera e exibe todos os anagramas do conjunto {a, b, c}.
     */
    public static void main(String[] args) {

        // Define um conjunto de letras para teste
        char[] letras = {'a', 'b', 'c'};

        // Gera todos os anagramas possíveis
        List<String> anagramas = gerarAnagramas(letras);

        // Exibe o resultado
        System.out.println("Anagramas de " + Arrays.toString(letras) + ":");
        anagramas.forEach(System.out::println);
    }
}