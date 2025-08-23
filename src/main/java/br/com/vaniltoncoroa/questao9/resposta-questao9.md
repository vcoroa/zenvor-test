# Testes para Sistema de Registro de Usuários

## Descrição do Sistema

Sistema que permite gerenciar usuários com as seguintes características:
- **Campos**: nome, email, endereço, telefone
- **Obrigatórios**: nome e email
- **Restrições**: email único no sistema
- **Segurança**: apenas admins podem excluir usuários

## 1. Tipos de Testes e Cenários

### Testes Unitários (Camada de Serviço)

**Objetivo**: Testar lógica de negócio isoladamente

**Cenários a testar:**
- Validação de campos obrigatórios
- Verificação de email único
- Regras de autorização para exclusão
- Sanitização de dados de entrada

**Exemplo de cenários:**
- Criar usuário com dados válidos
- Rejeitar usuário com email duplicado
- Rejeitar usuário sem nome ou email
- Permitir admin excluir usuário
- Negar exclusão para usuário comum

### Testes de Integração (Camada de Dados)

**Objetivo**: Verificar integração com banco de dados e persistência

**Cenários a testar:**
- Persistência correta no banco
- Constraints de unicidade funcionando
- Transações e rollback
- Consultas e filtros

**Exemplo de cenários:**
- Salvar usuário e recuperar do banco
- Tentar salvar email duplicado (constraint violation)
- Rollback em caso de falha na transação
- Buscar usuários por diferentes critérios

### Testes End-to-End (Interface Completa)

**Objetivo**: Simular fluxo completo do usuário final

**Cenários a testar:**
- Fluxo completo de cadastro via interface
- Interação entre componentes frontend/backend
- Validações de UI sincronizadas com backend
- Navegação e estados da aplicação

**Exemplo de cenários:**
- Usuário preenche formulário e cadastra com sucesso
- Sistema exibe erro de email duplicado na tela
- Admin consegue excluir usuário via interface
- Usuário comum não vê opção de exclusão

## 2. Casos Extremos e Tratamento

### Casos Extremos Identificados:

#### **Edge Cases:**
- **Nome muito longo**: "A".repeat(1000)
- **Email no limite**: 254 caracteres (limite RFC)
- **Endereço extenso**: Endereços muito longos
- **Telefone inválido**: Formatos não padronizados

**Tratamento:**
- Validação de tamanho máximo nos campos
- Truncar ou rejeitar dados excessivos
- Normalização de telefones

#### **Formatos de Email:**
- **Emails válidos edge case**: `user+tag@domain.co.uk`
- **Emails inválidos**: `invalid@`, `@domain.com`
- **Case sensitivity**: `User@Domain.com` vs `user@domain.com`

**Tratamento:**
- Regex robusta para validação de email
- Normalização para lowercase antes de salvar
- Validação tanto no frontend quanto backend

#### **Concorrência:**
- **Dois usuários tentam o mesmo email simultaneamente**
- **Admin exclui usuário enquanto outro está editando**

**Tratamento:**
- Constraint de unicidade no banco (última defesa)
- Locks otimistas ou pessimistas
- Tratamento de exceções de concorrência

#### **Caracteres Especiais:**
- **XSS**: `<script>alert('xss')</script>` no nome
- **SQL Injection**: `'; DROP TABLE users; --` em campos
- **Unicode**: Emojis, acentos, caracteres especiais

**Tratamento:**
- Sanitização de entrada (HTML encoding)
- Prepared statements (SQL injection)
- Validação de charset permitido

#### **Volumes Extremos:**
- **Cadastro em massa**: 10.000 usuários simultaneamente
- **Lista muito grande**: Paginação com milhões de registros

**Tratamento:**
- Rate limiting por usuário/IP
- Paginação obrigatória em listagens
- Timeouts adequados

## 3. Exemplos de Código de Teste

### Teste Unitário - Validação de Negócio

```java
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @InjectMocks
    private UsuarioService usuarioService;
    
    @Test
    void deveCriarUsuarioComDadosValidos() {
        // Given
        UsuarioCreateDTO dto = new UsuarioCreateDTO(
            "João Silva", 
            "joao@email.com", 
            "Rua A, 123", 
            "11999999999"
        );
        when(usuarioRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario(1L, dto));
        
        // When
        Usuario resultado = usuarioService.criarUsuario(dto);
        
        // Then
        assertThat(resultado.getNome()).isEqualTo("João Silva");
        assertThat(resultado.getEmail()).isEqualTo("joao@email.com");
        verify(usuarioRepository).save(any(Usuario.class));
    }
    
    @Test
    void deveRejeitarEmailDuplicado() {
        // Given
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Ana", "ana@email.com", null, null);
        when(usuarioRepository.existsByEmail("ana@email.com")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> usuarioService.criarUsuario(dto))
            .isInstanceOf(EmailJaExisteException.class)
            .hasMessage("Email já está em uso: ana@email.com");
    }
    
    @Test
    void devePermitirAdminExcluirUsuario() {
        // Given
        Usuario admin = new Usuario("admin@email.com", Role.ADMIN);
        Usuario usuario = new Usuario(1L, "user@email.com", Role.USER);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        
        // When
        usuarioService.excluirUsuario(1L, admin);
        
        // Then
        verify(usuarioRepository).delete(usuario);
    }
    
    @Test
    void deveRejeitarUsuarioComumExcluindoOutro() {
        // Given
        Usuario usuarioComum = new Usuario("user@email.com", Role.USER);
        
        // When & Then
        assertThatThrownBy(() -> usuarioService.excluirUsuario(1L, usuarioComum))
            .isInstanceOf(PermissaoInsuficienteException.class)
            .hasMessage("Apenas administradores podem excluir usuários");
    }
}
```

### Teste de Integração - Persistência

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class UsuarioIntegrationTest {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Test
    void devePersistirUsuarioNoBanco() {
        // Given
        UsuarioCreateDTO dto = new UsuarioCreateDTO(
            "Maria Santos", 
            "maria@test.com", 
            "Av. Principal, 456", 
            "11888888888"
        );
        
        // When
        Usuario salvo = usuarioService.criarUsuario(dto);
        
        // Then
        Optional<Usuario> encontrado = usuarioRepository.findById(salvo.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo("maria@test.com");
    }
    
    @Test
    void deveRejeitarEmailDuplicadoNoBanco() {
        // Given
        usuarioRepository.save(new Usuario("primeiro@test.com", "Primeiro"));
        
        // When & Then
        assertThatThrownBy(() -> {
            Usuario segundo = new Usuario("segundo@test.com", "Segundo");
            segundo.setEmail("primeiro@test.com"); // Email duplicado
            usuarioRepository.save(segundo);
            usuarioRepository.flush(); // Força execução no banco
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
```

### Teste End-to-End - API REST

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class UsuarioE2ETest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Test
    void deveCompletarFluxoDeCadastro() {
        // Given
        UsuarioCreateDTO novoUsuario = new UsuarioCreateDTO(
            "Pedro E2E", 
            "pedro@e2e.com", 
            "Rua E2E, 789", 
            "11777777777"
        );
        
        // When - Criar usuário via API
        ResponseEntity<Usuario> response = restTemplate.postForEntity(
            "/api/usuarios", novoUsuario, Usuario.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getNome()).isEqualTo("Pedro E2E");
        
        // Verificar se foi salvo no banco
        Optional<Usuario> salvo = usuarioRepository.findByEmail("pedro@e2e.com");
        assertThat(salvo).isPresent();
    }
    
    @Test
    void deveRetornarErroParaEmailDuplicado() {
        // Given - Usuário já existe
        usuarioRepository.save(new Usuario("existente@test.com", "Existente"));
        
        UsuarioCreateDTO duplicado = new UsuarioCreateDTO(
            "Tentativa", "existente@test.com", null, null);
        
        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/usuarios", duplicado, String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Email já está em uso");
    }
}
```

### Teste de Casos Extremos

```java
@ParameterizedTest
@ValueSource(strings = {"", "invalid", "sem@dominio", "@invalid.com", "user@"})
void deveRejeitarEmailsInvalidos(String emailInvalido) {
    UsuarioCreateDTO dto = new UsuarioCreateDTO("Nome", emailInvalido, null, null);
    
    assertThatThrownBy(() -> usuarioService.criarUsuario(dto))
        .isInstanceOf(EmailInvalidoException.class);
}

@Test
void deveTratarNomeMuitoLongo() {
    String nomeLongo = "A".repeat(500); // 500 caracteres
    UsuarioCreateDTO dto = new UsuarioCreateDTO(nomeLongo, "test@email.com", null, null);
    
    assertThatThrownBy(() -> usuarioService.criarUsuario(dto))
        .isInstanceOf(NomeMuitoLongoException.class);
}

@Test
void deveTratarConcorrenciaEmailDuplicado() {
    String email = "concorrencia@test.com";
    UsuarioCreateDTO dto1 = new UsuarioCreateDTO("User1", email, null, null);
    UsuarioCreateDTO dto2 = new UsuarioCreateDTO("User2", email, null, null);
    
    // Simula duas threads tentando criar usuário com mesmo email
    CompletableFuture<Usuario> future1 = CompletableFuture.supplyAsync(() -> 
        usuarioService.criarUsuario(dto1));
    CompletableFuture<Usuario> future2 = CompletableFuture.supplyAsync(() -> 
        usuarioService.criarUsuario(dto2));
    
    // Apenas um deve ter sucesso
    List<Usuario> resultados = Stream.of(future1, future2)
        .map(f -> {
            try { return f.get(); } 
            catch (Exception e) { return null; }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
        
    assertThat(resultados).hasSize(1);
}
```

## Estratégia de Cobertura de Testes

### Pirâmide de Testes:
- **70% Testes Unitários**: Validações, regras de negócio, casos extremos
- **20% Testes de Integração**: Persistência, transações, constraints
- **10% Testes E2E**: Fluxos críticos, validação end-to-end

### Métricas de Qualidade:
- **Cobertura de código**: > 80%
- **Cobertura de branches**: > 70%
- **Mutation testing**: > 60%
- **Tempo de execução**: < 30 segundos para toda suite

Esta estratégia garante alta qualidade e confiabilidade do sistema de registro de usuários.