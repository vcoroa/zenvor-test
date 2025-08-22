package br.com.vaniltoncoroa.questao2;

import java.util.Objects;

public class PessoaSemCpf {
    private String nome;
    private int idade;
    private String email;

    public PessoaSemCpf(String nome, int idade, String email) {
        this.nome = nome;
        this.idade = idade;
        this.email = email;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (objeto == null || getClass() != objeto.getClass()) return false;

        PessoaSemCpf pessoa = (PessoaSemCpf) objeto;
        return idade == pessoa.idade &&
                Objects.equals(nome, pessoa.nome) &&
                Objects.equals(email, pessoa.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, idade, email);
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getIdade() { return idade; }
    public void setIdade(int idade) { this.idade = idade; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return String.format("PessoaSemCPF{nome='%s', idade=%d, email='%s'}",
                nome, idade, email);
    }
}
