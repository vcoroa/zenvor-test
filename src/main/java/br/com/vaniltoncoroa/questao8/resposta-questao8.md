# Sistema XYZ - Fase 1: Gestão de Plantas

## 1. User Stories

### US001 - Cadastrar Planta
```
Como um usuário do sistema XYZ,
Eu quero cadastrar uma nova planta com código e descrição,
Para que os dados fiquem disponíveis para a segunda fase do projeto.

Critérios de Aceitação:
- Posso inserir código numérico obrigatório
- Posso inserir descrição alfanumérica opcional (máximo 10 caracteres)
- Sistema impede cadastro de código duplicado
- Recebo confirmação de sucesso após cadastro
- Dados ficam persistidos no sistema
```

### US002 - Consultar Plantas
```
Como um usuário do sistema XYZ,
Eu quero consultar plantas cadastradas por código ou descrição,
Para visualizar as informações disponíveis no sistema.

Critérios de Aceitação:
- Posso buscar por código específico
- Posso buscar por parte da descrição
- Sistema retorna lista de plantas encontradas
- Posso visualizar todos os detalhes da planta
```

### US003 - Atualizar Planta
```
Como um usuário do sistema XYZ,
Eu quero atualizar informações de uma planta existente,
Para manter os dados sempre atualizados.

Critérios de Aceitação:
- Posso alterar apenas a descrição (código é imutável)
- Validações de formato são aplicadas
- Sistema confirma alteração realizada
```

### US004 - Excluir Planta
```
Como um usuário administrador do sistema XYZ,
Eu quero excluir plantas que não são mais necessárias,
Para manter o sistema organizado e limpo.

Critérios de Aceitação:
- Apenas usuários admin podem excluir
- Sistema solicita confirmação antes de excluir
- Planta é removida permanentemente do sistema
```

## 2. Regras de Negócio e Premissas

### Regras de Negócio:
- **RN001**: Código da planta deve ser numérico, obrigatório e único no sistema
- **RN002**: Descrição é opcional, alfanumérica, máximo 10 caracteres
- **RN003**: Apenas usuários com perfil "Admin" podem excluir plantas
- **RN004**: Código da planta não pode ser alterado após criação
- **RN005**: Sistema deve prevenir duplicação de códigos em qualquer operação

### Premissas:
- Sistema possui controle de usuários e perfis implementado
- Dados serão utilizados como input para a fase 2 (sem necessidade de migração)
- Performance não é crítica na fase 1 (volume baixo esperado)
- Sistema deve ser web-based para facilitar acesso
- Auditoria básica é suficiente (quem criou/alterou)

## 3. Validações e Medidas de Segurança

### Validações de Entrada:
```java
// Validação do código da planta
@NotNull(message = "Código é obrigatório")
@Pattern(regexp = "^[0-9]+$", message = "Código deve conter apenas números")
private String codigo;

// Validação da descrição
@Size(max = 10, message = "Descrição não pode exceder 10 caracteres")
@Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Descrição deve ser alfanumérica")
private String descricao;
```

### Validações de Negócio:
- Verificar unicidade do código antes de salvar
- Validar se código não está sendo alterado em updates
- Confirmar permissão de admin antes de exclusões

### Medidas de Segurança:
- **Autenticação**: Login obrigatório para acessar funcionalidades
- **Autorização**: Verificação de perfil para operações sensíveis
- **Auditoria**: Log de todas as operações (criar, alterar, excluir)
- **Sanitização**: Limpeza de entrada para prevenir XSS/SQL Injection
- **Rate Limiting**: Limite de operações por minuto por usuário

### Controle de Acesso:
```java
@PreAuthorize("hasRole('USER')")
public void criarPlanta(PlantaDTO planta) { ... }

@PreAuthorize("hasRole('USER')")
public void atualizarPlanta(PlantaDTO planta) { ... }

@PreAuthorize("hasRole('ADMIN')")
public void excluirPlanta(String codigo) { ... }
```

## 4. Estratégia de Testes

### Testes Unitários:
```java
@Test
public void deveCriarPlantaComDadosValidos() {
    PlantaDTO planta = new PlantaDTO("123", "Planta A");
    PlantaDTO resultado = plantaService.criar(planta);
    assertThat(resultado.getCodigo()).isEqualTo("123");
}

@Test
public void deveRejeitarCodigoDuplicado() {
    plantaService.criar(new PlantaDTO("123", "Primeira"));
    
    assertThatThrownBy(() -> 
        plantaService.criar(new PlantaDTO("123", "Segunda")))
        .isInstanceOf(CodigoDuplicadoException.class);
}
```

### Casos de Teste - Cenários Normais:
- **CT001**: Criar planta com código e descrição válidos
- **CT002**: Criar planta apenas com código (descrição opcional)
- **CT003**: Buscar planta por código existente
- **CT004**: Atualizar descrição de planta existente
- **CT005**: Admin excluir planta existente

### Casos de Teste - Casos Extremos:
- **CT006**: Código com zeros à esquerda (00123)
- **CT007**: Descrição com exatamente 10 caracteres
- **CT008**: Tentativa de criar código duplicado
- **CT009**: Usuário comum tentar excluir planta
- **CT010**: Buscar código inexistente
- **CT011**: Descrição com caracteres especiais
- **CT012**: Código vazio ou nulo
- **CT013**: Descrição com mais de 10 caracteres
- **CT014**: Tentativa de alterar código em update

### Testes de Integração:
- Fluxo completo: criar → consultar → atualizar → excluir
- Validação de persistência no banco de dados
- Teste de transações e rollback em falhas

### Testes de Segurança:
- Acesso sem autenticação (deve ser negado)
- Usuário comum tentando excluir (deve ser negado)
- SQL Injection em campos de entrada
- XSS em descrição da planta

### Testes de Performance:
- Criação de 1000 plantas simultâneas
- Busca em base com 10.000 plantas
- Validação de unicidade com alto volume

## 5. Implementação Sugerida

### Modelo de Dados:
```sql
CREATE TABLE plantas (
    codigo VARCHAR(20) PRIMARY KEY,
    descricao VARCHAR(10),
    data_criacao TIMESTAMP DEFAULT NOW(),
    usuario_criacao VARCHAR(50),
    data_alteracao TIMESTAMP,
    usuario_alteracao VARCHAR(50)
);

CREATE UNIQUE INDEX idx_plantas_codigo ON plantas(codigo);
```

### API REST:
- `POST /api/plantas` - Criar planta
- `GET /api/plantas/{codigo}` - Buscar por código
- `GET /api/plantas?descricao={termo}` - Buscar por descrição
- `PUT /api/plantas/{codigo}` - Atualizar planta
- `DELETE /api/plantas/{codigo}` - Excluir planta (admin only)

### Tratamento de Erros:
- **400 Bad Request**: Dados inválidos
- **401 Unauthorized**: Não autenticado
- **403 Forbidden**: Sem permissão (exclusão)
- **404 Not Found**: Planta não encontrada
- **409 Conflict**: Código duplicado

Esta implementação garante que a fase 1 atenda todos os requisitos e prepare adequadamente os dados para as fases subsequentes do projeto.