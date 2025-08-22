# Questão 2 - sobrescrta do método equals()

## Exemplo prático onde é necessário

Quando criamos uma classe `PessoaComCpf` e queremos comparar duas pessoas pelo CPF ao invés da referência na memória:

```java
public class PessoaComCpf {
    private String cpf;
    private String nome;
    
    public PessoaComCpf(String cpf, String nome) {
        this.cpf = cpf;
        this.nome = nome;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Pessoa pessoa = (Pessoa) obj;
        return Objects.equals(cpf, pessoa.cpf);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }
    
    // getters e setters...
}
```

## Por que precisamos disso?

Sem override do equals(), isso acontece:

```java
Pessoa p1 = new Pessoa("123.456.789-00", "João");
Pessoa p2 = new Pessoa("123.456.789-00", "João");

System.out.println(p1.equals(p2)); // false (compara referência)
```

Com override do equals():

```java
System.out.println(p1.equals(p2)); // true (compara CPF)
```

## Considerações importantes

### 1. Sempre implementar hashCode() junto
Se dois objetos são iguais pelo equals(), eles devem ter o mesmo hashCode():

```java
@Override
public int hashCode() {
    return Objects.hash(cpf); // mesmo campo usado no equals()
}
```

### 2. Verificações obrigatórias no equals()
- Verificar se é o mesmo objeto: `if (this == obj) return true;`
- Verificar se é null: `if (obj == null) return false;`
- Verificar se é da mesma classe: `if (getClass() != obj.getClass()) return false;`

### 3. Impacto em coleções
Com equals() correto, funciona em HashMap e HashSet:

```java
Set<PessoaComCpf> pessoas = new HashSet<>();
pessoas.add(new PessoaComCpf("123.456.789-00", "João"));
pessoas.add(new PessoaComCpf("123.456.789-00", "João Silva")); // não adiciona

System.out.println(pessoas.size()); // 1
```

## Regras que não podem ser quebradas

1. **Reflexiva**: `x.equals(x)` deve ser true
2. **Simétrica**: `x.equals(y)` = `y.equals(x)`
3. **Transitiva**: se `x.equals(y)` e `y.equals(z)`, então `x.equals(z)`
4. **Consistente**: múltiplas chamadas retornam o mesmo resultado
5. **Null**: `x.equals(null)` deve ser false

sempre lembrar que se fizer equals(), fazer hashCode() também usando os mesmos campos.