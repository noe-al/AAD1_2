# Ejercicio 2 - Acceso a Datos

Este proyecto es una práctica de **Acceso a Datos en Java**.

Permite gestionar productos y movimientos de stock mediante una aplicación en consola que se conecta a una base de datos **MySQL**.  
Incluye además exportación/importación de datos en distintos formatos y consultas avanzadas.

---

## Contenido del proyecto

Al descomprimir el ZIP encontrarás:

```
├── Main.java
├── StockManager.java
├── XmlManager.java
├── JsonExporter.java
├── Main.class
├── StockManager.class
├── XmlManager.class
├── JsonExporter.class
├── libs/
│   └── mysql-connector-j-9.4.0.jar
├── errores.log
├── inventario.csv
├── export_xml.xml
├── import_xml.xml
├── invalid_import.xml
├── inventario.xsd
├── stock_bajo.json
└── README.md
```

---

##  Ejecución del programa

### Requisitos

- **Java JDK 8+**
- **MySQL Server** en `localhost:3306`

### Comando de ejecución

#### Windows  
```
java -cp ".;./libs/mysql-connector-j-9.4.0.jar" Main
```

#### macOS/Linux  
```
java -cp ".:./libs/mysql-connector-j-9.4.0.jar" Main
```

> Si tu MySQL no está en localhost, modifica la variable `URL_BASE` en `Main.java`.

---

## Funcionalidades

### Gestión de productos
- Crear / listar / modificar / eliminar productos
- Control de stock (añadir / quitar)
- Registro automático de movimientos de stock

### Importación / Exportación

| Función | Formato | Detalles |
|--------|--------|---------|
Exportar productos con bajo stock | JSON | `stock_bajo.json`
Exportar inventario completo | XML | `export_xml.xml`
Importar inventario | XML | Validado con **XSD**
Importar movimientos masivos | CSV | Transacciones por lote + rollback

### Validación XML

Todos los XML de inventario se validan contra `inventario.xsd`.

---

## Consultas avanzadas SQL incluidas

| Consulta | Descripción |
|---------|-------------|
Top N productos más vendidos | Ranking según movimientos de stock |
Valor total del stock por categoría | SUM(precio × unidades) agrupado |
Histórico de movimientos por rango de fechas | Filtrado `BETWEEN fecha1 AND fecha2` |

> Se utiliza `JOIN`, `GROUP BY`, `ORDER BY`, y funciones agregadas.

---

## Menú principal (resumen)

| Opción | Descripción |
|--------|-------------|
1 | Crear producto  
2 | Listar productos  
3 | Modificar producto  
4 | Eliminar producto  
5 | Importar productos desde CSV  
6 | Añadir stock  
7 | Quitar stock  
8 | Ver movimientos  
9 | Exportar productos con bajo stock (JSON)  
10 | Exportar inventario completo a XML 
11 | Restaurar inventario desde XML 
12 | Importar movimientos desde CSV (transacciones)  
13 | Consultas avanzadas SQL 
0 | Salir  

---

## Ejemplos usados en el proyecto

### Top 5 productos más vendidos
```
SELECT p.nombre, SUM(ABS(m.cantidad)) AS total_vendido
FROM movimientos_stock m
JOIN productos p ON m.id_producto = p.id_producto
WHERE m.cantidad < 0
GROUP BY p.id_producto
ORDER BY total_vendido DESC
LIMIT 5;
```

### Valor total del stock por categoría
```
SELECT categoria, SUM(precio * stock) AS valor_total
FROM productos
GROUP BY categoria;
```

### Movimientos entre fechas
```
SELECT *
FROM movimientos_stock
WHERE fecha BETWEEN '2024-01-01' AND '2024-12-31';
```

---

## Notas

- Si hay un error al importar movimientos, se hace **rollback completo**.
- Log de errores: `errores.log`.

---

##  Autor

Proyecto desarrollado como parte de la asignatura **Acceso a Datos (DAM)**.

