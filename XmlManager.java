import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase que gestiona la exportación e importación de datos en formato XML.
 * Proporciona funcionalidad para:
 * - Exportar todo el inventario a un archivo XML
 * - Importar el inventario desde un archivo XML
 */
public class XmlManager {
    
    /**
     * Exporta todo el inventario a un archivo XML.
     * 
     * @param conn Conexión a la base de datos
     * @param filePath Ruta del archivo XML de salida
     * @throws SQLException Si hay un error al acceder a la base de datos
     * @throws ParserConfigurationException Si hay un error al crear el documento XML
     * @throws TransformerException Si hay un error al escribir el XML
     */
    public static void exportToXml(Connection conn, String filePath) 
            throws SQLException, ParserConfigurationException, TransformerException {
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // Crear el elemento raíz
        Element rootElement = doc.createElement("inventario");
        doc.appendChild(rootElement);

        // Obtener todos los productos
        String query = "SELECT * FROM productos";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Element producto = doc.createElement("producto");
                rootElement.appendChild(producto);

                // Añadir los atributos del producto
                producto.setAttribute("id", String.valueOf(rs.getInt("id_producto")));
                
                Element nombre = doc.createElement("nombre");
                nombre.setTextContent(rs.getString("nombre"));
                producto.appendChild(nombre);

                Element categoria = doc.createElement("categoria");
                categoria.setTextContent(rs.getString("categoria"));
                producto.appendChild(categoria);

                Element precio = doc.createElement("precio");
                precio.setTextContent(rs.getString("precio"));
                producto.appendChild(precio);

                Element stock = doc.createElement("stock");
                stock.setTextContent(String.valueOf(rs.getInt("stock")));
                producto.appendChild(stock);
            }
        }

        // Escribir el contenido al archivo XML con formato
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        // Configurar el transformer para añadir sangría
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }

    /**
     * Valida un archivo XML contra su esquema XSD.
     * 
     * @param xmlPath Ruta del archivo XML a validar
     * @throws SAXException Si hay un error en la validación
     * @throws IOException Si hay un error al leer los archivos
     */
    private static void validateXMLSchema(String xmlPath) 
            throws SAXException, IOException {
        try {
            // Crear el validador de esquema
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = factory.newSchema(new File("inventario.xsd"));
            Validator validator = schema.newValidator();

            // Validar el archivo XML
            validator.validate(new StreamSource(new File(xmlPath)));
        } catch (SAXException e) {
            throw new SAXException("Error de validación XML: " + e.getMessage());
        }
    }

    /**
     * Importa el inventario desde un archivo XML.
     * 
     * @param conn Conexión a la base de datos
     * @param filePath Ruta del archivo XML a importar
     * @throws SQLException Si hay un error al acceder a la base de datos
     * @throws ParserConfigurationException Si hay un error al parsear el XML
     * @throws SAXException Si hay un error en el formato del XML
     * @throws IOException Si hay un error al leer el archivo
     */
    public static void importFromXml(Connection conn, String filePath) 
            throws SQLException, ParserConfigurationException, SAXException, IOException {
        
        // Validar el XML antes de importarlo
        validateXMLSchema(filePath);
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(filePath));
        doc.getDocumentElement().normalize();

        // Limpiar la tabla actual
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM productos")) {
            ps.executeUpdate();
        }

        // Obtener todos los productos del XML
        NodeList productList = doc.getElementsByTagName("producto");
        
        String insertQuery = "INSERT INTO productos (id_producto, nombre, categoria, precio, stock) VALUES (?, ?, ?, ?, ?)";
        
        for (int i = 0; i < productList.getLength(); i++) {
            Node node = productList.item(i);
            
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                
                try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                    ps.setInt(1, Integer.parseInt(element.getAttribute("id")));
                    ps.setString(2, element.getElementsByTagName("nombre").item(0).getTextContent());
                    ps.setString(3, element.getElementsByTagName("categoria").item(0).getTextContent());
                    ps.setString(4, element.getElementsByTagName("precio").item(0).getTextContent());
                    ps.setInt(5, Integer.parseInt(element.getElementsByTagName("stock").item(0).getTextContent()));
                    ps.executeUpdate();
                }
            }
        }
    }
}