# Questão 5 - Prevenção de SQL Injection

## Técnicas para prevenir SQL Injection

SQL Injection é uma das vulnerabilidades mais críticas em aplicações web. Utilizo várias técnicas em camadas para garantir que o sistema esteja protegido contra esse tipo de ataque.

## 1. Prepared Statements / Parameterized Queries

### ❌ Código vulnerável (String concatenation)

```java
// NUNCA FAZER ISSO - Vulnerável a SQL Injection
@Repository
public class UsuarioRepositoryInseguro {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = '" + email + "'";
        // Vulnerável a: buscarPorEmail("'; DROP TABLE usuarios; --")
        return jdbcTemplate.queryForObject(sql, Usuario.class);
    }
    
    public List<Usuario> buscarPorNome(String nome) {
        String sql = "SELECT * FROM usuarios WHERE nome LIKE '%" + nome + "%'";
        // Vulnerável a: buscarPorNome("'; DELETE FROM usuarios; --")
        return jdbcTemplate.query(sql, new UsuarioRowMapper());
    }
}
```

### ✅ Código seguro (Prepared Statements)

```java
@Repository
public class UsuarioRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    // Usando prepared statements com JdbcTemplate
    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{email}, new UsuarioRowMapper());
    }
    
    // Usando named parameters (mais legível)
    public List<Usuario> buscarPorNome(String nome) {
        String sql = "SELECT * FROM usuarios WHERE nome LIKE :nome";
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("nome", "%" + nome + "%");
        
        return namedParameterJdbcTemplate.query(sql, params, new UsuarioRowMapper());
    }
    
    // Múltiplos parâmetros
    public List<Usuario> buscarPorIdadeECidade(int idadeMinima, String cidade) {
        String sql = "SELECT * FROM usuarios WHERE idade >= :idade AND cidade = :cidade";
        
        Map<String, Object> params = new HashMap<>();
        params.put("idade", idadeMinima);
        params.put("cidade", cidade);
        
        return namedParameterJdbcTemplate.query(sql, params, new UsuarioRowMapper());
    }
}
```

## 2. ORM com JPA/Hibernate

### JPQL com parâmetros nomeados

```java
@Repository
public class UsuarioJpaRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // JPQL com parâmetros nomeados - SEGURO
    public List<Usuario> buscarPorDominio(String dominio) {
        String jpql = "SELECT u FROM Usuario u WHERE u.email LIKE :pattern";
        
        return entityManager.createQuery(jpql, Usuario.class)
                .setParameter("pattern", "%" + dominio)
                .getResultList();
    }
    
    // Query com múltiplos parâmetros
    public List<Usuario> buscarPorCriterios(String nome, Integer idade, Boolean ativo) {
        StringBuilder jpql = new StringBuilder("SELECT u FROM Usuario u WHERE 1=1");
        Map<String, Object> params = new HashMap<>();
        
        if (nome != null && !nome.isEmpty()) {
            jpql.append(" AND u.nome LIKE :nome");
            params.put("nome", "%" + nome + "%");
        }
        
        if (idade != null) {
            jpql.append(" AND u.idade >= :idade");
            params.put("idade", idade);
        }
        
        if (ativo != null) {
            jpql.append(" AND u.ativo = :ativo");
            params.put("ativo", ativo);
        }
        
        TypedQuery<Usuario> query = entityManager.createQuery(jpql.toString(), Usuario.class);
        params.forEach(query::setParameter);
        
        return query.getResultList();
    }
}
```

### Criteria API (type-safe)

```java
@Repository
public class UsuarioCriteriaRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // Criteria API - 100% type-safe e seguro
    public List<Usuario> buscarPorFaixaIdade(int idadeMin, int idadeMax) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Usuario> query = cb.createQuery(Usuario.class);
        Root<Usuario> usuario = query.from(Usuario.class);
        
        query.select(usuario)
             .where(cb.between(usuario.get("idade"), idadeMin, idadeMax));
        
        return entityManager.createQuery(query).getResultList();
    }
    
    // Busca complexa com joins
    public List<Usuario> buscarComPedidos(String statusPedido) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Usuario> query = cb.createQuery(Usuario.class);
        Root<Usuario> usuario = query.from(Usuario.class);
        Join<Usuario, Pedido> pedidos = usuario.join("pedidos");
        
        query.select(usuario)
             .where(cb.equal(pedidos.get("status"), statusPedido))
             .distinct(true);
        
        return entityManager.createQuery(query).getResultList();
    }
}
```

### Spring Data JPA

```java
@Repository
public interface UsuarioSpringRepository extends JpaRepository<Usuario, Long> {
    
    // Query methods - automaticamente seguras
    List<Usuario> findByNomeContainingIgnoreCase(String nome);
    List<Usuario> findByIdadeBetween(int idadeMin, int idadeMax);
    List<Usuario> findByEmailEndingWith(String dominio);
    
    // @Query com parâmetros nomeados
    @Query("SELECT u FROM Usuario u WHERE u.nome = :nome AND u.ativo = :ativo")
    List<Usuario> buscarPorNomeEStatus(@Param("nome") String nome, @Param("ativo") boolean ativo);
    
    // Query nativa (quando necessário) - sempre com parâmetros
    @Query(value = "SELECT * FROM usuarios u WHERE u.data_criacao >= :dataInicio", nativeQuery = true)
    List<Usuario> buscarCriadosApos(@Param("dataInicio") LocalDateTime dataInicio);
}
```

## 3. Validação de entrada

```java
@RestController
@Validated
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    // Validação com Bean Validation
    public ResponseEntity<Usuario> buscarUsuario(
            @PathVariable @Valid @Pattern(regexp = "\\d+") String id) {
        
        Long userId = Long.parseLong(id);
        Usuario usuario = usuarioService.buscarPorId(userId);
        return ResponseEntity.ok(usuario);
    }
    
    // Sanitização de entrada
    public ResponseEntity<List<Usuario>> pesquisarUsuarios(
            @RequestParam String termo) {
        
        // Remove caracteres potencialmente perigosos
        String termoLimpo = sanitizarEntrada(termo);
        List<Usuario> usuarios = usuarioService.pesquisar(termoLimpo);
        return ResponseEntity.ok(usuarios);
    }
    
    private String sanitizarEntrada(String entrada) {
        if (entrada == null) return "";
        
        // Remove caracteres SQL perigosos
        return entrada.replaceAll("[';\"\\-\\-/\\*\\*/]", "")
                     .trim()
                     .substring(0, Math.min(entrada.length(), 100)); // Limite de tamanho
    }
}
```

## 4. Stored Procedures (quando apropriado)

```java
@Repository
public class UsuarioStoredProcRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // Chamada de stored procedure segura
    public void atualizarStatusUsuario(Long usuarioId, String novoStatus) {
        String sql = "CALL atualizar_status_usuario(?, ?)";
        jdbcTemplate.update(sql, usuarioId, novoStatus);
    }
    
    // Stored procedure com resultado
    public List<Map<String, Object>> relatorioUsuarios(LocalDate dataInicio, LocalDate dataFim) {
        String sql = "CALL gerar_relatorio_usuarios(?, ?)";
        return jdbcTemplate.queryForList(sql, dataInicio, dataFim);
    }
}
```

## Medidas adicionais de segurança

### 1. Configuração de banco de dados

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/meudb
    username: app_user  # Usuário com privilégios mínimos
    password: ${DB_PASSWORD}  # Senha em variável de ambiente
    
  jpa:
    show-sql: false  # Nunca mostrar SQL em produção
    hibernate:
      ddl-auto: validate  # Nunca 'create' ou 'update' em produção
```

### 2. Princípio do menor privilégio

```sql
-- Usuário da aplicação com permissões mínimas
CREATE USER app_user WITH PASSWORD 'senha_forte';

-- Apenas SELECT, INSERT, UPDATE, DELETE nas tabelas necessárias
GRANT SELECT, INSERT, UPDATE, DELETE ON usuarios TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON pedidos TO app_user;

-- Sem privilégios administrativos
-- Sem DROP, CREATE, ALTER
-- Sem acesso a tabelas do sistema
```

### 3. Monitoramento e logging

```java
@Component
public class SqlInjectionDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlInjectionDetector.class);
    
    private static final List<String> PADROES_SUSPEITOS = Arrays.asList(
        "union", "select", "drop", "delete", "insert", "update",
        "--", "/*", "*/", "xp_", "sp_", "exec"
    );
    
    public boolean detectarTentativaInjection(String entrada) {
        if (entrada == null) return false;
        
        String entradaLower = entrada.toLowerCase();
        
        for (String padrao : PADROES_SUSPEITOS) {
            if (entradaLower.contains(padrao)) {
                logger.warn("Possível tentativa de SQL Injection detectada: {}", entrada);
                // Aqui poderia disparar alertas, bloquear IP, etc.
                return true;
            }
        }
        
        return false;
    }
}
```