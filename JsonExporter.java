import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase utilitaria para exportar datos de la base de datos a formato JSON.
 * Permite exportar consultas específicas a archivos JSON formateados.
 * No requiere bibliotecas externas de JSON, utiliza StringBuilder para la generación.
 */
public class JsonExporter {
    /**
     * Exporta los productos con stock bajo a un archivo JSON.
     * 
     * @param conn Conexión a la base de datos
     * @param limiteStock Stock máximo para considerar un producto como "stock bajo"
     * @param rutaArchivo Ruta donde se guardará el archivo JSON
     * @throws SQLException Si hay un error al acceder a la base de datos
     * @throws IOException Si hay un error al escribir el archivo
     */
    public static void exportarProductosStockBajo(Connection conn, int limiteStock, String rutaArchivo) 
            throws SQLException, IOException {
        StringBuilder json = new StringBuilder();
        String sql = "SELECT * FROM productos WHERE stock < ?";
        List<String> productos = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limiteStock);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String producto = String.format("""
                        {
                            "id_producto": %d,
                            "nombre": "%s",
                            "categoria": "%s",
                            "precio": "%s",
                            "stock": %d
                        }""",
                        rs.getInt("id_producto"),
                        rs.getString("nombre").replace("\"", "\\\""),
                        rs.getString("categoria").replace("\"", "\\\""),
                        rs.getString("precio").replace("\"", "\\\""),
                        rs.getInt("stock")
                    );
                    productos.add(producto);
                }
            }
        }

        // Construir el JSON array
        json.append("[\n");
        for (int i = 0; i < productos.size(); i++) {
            json.append("    ").append(productos.get(i));
            if (i < productos.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]");

        // Escribir el JSON al archivo
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            writer.write(json.toString());
        }
    }
}