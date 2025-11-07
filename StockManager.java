import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Clase que gestiona todas las operaciones relacionadas con el stock de productos.
 * Proporciona funcionalidad para:
 * - Crear la tabla de movimientos de stock
 * - Registrar entradas y salidas de stock
 * - Consultar el histórico de movimientos
 */
public class StockManager {
    // SQL para crear la tabla de movimientos si no existe
    private static final String CREATE_MOVIMIENTOS_TABLE = """
        CREATE TABLE IF NOT EXISTS movimientos_stock (
            id_movimiento INT PRIMARY KEY AUTO_INCREMENT,
            id_producto INT,
            tipo_movimiento ENUM('ENTRADA', 'SALIDA'),
            cantidad INT,
            fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
        )
    """;

    // Constantes para tipos de movimiento
    public static final String ENTRADA = "ENTRADA";
    public static final String SALIDA = "SALIDA";

    /**
     * Inicializa las tablas necesarias si no existen en la base de datos.
     * Crea la tabla movimientos_stock si no existe, que se utiliza para
     * registrar todos los movimientos de entrada y salida de productos.
     *
     * @param conn Conexión a la base de datos
     * @throws SQLException Si hay un error al crear la tabla
     */
    public static void initializeTables(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(CREATE_MOVIMIENTOS_TABLE)) {
            ps.executeUpdate();
        }
    }

    /**
     * Registra un movimiento de stock (entrada o salida) en el histórico.
     * Valida que el tipo de movimiento sea válido y que la cantidad sea positiva.
     *
     * @param conn Conexión a la base de datos
     * @param idProducto ID del producto al que se le registra el movimiento
     * @param tipoMovimiento Tipo de movimiento (debe ser ENTRADA o SALIDA)
     * @param cantidad Cantidad de unidades del movimiento (debe ser positiva)
     * @throws SQLException Si hay un error al registrar el movimiento
     * @throws IllegalArgumentException Si el tipo de movimiento no es válido o la cantidad es menor o igual a cero
     */
    public static void registrarMovimientoStock(Connection conn, int idProducto, String tipoMovimiento, int cantidad) throws SQLException {
        // Verificar que el tipo de movimiento sea válido
        if (!tipoMovimiento.equals(ENTRADA) && !tipoMovimiento.equals(SALIDA)) {
            throw new IllegalArgumentException("Tipo de movimiento no válido");
        }

        // Verificar que la cantidad sea positiva
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }

        // Registrar el movimiento
        String insertMovimiento = "INSERT INTO movimientos_stock (id_producto, tipo_movimiento, cantidad) VALUES (?, ?, ?)";
        try (PreparedStatement psInsertMov = conn.prepareStatement(insertMovimiento)) {
            psInsertMov.setInt(1, idProducto);
            psInsertMov.setString(2, tipoMovimiento);
            psInsertMov.setInt(3, cantidad);
            psInsertMov.executeUpdate();
        }
    }

    /**
     * Consulta y muestra el histórico de movimientos de un producto.
     * Los movimientos se muestran ordenados por fecha descendente (más recientes primero).
     * Para cada movimiento muestra: ID, tipo (entrada/salida), cantidad y fecha.
     *
     * @param conn Conexión a la base de datos
     * @param idProducto ID del producto del cual se quieren consultar los movimientos
     * @throws SQLException Si hay un error al consultar los movimientos
     */
    public static void consultarMovimientos(Connection conn, int idProducto) throws SQLException {
        String sql = "SELECT * FROM movimientos_stock WHERE id_producto = ? ORDER BY fecha_movimiento DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (var rs = ps.executeQuery()) {
                System.out.println("\n=== HISTÓRICO DE MOVIMIENTOS ===");
                while (rs.next()) {
                    System.out.println("ID Movimiento: " + rs.getInt("id_movimiento"));
                    System.out.println("Tipo: " + rs.getString("tipo_movimiento"));
                    System.out.println("Cantidad: " + rs.getInt("cantidad"));
                    System.out.println("Fecha: " + rs.getTimestamp("fecha_movimiento"));
                    System.out.println("------------------------");
                }
            }
        }
    }

    /**
     * Consulta el histórico de movimientos de stock dentro de un rango de fechas.
     * Muestra todos los movimientos ordenados por fecha, incluyendo el nombre del producto.
     *
     * @param conn Conexión a la base de datos
     * @param fechaInicio Fecha de inicio del rango (formato: YYYY-MM-DD)
     * @param fechaFin Fecha de fin del rango (formato: YYYY-MM-DD)
     * @throws SQLException Si hay un error al consultar los movimientos
     * @throws IllegalArgumentException Si el formato de las fechas es incorrecto
     */
    public static void consultarMovimientosPorFecha(Connection conn, String fechaInicio, String fechaFin) throws SQLException {
        // Validar el formato de las fechas (YYYY-MM-DD)
        if (!fechaInicio.matches("\\d{4}-\\d{2}-\\d{2}") || !fechaFin.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Formato de fecha incorrecto. Use YYYY-MM-DD");
        }

        String sql = """
            SELECT m.id_movimiento, p.nombre, p.categoria, m.tipo_movimiento, 
                   m.cantidad, m.fecha_movimiento
            FROM movimientos_stock m
            JOIN productos p ON m.id_producto = p.id_producto
            WHERE DATE(m.fecha_movimiento) BETWEEN ? AND ?
            ORDER BY m.fecha_movimiento DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            
            try (var rs = ps.executeQuery()) {
                System.out.println("\n=== HISTÓRICO DE MOVIMIENTOS POR FECHA ===");
                System.out.println("Período: " + fechaInicio + " hasta " + fechaFin);
                System.out.println("------------------------");
                
                while (rs.next()) {
                    System.out.println("ID Movimiento: " + rs.getInt("id_movimiento"));
                    System.out.println("Producto: " + rs.getString("nombre"));
                    System.out.println("Categoría: " + rs.getString("categoria"));
                    System.out.println("Tipo: " + rs.getString("tipo_movimiento"));
                    System.out.println("Cantidad: " + rs.getInt("cantidad"));
                    System.out.println("Fecha: " + rs.getTimestamp("fecha_movimiento"));
                    System.out.println("------------------------");
                }
            }
        }
    }

    /**
     * Consulta los N productos más vendidos basado en la cantidad total de salidas.
     * Para cada producto muestra: ID, nombre, descripción y cantidad total vendida.
     *
     * @param conn Conexión a la base de datos
     * @param limit Número de productos a mostrar (N)
     * @throws SQLException Si hay un error al consultar los productos
     * @throws IllegalArgumentException Si limit es menor o igual a 0
     */
    public static void consultarProductosMasVendidos(Connection conn, int limit) throws SQLException {
        if (limit <= 0) {
            throw new IllegalArgumentException("El límite debe ser mayor que 0");
        }

        String sql = """
            SELECT p.id_producto, p.nombre, p.categoria, p.precio,
                   COALESCE(SUM(m.cantidad), 0) as total_vendido
            FROM productos p
            LEFT JOIN movimientos_stock m ON p.id_producto = m.id_producto 
                AND m.tipo_movimiento = ?
            GROUP BY p.id_producto, p.nombre, p.categoria, p.precio
            ORDER BY total_vendido DESC
            LIMIT ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, SALIDA);
            ps.setInt(2, limit);
            
            try (var rs = ps.executeQuery()) {
                System.out.println("\n=== TOP " + limit + " PRODUCTOS MÁS VENDIDOS ===");
                while (rs.next()) {
                    System.out.println("ID Producto: " + rs.getInt("id_producto"));
                    System.out.println("Nombre: " + rs.getString("nombre"));
                    System.out.println("Categoría: " + rs.getString("categoria"));
                    System.out.println("Precio: " + rs.getString("precio"));
                    System.out.println("Total Vendido: " + rs.getInt("total_vendido"));
                    System.out.println("------------------------");
                }
            }
        }
    }

    /**
     * Calcula y muestra el total de stock por categoría.
     * Para cada categoría muestra:
     * - Nombre de la categoría
     * - Número de productos diferentes
     * - Cantidad total de unidades en stock
     *
     * @param conn Conexión a la base de datos
     * @throws SQLException Si hay un error al consultar los datos
     */
    public static void consultarValorStockPorCategoria(Connection conn) throws SQLException {
        String sql = """
            SELECT 
                categoria,
                COUNT(*) as total_productos,
                SUM(stock) as total_stock
            FROM productos
            GROUP BY categoria
            ORDER BY total_stock DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (var rs = ps.executeQuery()) {
                System.out.println("\n=== TOTAL DE STOCK POR CATEGORÍA ===");
                while (rs.next()) {
                    System.out.println("Categoría: " + rs.getString("categoria"));
                    System.out.println("Productos Diferentes: " + rs.getInt("total_productos"));
                    System.out.println("Total Unidades en Stock: " + rs.getInt("total_stock"));
                    System.out.println("------------------------");
                }
            }
        }
    }
}