package br.com.vaniltoncoroa.questao2;

import java.util.Objects;

public class PessoaComCpf {
    private String cpf;
    private String nome;
    private int idade;
    private String email;

    public PessoaComCpf(String cpf, String nome, int idade, String email) {
        this.cpf = cpf;
        this.nome = nome;
        this.idade = idade;
        this.email = email;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (objeto == null || getClass() != objeto.getClass()) return false;

        PessoaComCpf pessoa = (PessoaComCpf) objeto;
        return Objects.equals(cpf, pessoa.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getIdade() { return idade; }
    public void setIdade(int idade) { this.idade = idade; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return String.format("Pessoa{cpf='%s', nome='%s', idade=%d, email='%s'}",
                cpf, nome, idade, email);
    }
}
