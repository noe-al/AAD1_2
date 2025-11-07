import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Clase principal del sistema de gestión de inventario.
 * Proporciona una interfaz de usuario basada en consola para:
 * - Gestionar productos (crear, ver, modificar, eliminar)
 * - Controlar el stock (entradas, salidas)
 * - Importar datos desde CSV
 * - Exportar datos a JSON
 * - Consultar movimientos de stock
 */
public class Main {

	// 🔹 Datos de conexión con XAMPP/MySQL
	private static final String URL_BASE = "jdbc:mysql://localhost:3306/";
	private static final String DB_NAME = "aad1_2";
	private static final String URL = URL_BASE + DB_NAME;
	private static final String USUARIO = "root";
	private static final String PASSWORD = "";
    
    /**
     * Inicializa la base de datos y la tabla productos si no existen.
     */
    private static void initializeDatabase() {
        try {
            // Primero intentamos crear la base de datos
            try (Connection conn = DriverManager.getConnection(URL_BASE, USUARIO, PASSWORD)) {
                try (PreparedStatement ps = conn.prepareStatement("CREATE DATABASE IF NOT EXISTS " + DB_NAME)) {
                    ps.executeUpdate();
                    System.out.println("Base de datos creada o verificada correctamente.");
                }
            }
            
            // Ahora creamos la tabla productos en la base de datos
            try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
                String createTableSQL = "CREATE TABLE IF NOT EXISTS productos ("
                    + "id_producto INT PRIMARY KEY,"
                    + "nombre VARCHAR(100) NOT NULL,"
                    + "categoria VARCHAR(50) NOT NULL,"
                    + "precio VARCHAR(20) NOT NULL,"
                    + "stock INT NOT NULL DEFAULT 0"
                    + ")";
                
                try (PreparedStatement ps = conn.prepareStatement(createTableSQL)) {
                    ps.executeUpdate();
                    System.out.println("Tabla productos creada o verificada correctamente.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al inicializar la base de datos: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Punto de entrada principal de la aplicación.
     * Muestra un menú interactivo que permite al usuario realizar diferentes
     * operaciones de gestión de inventario.
     *
     * @param args Argumentos de línea de comandos (no se utilizan)
     */
	public static void main(String[] args) {
        // Inicializar la base de datos y tablas
        initializeDatabase();
		Scanner scanner = new Scanner(System.in);
		int opcion;
		
		// Inicializar la tabla de movimientos de stock
		try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
			StockManager.initializeTables(conn);
		} catch (SQLException e) {
			System.out.println("Error al inicializar las tablas: " + e.getMessage());
		}
		
		do {
			System.out.println("\n=== GESTIÓN DE PRODUCTOS ===");
			System.out.println("1. Crear producto");
			System.out.println("2. Ver productos");
			System.out.println("3. Modificar producto");
			System.out.println("4. Eliminar producto");
			System.out.println("5. Añadir productos del CSV");
			System.out.println("6. Registrar entrada de stock");
			System.out.println("7. Registrar salida de stock");
			System.out.println("8. Ver movimientos de stock");
			System.out.println("9. Exportar productos con stock bajo a JSON");
			System.out.println("10. Exportar inventario a XML");
			System.out.println("11. Importar inventario desde XML");
			System.out.println("12. Ver top N productos más vendidos");
			System.out.println("13. Ver valor total de stock por categoría");
			System.out.println("14. Ver histórico de movimientos por fecha");
			System.out.println("0. Salir");
			System.out.print("Seleccione una opción: ");
			
			opcion = Integer.parseInt(scanner.nextLine());
			
			switch (opcion) {
				case 1:
					crearProducto(scanner);
					break;
				case 2:
					verProductos();
					break;
				case 3:
					modificarProducto(scanner);
					break;
				case 4:
					eliminarProducto(scanner);
					break;
				case 5:
					anadirDesdeCSV();
					break;
				case 6:
					registrarEntradaStock(scanner);
					break;
				case 7:
					registrarSalidaStock(scanner);
					break;
				case 8:
					verMovimientosStock(scanner);
					break;
				case 9:
					exportarProductosStockBajoJSON(scanner);
					break;
				case 10:
					exportarInventarioXML(scanner);
					break;
				case 11:
					importarInventarioXML(scanner);
					break;
				case 12:
					verTopProductosMasVendidos(scanner);
					break;
				case 13:
					verValorStockPorCategoria();
					break;
				case 14:
					verMovimientosPorFecha(scanner);
					break;
				case 0:
					System.out.println("¡Hasta luego!");
					break;
				default:
					System.out.println("Opción no válida");
			}
		} while (opcion != 0);
		
		scanner.close();
	}
	
    /**
     * Permite crear un nuevo producto en la base de datos.
     * Solicita al usuario los datos del producto (nombre, categoría, precio y stock inicial)
     * y genera automáticamente un ID único para el nuevo producto.
     *
     * @param scanner Scanner para leer la entrada del usuario
     */
	private static void crearProducto(Scanner scanner) {
		try {
			System.out.println("\n=== CREAR NUEVO PRODUCTO ===");
			
			System.out.print("Nombre del producto: ");
			String nombre = scanner.nextLine();
			
			System.out.print("Categoría: ");
			String categoria = scanner.nextLine();
			
			System.out.print("Precio: ");
			String precio = scanner.nextLine();
			
			System.out.print("Stock inicial: ");
			int stock = Integer.parseInt(scanner.nextLine());
			
			try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
				String sql = "SELECT MAX(id_producto) FROM productos";
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				
				// Obtener el siguiente ID
				int nextId = 1; // valor por defecto si la tabla está vacía
				if (rs.next()) {
					int maxId = rs.getInt(1); // obtener el valor de la primera columna
					nextId = maxId + 1;
				}
				
				sql = "INSERT INTO productos (id_producto, nombre, categoria, precio, stock) VALUES (?, ?, ?, ?, ?)";
				ps = conn.prepareStatement(sql);

				ps.setInt(1, nextId);
				ps.setString(2, nombre);
				ps.setString(3, categoria);
				ps.setString(4, precio);
				ps.setInt(5, stock);
				
				ps.executeUpdate();
				System.out.println("Producto creado correctamente.");
				
			} catch (SQLException e) {
				System.out.println("Error al crear el producto: " + e.getMessage());
			}
		} catch (NumberFormatException e) {
			System.out.println("Error: El precio y el stock deben ser números válidos.");
		}
	}
	
    /**
     * Muestra todos los productos existentes en la base de datos.
     * Lista cada producto con su ID, nombre, categoría, precio y stock actual.
     */
	private static void verProductos() {
		try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
			String sql = "SELECT * FROM productos";

			// Ejecutar EXPLAIN
        	ejecutarExplain(conn, sql);
			PreparedStatement ps = conn.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			
			System.out.println("\n=== LISTA DE PRODUCTOS ===");
			while (rs.next()) {
				System.out.println("ID: " + rs.getInt("id_producto"));
				System.out.println("Nombre: " + rs.getString("nombre"));
				System.out.println("Categoría: " + rs.getString("categoria"));
				System.out.println("Precio: " + rs.getString("precio"));
				System.out.println("Stock: " + rs.getInt("stock"));
				System.out.println("------------------------");
			}
		} catch (SQLException e) {
			System.out.println("Error al obtener los productos: " + e.getMessage());
		}
	}
	
    /**
     * Permite modificar los datos de un producto existente.
     * El usuario puede modificar nombre, categoría, precio y stock.
     * Utiliza transacciones para garantizar la integridad de los datos
     * cuando se modifica el stock y registra los movimientos en el histórico.
     *
     * @param scanner Scanner para leer la entrada del usuario
     */
	private static void modificarProducto(Scanner scanner) {
		try {
			System.out.print("Introduzca el nombre del producto a modificar: ");
			String nombre = scanner.nextLine();
			
			try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
				String sql = "SELECT * FROM productos WHERE nombre = ?";

				// Ejecutar EXPLAIN
        		ejecutarExplain(conn, sql);
				// Primero buscar el producto
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, nombre);
				ResultSet rs = ps.executeQuery();
				
				if (rs.next()) {
					System.out.println("\nProducto actual:");
					System.out.println("ID: " + rs.getInt("id_producto"));
					System.out.println("Nombre: " + rs.getString("nombre"));
					System.out.println("Categoría: " + rs.getString("categoria"));
					System.out.println("Precio: " + rs.getString("precio"));
					System.out.println("Stock: " + rs.getInt("stock"));
					
					// Pedir nuevos datos
					System.out.println("\nIntroduzca los nuevos datos (deje en blanco para mantener el valor actual):");
					
					System.out.print("Nuevo nombre [" + rs.getString("nombre") + "]: ");
					String nuevoNombre = scanner.nextLine();
					if (nuevoNombre.trim().isEmpty()) nuevoNombre = rs.getString("nombre");

					System.out.print("Nueva categoría [" + rs.getString("categoria") + "]: ");
					String nuevaCategoria = scanner.nextLine();
					if (nuevaCategoria.trim().isEmpty()) nuevaCategoria = rs.getString("categoria");
					
					System.out.print("Nuevo precio [" + rs.getString("precio") + "]: ");
					String precioStr = scanner.nextLine();
					if (precioStr.trim().isEmpty()) precioStr = rs.getString("precio");
					
					System.out.print("Nuevo stock [" + rs.getInt("stock") + "]: ");
					String stockStr = scanner.nextLine();
					int nuevoStock = stockStr.trim().isEmpty() ? rs.getInt("stock") : Integer.parseInt(stockStr);
					
					// Obtener el stock actual y el ID del producto
					int stockActual = rs.getInt("stock");
					int idProducto = rs.getInt("id_producto");
					
					conn.setAutoCommit(false);
					try {
						// Actualizar producto
						PreparedStatement psUpdate = conn.prepareStatement(
							"UPDATE productos SET nombre = ?, categoria = ?, precio = ? WHERE nombre = ?"
						);
						psUpdate.setString(1, nuevoNombre);
						psUpdate.setString(2, nuevaCategoria);
						psUpdate.setString(3, precioStr);
						psUpdate.setString(4, nombre);
						
						psUpdate.executeUpdate();
						
						// Si hay cambio en el stock, actualizarlo y registrar el movimiento
						if (nuevoStock != stockActual) {
							int diferencia = nuevoStock - stockActual;
							String tipoMovimiento = diferencia > 0 ? StockManager.ENTRADA : StockManager.SALIDA;
							
							// Actualizar stock
							String updateStock = diferencia > 0 
								// Si la diferencia es positiva, se usa una consulta simple para sumar al stock
								// Si es negativa, se añade una condición para verificar que hay suficiente stock
								? "UPDATE productos SET stock = stock + ? WHERE id_producto = ?"
								: "UPDATE productos SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";
							
							try (PreparedStatement psStock = conn.prepareStatement(updateStock)) {
								// Se usa el valor absoluto de la diferencia ya que el signo ya está en la consulta
								psStock.setInt(1, Math.abs(diferencia));
								psStock.setInt(2, idProducto);
								
								// Si es una reducción de stock (diferencia negativa)
								// añadimos el parámetro para la verificación de stock suficiente
								if (diferencia < 0) {
									psStock.setInt(3, Math.abs(diferencia));
								}
								
								// Ejecutar la actualización y verificar si se actualizó alguna fila
								int filasActualizadas = psStock.executeUpdate();
								if (filasActualizadas == 0) {
									// Si no se actualizó ninguna fila, puede ser porque:
									// 1. El producto no existe
									// 2. No hay suficiente stock (en caso de reducción)
									throw new SQLException("No hay suficiente stock disponible");
								}
								
								// Si la actualización fue exitosa, registrar el movimiento en el histórico
								StockManager.registrarMovimientoStock(conn, idProducto, 
									tipoMovimiento, Math.abs(diferencia));
							}
						}
						
						conn.commit();
						System.out.println("Producto actualizado correctamente.");
					} catch (SQLException ex) {
						conn.rollback();
						throw ex;
					} finally {
						conn.setAutoCommit(true);
					}
				} else {
					System.out.println("No se encontró ningún producto con ese nombre.");
				}
			} catch (SQLException e) {
				System.out.println("Error al modificar el producto: " + e.getMessage());
			}
		} catch (NumberFormatException e) {
			System.out.println("Error: El stock debe ser un número válido.");
		}
	}
	
    /**
     * Permite eliminar un producto de la base de datos.
     * Muestra los detalles del producto antes de eliminarlo y
     * solicita confirmación del usuario.
     *
     * @param scanner Scanner para leer la entrada del usuario
     */
    private static void eliminarProducto(Scanner scanner) {
        try {
            System.out.print("Introduzca el nombre del producto a eliminar: ");
            String nombre = scanner.nextLine();
            
            try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {

				String sql = "SELECT * FROM productos WHERE nombre = ?";

				// Ejecutar EXPLAIN
        		ejecutarExplain(conn, sql);
                // Buscar y mostrar el producto
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, nombre);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    System.out.println("\nProducto encontrado:");
                    System.out.println("ID: " + rs.getInt("id_producto"));
                    System.out.println("Nombre: " + rs.getString("nombre"));
                    System.out.println("Categoría: " + rs.getString("categoria"));
                    System.out.println("Precio: " + rs.getString("precio"));
                    System.out.println("Stock: " + rs.getInt("stock"));
                    
                    System.out.print("\n¿Está seguro de que desea eliminar este producto? (S/N): ");
                    String confirmacion = scanner.nextLine();
                    
                    if (confirmacion.equalsIgnoreCase("S")) {
                        int idProducto = rs.getInt("id_producto");
                        conn.setAutoCommit(false);
                        
                        try {
                            // Eliminar los movimientos primero
                            PreparedStatement psDeleteMov = conn.prepareStatement(
                                "DELETE FROM movimientos_stock WHERE id_producto = ?"
                            );
                            psDeleteMov.setInt(1, idProducto);
                            psDeleteMov.executeUpdate();
                            
                            // Luego eliminar el producto
                            PreparedStatement psDeleteProd = conn.prepareStatement(
                                "DELETE FROM productos WHERE id_producto = ?"
                            );
                            psDeleteProd.setInt(1, idProducto);
                            psDeleteProd.executeUpdate();
                            
                            conn.commit();
                            System.out.println("Producto y sus movimientos eliminados correctamente.");
                            
                        } catch (SQLException e) {
                            conn.rollback();
                            System.out.println("Error durante la eliminación: " + e.getMessage());
                        } finally {
                            conn.setAutoCommit(true);
                        }
                    } else {
                        System.out.println("Operación cancelada.");
                    }
                } else {
                    System.out.println("No se encontró ningún producto con ese nombre.");
                }
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error general: " + e.getMessage());
        }
    }

    /**
     * Registra una entrada de stock para un producto.
     * Actualiza el stock del producto y registra el movimiento en el histórico.
     * Utiliza transacciones para garantizar la integridad de los datos.
     *
     * @param scanner Scanner para leer la entrada del usuario
     */
	private static void registrarEntradaStock(Scanner scanner) {
		try {
			System.out.println("\n=== REGISTRAR ENTRADA DE STOCK ===");
			
			// Mostrar productos disponibles
			verProductos();
			
			System.out.print("\nIntroduzca el ID del producto: ");
			int idProducto = Integer.parseInt(scanner.nextLine());
			
			System.out.print("Cantidad a añadir: ");
			int cantidad = Integer.parseInt(scanner.nextLine());
			
			try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
                conn.setAutoCommit(false);
                try {
                    // 1. Actualizar el stock
                    String updateStock = "UPDATE productos SET stock = stock + ? WHERE id_producto = ?";
                    try (PreparedStatement psUpdate = conn.prepareStatement(updateStock)) {
                        psUpdate.setInt(1, cantidad);
                        psUpdate.setInt(2, idProducto);
                        
                        int filasActualizadas = psUpdate.executeUpdate();
                        if (filasActualizadas == 0) {
                            throw new SQLException("El producto no existe");
                        }
                    }

                    // 2. Registrar el movimiento en el histórico
                    StockManager.registrarMovimientoStock(conn, idProducto, StockManager.ENTRADA, cantidad);
                    
                    conn.commit();
                    System.out.println("Entrada de stock registrada correctamente.");
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
			} catch (SQLException e) {
				System.out.println("Error al registrar la entrada de stock: " + e.getMessage());
			}
		} catch (NumberFormatException e) {
			System.out.println("Error: Por favor, introduzca números válidos.");
		}
	}
	
    /**
     * Registra una salida de stock para un producto.
     * Verifica que haya suficiente stock antes de realizar la operación.
     * Actualiza el stock y registra el movimiento en el histórico.
     * Utiliza transacciones para garantizar la integridad de los datos.
     *
     * @param scanner Scanner para leer la entrada del usuario
     */
	private static void registrarSalidaStock(Scanner scanner) {
		try {
			System.out.println("\n=== REGISTRAR SALIDA DE STOCK ===");
			
			// Mostrar productos disponibles
			verProductos();
			
			System.out.print("\nIntroduzca el ID del producto: ");
			int idProducto = Integer.parseInt(scanner.nextLine());
			
			System.out.print("Cantidad a retirar: ");
			int cantidad = Integer.parseInt(scanner.nextLine());
			
			try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
                conn.setAutoCommit(false);
                try {
                    // 1. Verificar y actualizar el stock
                    String updateStock = "UPDATE productos SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";
                    try (PreparedStatement psUpdate = conn.prepareStatement(updateStock)) {
                        psUpdate.setInt(1, cantidad);
                        psUpdate.setInt(2, idProducto);
                        psUpdate.setInt(3, cantidad);
                        
                        int filasActualizadas = psUpdate.executeUpdate();
                        if (filasActualizadas == 0) {
                            throw new SQLException("No hay suficiente stock disponible o el producto no existe");
                        }
                    }

                    // 2. Registrar el movimiento en el histórico
                    StockManager.registrarMovimientoStock(conn, idProducto, StockManager.SALIDA, cantidad);
                    
                    conn.commit();
                    System.out.println("Salida de stock registrada correctamente.");
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
			} catch (SQLException e) {
				System.out.println("Error al registrar la salida de stock: " + e.getMessage());
			}
		} catch (NumberFormatException e) {
			System.out.println("Error: Por favor, introduzca números válidos.");
		}
	}
	
    /**
     * Exporta a un archivo JSON los productos que tienen stock bajo.
     * Permite al usuario especificar el límite de stock para considerar
     * que un producto tiene stock bajo.
     *
     * @param scanner Scanner para leer la entrada del usuario
     */
	private static void exportarProductosStockBajoJSON(Scanner scanner) {
		try {
			System.out.println("\n=== EXPORTAR PRODUCTOS CON STOCK BAJO A JSON ===");
			
			System.out.print("Introduzca el límite de stock (productos con stock menor a este valor): ");
			int limiteStock = Integer.parseInt(scanner.nextLine());
			
			try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
				// Exportar productos con stock bajo a JSON
				String rutaArchivo = "stock_bajo.json";
				JsonExporter.exportarProductosStockBajo(conn, limiteStock, rutaArchivo);
				System.out.println("Se han exportado los productos con stock inferior a " + limiteStock + 
                                 " al archivo " + rutaArchivo);
			} catch (SQLException e) {
				System.out.println("Error al obtener los productos: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("Error al escribir el archivo JSON: " + e.getMessage());
			}
		} catch (NumberFormatException e) {
			System.out.println("Error: Por favor, introduzca un número válido.");
		}
	}

    /**
     * Muestra el histórico de movimientos de stock para un producto específico.
     * Lista todos los movimientos (entradas y salidas) ordenados por fecha.
     *
     * @param scanner Scanner para leer la entrada del usuario
     */
	private static void verMovimientosStock(Scanner scanner) {
		try {
			System.out.println("\n=== VER MOVIMIENTOS DE STOCK ===");
			
			// Mostrar productos disponibles
			verProductos();
			
			System.out.print("\nIntroduzca el ID del producto: ");
			int idProducto = Integer.parseInt(scanner.nextLine());
			
			try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
				StockManager.consultarMovimientos(conn, idProducto);
			} catch (SQLException e) {
				System.out.println("Error al consultar los movimientos: " + e.getMessage());
			}
		} catch (NumberFormatException e) {
			System.out.println("Error: Por favor, introduzca un ID válido.");
		}
	}
	
    /**
     * Importa productos desde un archivo CSV a la base de datos.
     * Realiza una validación previa del archivo CSV y registra cualquier error
     * en un archivo de log. Solo procede con la importación si no hay errores.
     * El archivo CSV debe tener el formato: id_producto;nombre;categoria;precio;stock
     */
	private static void anadirDesdeCSV() {
		final String rutaCSV = "inventario.csv";
		final String rutaLog = "errores.log";   
		String lineaActual;
		int lineaNumero = 0;
		boolean hayErrores = false;
		
		try (BufferedReader br = new BufferedReader(new FileReader(rutaCSV));
			 BufferedWriter log = new BufferedWriter(new FileWriter(rutaLog, true));
			 Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
			
			// Desactivar el autocommit para usar transacciones
			conn.setAutoCommit(false);
			
			// Primera pasada: validación
			while ((lineaActual = br.readLine()) != null) {
				lineaNumero++;
				try {
					// Divide la línea en columnas usando punto y coma
					String[] columnas = lineaActual.split(";");
					
					if (columnas.length != 5) {
						throw new Exception("Número incorrecto de columnas: " + columnas.length);
					}
					
					// Validaciones adicionales
					if (lineaNumero > 1) { // Ignorar la primera línea (encabezados)
						try {
							// Validar que el ID sea un número
							Integer.parseInt(columnas[0].trim());
							
							// Validar que el nombre y categoría no estén vacíos
							if (columnas[1].trim().isEmpty() || columnas[2].trim().isEmpty()) {
								throw new Exception("El nombre y la categoría no pueden estar vacíos");
							}
							
							// Validar el formato del precio (debe contener números y opcionalmente '€' y ',')
							if (!columnas[3].trim().matches("^\\d+([,.]\\d{1,2})?€?$")) {
								throw new Exception("Formato de precio inválido: " + columnas[3]);
							}
							
							// Validar que el stock sea un número positivo
							int stock = Integer.parseInt(columnas[4].trim());
							if (stock < 0) {
								throw new Exception("El stock no puede ser negativo: " + stock);
							}
						} catch (NumberFormatException e) {
							throw new Exception("Error en formato numérico: " + e.getMessage());
						}
					}
					
				} catch (Exception e) {
					// Registrar error en archivo log
					log.write("Error en línea " + lineaNumero + ": " + e.getMessage());
					log.newLine();
					log.write("Contenido: " + lineaActual);
					log.newLine();
					log.write("--------------------------------------------------");
					log.newLine();
					hayErrores = true;
				}
			}
			
			if (!hayErrores) {
				System.out.println("El archivo CSV es correcto. Añadiendo productos a la base de datos...");
				
				try {
					// Segunda pasada: inserción en la base de datos
					try (BufferedReader br2 = new BufferedReader(new FileReader(rutaCSV))) {
						// Saltar la primera línea (encabezados)
						br2.readLine();
						
						String sql = "INSERT INTO productos (id_producto, nombre, categoria, precio, stock) VALUES (?, ?, ?, ?, ?)";
						PreparedStatement ps = conn.prepareStatement(sql);
						
						while ((lineaActual = br2.readLine()) != null) {
							String[] columnas = lineaActual.split(";");
							
							ps.setInt(1, Integer.parseInt(columnas[0].trim())); //id_producto
							ps.setString(2, columnas[1].trim()); // nombre
							ps.setString(3, columnas[2].trim()); // categoria
							ps.setString(4, columnas[3].trim()); // precio
							ps.setInt(5, Integer.parseInt(columnas[4].trim())); // stock
							
							ps.executeUpdate();
						}
						
						// Si llegamos aquí sin errores, confirmamos la transacción
						conn.commit();
						System.out.println("Productos añadidos correctamente.");
					}
				} catch (Exception e) {
					// Si hay cualquier error durante la inserción, hacemos rollback
					conn.rollback();
					System.out.println("Error durante la importación. Se ha revertido la operación.");
					System.out.println("Error: " + e.getMessage());
					
					// Registrar el error en el log
					log.write("Error durante la importación: " + e.getMessage());
					log.newLine();
					log.write("Se ha realizado rollback de la transacción.");
					log.newLine();
					log.write("--------------------------------------------------");
					log.newLine();
				}
			} else {
				System.out.println("Se encontraron errores en el archivo CSV. Revise el archivo de log para más detalles.");
			}
			
		} catch (IOException e) {
			System.out.println("Error al leer el archivo CSV o escribir en el log: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("Error al conectar con la base de datos: " + e.getMessage());
		}
	}

	/**
     * Exporta todo el inventario a un archivo XML.
     * Solicita al usuario la ruta del archivo de salida y guarda todos los productos
     * en formato XML.
     *
     * @param scanner Scanner para leer la entrada del usuario
     */
	private static void exportarInventarioXML(Scanner scanner) {
		System.out.println("\n=== EXPORTAR INVENTARIO A XML ===");
		System.out.print("Introduzca la ruta del archivo XML de salida: ");
		String rutaXML = scanner.nextLine();

		try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
			XmlManager.exportToXml(conn, rutaXML);
			System.out.println("Inventario exportado correctamente a XML.");
		} catch (Exception e) {
			System.out.println("Error al exportar el inventario a XML: " + e.getMessage());
		}
	}

	/**
     * Importa el inventario desde un archivo XML.
     * Solicita al usuario la ruta del archivo XML y actualiza la base de datos
     * con los productos contenidos en el archivo.
     *
     * @param scanner Scanner para leer la entrada del usuario
     */
	private static void importarInventarioXML(Scanner scanner) {
		System.out.println("\n=== IMPORTAR INVENTARIO DESDE XML ===");
		System.out.print("Introduzca la ruta del archivo XML a importar: ");
		String rutaXML = scanner.nextLine();

		System.out.println("¡ADVERTENCIA! Esta operación eliminará todos los productos actuales.");
		System.out.print("¿Está seguro de que desea continuar? (s/n): ");
		String confirmacion = scanner.nextLine();

		if (confirmacion.toLowerCase().equals("s")) {
			try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
				XmlManager.importFromXml(conn, rutaXML);
				System.out.println("Inventario importado correctamente desde XML.");
			} catch (Exception e) {
				System.out.println("Error al importar el inventario desde XML: " + e.getMessage());
			}
		} else {
			System.out.println("Operación cancelada.");
		}
	}

	/**
     * Muestra los N productos más vendidos del inventario.
     * Solicita al usuario el número de productos a mostrar y presenta
     * un listado ordenado por cantidad total de ventas.
     *
     * @param scanner Scanner para leer la entrada del usuario
     */
    private static void verTopProductosMasVendidos(Scanner scanner) {
        try {
            System.out.println("\n=== VER TOP PRODUCTOS MÁS VENDIDOS ===");
            System.out.print("Introduzca el número de productos a mostrar: ");
            int limit = Integer.parseInt(scanner.nextLine());

            if (limit <= 0) {
                System.out.println("Error: El número debe ser mayor que 0");
                return;
            }

            try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
                StockManager.consultarProductosMasVendidos(conn, limit);
            } catch (SQLException e) {
                System.out.println("Error al consultar los productos más vendidos: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Debe introducir un número válido.");
        }
    }//verTopProductosMasVendidos

	/**
     * Muestra el valor total del stock agrupado por categorías.
     * Para cada categoría muestra el número total de productos y
     * el valor total del stock (precio * cantidad).
     */
    private static void verValorStockPorCategoria() {
        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
            StockManager.consultarValorStockPorCategoria(conn);
        } catch (SQLException e) {
            System.out.println("Error al consultar el valor del stock por categoría: " + e.getMessage());
        }
    }//verValorStockPorCategoria

	/**
     * Muestra el histórico de movimientos de stock dentro de un rango de fechas.
     * Solicita al usuario las fechas de inicio y fin del período a consultar.
     *
     * @param scanner Scanner para leer la entrada del usuario
     */
    private static void verMovimientosPorFecha(Scanner scanner) {
        try {
            System.out.println("\n=== VER MOVIMIENTOS POR RANGO DE FECHAS ===");
            System.out.println("Ingrese las fechas en formato YYYY-MM-DD");
            
            System.out.print("Fecha de inicio: ");
            String fechaInicio = scanner.nextLine();
            
            System.out.print("Fecha de fin: ");
            String fechaFin = scanner.nextLine();

            try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD)) {
                StockManager.consultarMovimientosPorFecha(conn, fechaInicio, fechaFin);
            } catch (SQLException e) {
                System.out.println("Error al consultar los movimientos: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Asegúrese de usar el formato YYYY-MM-DD (ejemplo: 2025-11-07)");
        }
    }//verMovimientosPorFecha

	private static void ejecutarExplain(Connection conn, String sql) {
		try (PreparedStatement ps = conn.prepareStatement("EXPLAIN " + sql)) {
			ResultSet rs = ps.executeQuery();
			
			System.out.println("\n📊 PLAN DE EJECUCIÓN (EXPLAIN):");
			while (rs.next()) {
				System.out.printf(
					"id=%s | select_type=%s | table=%s | type=%s | possible_keys=%s | key=%s | rows=%s | Extra=%s\n",
					rs.getString("id"),
					rs.getString("select_type"),
					rs.getString("table"),
					rs.getString("type"),
					rs.getString("possible_keys"),
					rs.getString("key"),
					rs.getString("rows"),
					rs.getString("Extra")
				);
			}
			System.out.println("---------------------------------------------");
		} catch (SQLException e) {
			System.out.println("Error ejecutando EXPLAIN: " + e.getMessage());
		}
	}

}//Main class