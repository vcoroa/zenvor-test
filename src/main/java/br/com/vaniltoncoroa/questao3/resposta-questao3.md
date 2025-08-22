# Questão 3 - Design Pattern para Desacoplamento de Biblioteca Externa

## Problema Comum
Você está usando uma biblioteca para logging (como Log4j) e no futuro pode precisar trocar por outra (como Logback ou SLF4J). Como fazer essa troca sem quebrar todo o código?

## Solução: Adapter Pattern

### Exemplo prático - Sistema de Log

#### Situação atual: usando Log4j diretamente
```java
// PROBLEMA: código acoplado à biblioteca específica
import org.apache.log4j.Logger;

public class ServicoUsuario {
    private static final Logger logger = Logger.getLogger(ServicoUsuario.class);
    
    public void criarUsuario(String nome) {
        logger.info("Criando usuário: " + nome);
        // lógica...
        logger.error("Erro ao criar usuário");
    }
}
```

#### Solução com Adapter Pattern

**1. Interface própria do sistema:**
```java
public interface MeuLogger {
    void info(String mensagem);
    void erro(String mensagem);
    void debug(String mensagem);
}
```

**2. Adapter para Log4j:**
```java
import org.apache.log4j.Logger;

public class AdapterLog4j implements MeuLogger {
    private final Logger logger;
    
    public AdapterLog4j(Class<?> clazz) {
        this.logger = Logger.getLogger(clazz);
    }
    
    @Override
    public void info(String mensagem) {
        logger.info(mensagem);
    }
    
    @Override
    public void erro(String mensagem) {
        logger.error(mensagem);
    }
    
    @Override
    public void debug(String mensagem) {
        logger.debug(mensagem);
    }
}
```

**3. Adapter para Logback (biblioteca alternativa):**
```java
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class AdapterLogback implements MeuLogger {
    private final Logger logger;
    
    public AdapterLogback(Class<?> clazz) {
        this.logger = (Logger) LoggerFactory.getLogger(clazz);
    }
    
    @Override
    public void info(String mensagem) {
        logger.info(mensagem);
    }
    
    @Override
    public void erro(String mensagem) {
        logger.error(mensagem);
    }
    
    @Override
    public void debug(String mensagem) {
        logger.debug(mensagem);
    }
}
```

**4. Factory para facilitar a troca:**
```java
public class LoggerFactory {
    private static final String TIPO_LOGGER = System.getProperty("logger.type", "log4j");
    
    public static MeuLogger criarLogger(Class<?> clazz) {
        return switch (TIPO_LOGGER) {
            case "logback" -> new AdapterLogback(clazz);
            case "log4j" -> new AdapterLog4j(clazz);
            default -> new AdapterLog4j(clazz);
        };
    }
}
```

**5. Uso no código (totalmente desacoplado):**
```java
public class ServicoUsuario {
    private final MeuLogger logger = LoggerFactory.criarLogger(ServicoUsuario.class);
    
    public void criarUsuario(String nome) {
        logger.info("Criando usuário: " + nome);
        // lógica...
        logger.erro("Erro ao criar usuário");
    }
}
```

## Vantagens do Adapter Pattern

### ✅ **Baixo acoplamento**
- O código principal não conhece detalhes das bibliotecas externas
- Mudança de biblioteca não afeta a lógica de negócio

### ✅ **Facilidade de troca**
- Trocar Log4j por Logback é apenas mudar configuração
- Não precisa alterar código que usa logging

### ✅ **Interface consistente**
- Todas as bibliotecas de logging seguem a mesma interface
- Desenvolvedor sempre usa os mesmos métodos

### ✅ **Testabilidade**
- Fácil criar mocks da interface `MeuLogger`
- Testes unitários não dependem de bibliotecas externas

## Limitações

### ❌ **Camada adicional**
- Mais código para manter
- Possível overhead de performance (mínimo)

### ❌ **Pode ocultar funcionalidades**
- Recursos específicos da biblioteca podem não estar disponíveis
- Interface comum pode ser "menor denominador comum"

### ❌ **Mapeamento de funcionalidades**
- Necessário adaptar métodos entre bibliotecas
- Algumas funcionalidades podem se perder na tradução

## Exemplo de troca de biblioteca

Para trocar de Log4j para Logback:

**1. Mudar propriedade do sistema:**
```bash
logger.type=logback
```

**2. Zero alterações no código de negócio** - continua funcionando igual!