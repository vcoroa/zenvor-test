### a. Vendedores que não têm pedidos com Samsonic

```sql
SELECT s.Name
FROM Salesperson s
WHERE s.ID NOT IN (
    SELECT DISTINCT o.salesperson_id
    FROM Orders o
    INNER JOIN Customer c ON o.customer_id = c.ID
    WHERE c.Name = 'Samsonic'
);
```

### b. Atualizar nomes dos vendedores com 2+ pedidos (adicionar '*')

```sql
UPDATE Salesperson 
SET Name = Name + '*'
WHERE ID IN (
    SELECT salesperson_id
    FROM Orders
    GROUP BY salesperson_id
    HAVING COUNT(*) >= 2
);
```

### c. Deletar vendedores que fizeram pedidos para Jackson

```sql
DELETE FROM Salesperson
WHERE ID IN (
    SELECT DISTINCT o.salesperson_id
    FROM Orders o
    INNER JOIN Customer c ON o.customer_id = c.ID
    WHERE c.City = 'Jackson'
);
```

### d. Total de vendas por vendedor (incluindo zeros)

```sql
SELECT 
    s.Name,
    COALESCE(SUM(o.Amount), 0) as TotalSales
FROM Salesperson s
LEFT JOIN Orders o ON s.ID = o.salesperson_id
GROUP BY s.ID, s.Name
ORDER BY TotalSales DESC;
```