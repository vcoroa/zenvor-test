# Questão 6 - Otimização de Performance de Batch Process

## Abordagem para diagnóstico e otimização

Para otimizar um batch process que interage com database e FTP, sigo uma metodologia estruturada focando nos principais gargalos.

## 1. Identificação de Bottlenecks

Primeiro adiciono logs com timestamp em cada etapa principal (início query banco, fim query, início processamento, início FTP, etc.) para ver onde o tempo está sendo gasto. Monitoro CPU, memória e se há muitas conexões ociosas ou em espera.

O objetivo é identificar se o gargalo está no banco, no processamento ou na transferência FTP.

## 2. Otimização de Database Queries

### Identificação de problemas:
- Queries pesadas sem índices
- Full table scans desnecessários
- Falta de paginação onde seria coerente
- Connection pool mal dimensionado

### Soluções implementadas:
- **Índices onde faz sentido ter**: Em colunas de WHERE, JOIN e ORDER BY
- **Paginação**: Processar em lotes de 1000-10000 registros (verificar quantidades que fazem sentido)
- **Projeção**: SELECT apenas campos necessários
- **Batch operations**: Agrupar INSERTs/UPDATEs em lotes
- **otimização de pool**: Pool size adequado (10-20 conexões)

Exemplo: Substituir `SELECT * FROM usuarios` por `SELECT id, nome FROM usuarios WHERE ativo = true ORDER BY id LIMIT 10000`.

## 3. Otimização da Lógica de Execução

### Problemas identificados:
- Processamento sequencial de grandes volumes
- Uso excessivo de memória carregando tudo de uma vez

### Melhorias aplicadas:
- **Processamento paralelo**: Múltiplas threads para CPU-intensive tasks (avaliar uso de virtual threads disponivel apartir do java 19)
- **Streaming**: Processar dados conforme chegam, não tudo na memória
- **Memory management**: Clear de caches, objetos null após uso
- **Batch sizing**: Processar em lotes menores para controle de memória

Exemplo: Dividir 1 milhão de registros em 100 lotes de 10k processados em paralelo.

### Framework alternativo:
Para casos mais complexos, considero usar **Spring Batch** que já vem com gerenciamento de transações, restart automático em falhas, processamento paralelo e métricas built-in.
Facilita muito quando o processo tem muitas regras de negócio.

## 4. Otimização de Transferência FTP

### Gargalos comuns:
- Nova conexão para cada arquivo
- Transferência sequencial
- Arquivos grandes sem compressão
- Timeout inadequado

### Otimizações que poderiam ser implementadas:
- **Connection pooling**: Reutilizar conexões FTP
- **Transferência paralela**: Múltiplas threads para uploads simultâneos
- **Compressão**: GZIP antes do envio (reduz 60-80% do tamanho)
- **Modo binário**: Mais eficiente que ASCII
- **Buffer otimizado**: Aumentar buffer size (1MB+)

Exemplo: Pool de 5 conexões FTP processando arquivos em paralelo com compressão.

## 5. Ferramentas de Análise Utilizadas

Para análise uso basicamente logs detalhados, monitoramento de recursos do sistema (CPU/memória), e habilito slow query logs no banco. Se precisar de profiling mais detalhado, qualquer ferramenta de JVM serve.

O importante é medir antes e depois de cada otimização para confirmar a melhoria.