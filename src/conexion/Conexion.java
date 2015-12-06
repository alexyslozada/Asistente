package conexion;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import objetos.Estructura;

public class Conexion {
    private Connection conexion = null;
    private DatabaseMetaData dbmd = null;
    private ResultSet rs = null;
    private static final Logger LOG = Logger.getLogger(Conexion.class.getName());

    public Conexion(String database, String user, String password){
        try{
            Class.forName("org.postgresql.Driver");
            Properties props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", password);
            props.setProperty("ssl", "true");

            conexion = DriverManager.getConnection("jdbc:postgresql://127.0.0.1/"+database, props);
            dbmd = conexion.getMetaData();

        } catch (SQLException | ClassNotFoundException cnfe){
            System.out.println("Error encontrado: "+cnfe.getMessage());
            LOG.log(Level.SEVERE, "Error encontrado: {0}", new Object[]{cnfe.getMessage()});
        } 
    }
    
    public List<String> getTablas() throws SQLException{
        List<String> tablas = new ArrayList<>();
        String types[] = {"TABLE"};
        rs = dbmd.getTables(null, "public", "%", types);
        while(rs.next()){
            tablas.add(rs.getString(3));
        }
        rs.close();
        return tablas;
    }
    
    public List<Estructura> getEstructura(String tabla) throws SQLException{
        List<Estructura> lista_estructura = new ArrayList<>();
        rs = dbmd.getColumns(null, null, tabla, "%");
        while(rs.next()){
            Estructura estructura = new Estructura();
            estructura.columna = rs.getString(4);
            estructura.tipo    = rs.getString(6);
            estructura.tamanio = rs.getInt(7);
            estructura.decimales = rs.getInt(9);
            estructura.aceptaNulos = rs.getInt(11);
            estructura.defecto = rs.getString(13);
            estructura.posicion = rs.getInt(17);
            estructura.aceptaNulos2 = rs.getString(18);
            estructura.autoincremento = rs.getString(23);
            lista_estructura.add(estructura);
        }
        rs.close();
        return lista_estructura;
    }
}
