# Ejercicio 1 - Acceso a Datos

Este proyecto es una práctica de **Acceso a Datos en Java**.
El usuario podrá gestionar productos y sus movimientos de stock mediante una aplicación de consola que interactúa con una base de datos MySQL.

---

## Contenido del ZIP

Al descomprimir el archivo ZIP recibido, encontrarás:

```
├── Main.java
├── libs/
│   └── mysql-connector-j-9.4.0.jar
├── errores.log
├── inventario.csv
├── JsonExporter.class
├──JsonExporter.java
├── Main.class
├── Main.java
├── README.md
├── stock_bajo.json
├── StockManager.class
└── StockManager.java
```

---

## Ejecución del programa

1. **Requisitos previos:**

   * Tener instalado **Java JDK** (versión 8 o superior).
   * Tener una base de datos **MySQL** ejecutándose en `localhost:3306`.

2. **Comando de ejecución:**
   Desde la terminal, dentro de la carpeta del proyecto, ejecuta:

   ```
   java -cp ".;.\libs\mysql-connector-j-9.4.0.jar" Main
   ```

   En sistemas macOS o Linux, el separador de rutas debe ser `:` en lugar de `;`:

   ```
   java -cp ".:./libs/mysql-connector-j-9.4.0.jar" Main
   ```

3. **Configuración de la base de datos:**

   * Por defecto, el programa se conecta a `localhost` en el puerto `3306`.
   * Si tu base de datos está en otro host o puerto, modifica la **línea 25** del archivo `Main.java`, donde se define `URL_BASE`.

---

## Creación automática de la base de datos

Al ejecutar el programa por primera vez, se creará automáticamente la base de datos:

```
aad1_1
```

Con las siguientes tablas:

* **productos**
* **movimientos_stock**

---

## Menú principal

Al iniciar el programa, se mostrará un menú con las siguientes opciones:

| Nº                           | Opción                                       | Descripción                                                                                                                   |
| ---------------------------- | -------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| 1                            | **Crear producto**                           | Permite introducir un nuevo producto (nombre, categoría, precio y stock inicial).                                             |
| 2                            | **Listar productos**                         | Muestra todos los productos con sus datos e IDs.                                                                              |
| 3                            | **Modificar producto**                       | Permite modificar los datos de un producto existente escribiendo su nombre. Si dejas un campo vacío, ese dato no se modifica. |
| 4                            | **Eliminar producto**                        | Elimina un producto tras confirmación. Se pedirá el nombre y confirmación con `S` para eliminar.                              |
| 5                            | **Importar productos desde CSV**             | Permite añadir productos desde un archivo CSV bien estructurado. Antes de insertar, se validan los datos.                     |
| 6                            | **Añadir stock**                             | Muestra los productos y permite seleccionar por ID el producto al que se añadirá stock, indicando la cantidad.                |
| 7                            | **Quitar stock**                             | Similar a la opción 6, pero para reducir el stock.                                                                            |
| 8                            | **Ver movimientos de stock**                 | Muestra todos los cambios de stock registrados (modificaciones, entradas y salidas).                                          |
| 9                            | **Exportar productos con bajo stock (JSON)** | Exporta a un archivo JSON los productos cuyo stock sea menor o igual al valor introducido. <br> 
| 0 | **Salir** | Finaliza el programa. |

---

## Notas adicionales

- Asegúrate de tener permisos de escritura en el directorio de ejecución si deseas exportar el JSON.
- Los movimientos de stock se registran automáticamente tanto al crear o modificar productos como al añadir o quitar stock.

---

##  Autor

Ejercicio desarrollado como parte de la asignatura **Acceso a Datos**.
