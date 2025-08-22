package br.com.vaniltoncoroa.questao2;

import java.util.HashSet;
import java.util.Set;

public class TesteComparacao {

    public static void main(String[] args) {
        System.out.println("=== TESTE COM CPF (Identificador Único) ===");
        testarPessoaComCPF();

        System.out.println("\n=== TESTE SEM CPF (Todos os Atributos) ===");
        testarPessoaSemCPF();
    }

    private static void testarPessoaComCPF() {
        PessoaComCpf joao1 = new PessoaComCpf("123.456.789-00", "João Silva", 30, "joao@email.com");
        PessoaComCpf joao2 = new PessoaComCpf("123.456.789-00", "João Santos", 35, "joao.santos@email.com");
        PessoaComCpf maria = new PessoaComCpf("987.654.321-00", "Maria Silva", 30, "maria@email.com");

        System.out.println("João 1: " + joao1);
        System.out.println("João 2: " + joao2);
        System.out.println("Maria:  " + maria);

        System.out.println("\nComparações:");
        System.out.println("joao1.equals(joao2): " + joao1.equals(joao2)); // true - mesmo CPF
        System.out.println("joao1.equals(maria): " + joao1.equals(maria)); // false - CPF diferente

        Set<PessoaComCpf> pessoas = new HashSet<>();
        pessoas.add(joao1);
        pessoas.add(joao2); // Não será adicionado - mesmo CPF
        pessoas.add(maria);

        System.out.println("Tamanho do Set: " + pessoas.size()); // 2
        System.out.println("Pessoas no Set:");
        pessoas.forEach(System.out::println);
    }

    private static void testarPessoaSemCPF() {
        PessoaSemCpf joao1 = new PessoaSemCpf("João Silva", 30, "joao@email.com");
        PessoaSemCpf joao2 = new PessoaSemCpf("João Silva", 30, "joao@email.com");
        PessoaSemCpf joao3 = new PessoaSemCpf("João Silva", 35, "joao@email.com");

        System.out.println("João 1: " + joao1);
        System.out.println("João 2: " + joao2);
        System.out.println("João 3: " + joao3);

        System.out.println("\nComparações:");
        System.out.println("joao1.equals(joao2): " + joao1.equals(joao2)); // true - todos iguais
        System.out.println("joao1.equals(joao3): " + joao1.equals(joao3)); // false - idade diferente

        Set<PessoaSemCpf> pessoas = new HashSet<>();
        pessoas.add(joao1);
        pessoas.add(joao2); // Não será adicionado - dados idênticos
        pessoas.add(joao3); // Será adicionado - idade diferente

        System.out.println("Tamanho do Set: " + pessoas.size()); // 2
        System.out.println("Pessoas no Set:");
        pessoas.forEach(System.out::println);
    }
}
