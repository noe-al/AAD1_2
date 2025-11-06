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
}