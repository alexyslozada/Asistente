/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asistente;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Vector;

/**
 *
 * @author Alexys
 */
public class Asistente extends JFrame {

    private final JLabel jlTitulo = new JLabel("Asistente para Formularios");
    private final JLabel jlBase = new JLabel("Base de Datos:");
    private final JLabel jlTabla = new JLabel("Tabla:");
    private final JLabel jlNombreArchivo = new JLabel("Nombre del Archivo:");
    private final JLabel jlTituloPanel = new JLabel("Titulo del Panel:");
    private final JLabel jlDescripcionPanel = new JLabel("Descripcion del Panel:");
    private final JLabel jlNombreObjeto = new JLabel("Nombre del Objeto:");

    private final JComboBox jcBase = new JComboBox();
    private final JComboBox jcTabla = new JComboBox();

    private final JTextField jtNombreArchivo = new JTextField(30);
    private final JTextField jtTituloPanel = new JTextField(50);
    private final JTextField jtDescripcionPanel = new JTextField(50);
    private final JTextField jtNombreObjeto = new JTextField(10);

    private final JButton jbEscoje = new JButton("Seleccione Ubicacion");
    private final JButton jbGenerar = new JButton("Generar Archivo Frame");
    private final JButton jbGenSQL = new JButton("Generar funciones SQL");

    private final JPanel jpPanel = new JPanel();

    private Connection conexion;
    private Vector<String> vBases = new Vector<String>();
    private Vector<String> vTablas = new Vector<String>();

    // Este es para generar el archivo plano
    File archivo = null;
    PrintWriter archivoAGenerar = null;

    int columnas; // Este guarda la cantidad de columnas de la tabla
    String nombreColumna[];  // Este guarda el nombre de las columnas de la tabla
    int tamanioColumna[];  // Este guarda el tamanio de la columna
    String tipoColumna[];  // Este guarda el tipo de columna

    public Asistente() {

        super("Asistente para Formularios");

        jbEscoje.addActionListener(new SeleccionaArchivo());
        llenaBases();
        jcBase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                llenaTablas((String) jcBase.getSelectedItem());
            }
        });
        jbGenerar.addActionListener(new GeneraPlano());
        jbGenSQL.addActionListener(new GeneraSQL());

        jpPanel.setLayout(new GridLayout(8, 2));
        jpPanel.add(jlTitulo);
        jpPanel.add(new JLabel(""));
        jpPanel.add(jlBase);
        jpPanel.add(jcBase);
        jpPanel.add(jlTabla);
        jpPanel.add(jcTabla);
        //jpPanel.add(jlNombreArchivo);
        jpPanel.add(jbEscoje);
        jpPanel.add(jtNombreArchivo);
        jpPanel.add(jlTituloPanel);
        jpPanel.add(jtTituloPanel);
        jpPanel.add(jlDescripcionPanel);
        jpPanel.add(jtDescripcionPanel);
        jpPanel.add(jlNombreObjeto);
        jpPanel.add(jtNombreObjeto);
        jpPanel.add(jbGenerar);
        jpPanel.add(jbGenSQL);

        setLayout(new BorderLayout());
        add(jpPanel, BorderLayout.NORTH);
        setPreferredSize(new Dimension(400, 300));
        pack();
        setVisible(true);
    }

    private void llenaBases() {
        try {
            Class.forName("org.postgresql.Driver");
            conexion = DriverManager.getConnection("jdbc:postgresql://127.0.0.1/Tampa", "usringenio", "usringenio");
        } catch (ClassNotFoundException cnfe) {
            JOptionPane.showMessageDialog(null, "Error de cnfe: " + cnfe, "Error de clase", JOptionPane.DEFAULT_OPTION);
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(null, "Error de sqle: " + sqle, "Error de sql", JOptionPane.DEFAULT_OPTION);
        }

        try {
            Statement sentencia = conexion.createStatement();
            String consulta = "select datname from pg_database";
            ResultSet resultados = sentencia.executeQuery(consulta);

            while (resultados.next()) {
                vBases.add(resultados.getString("datname"));
                jcBase.addItem(resultados.getString("datname"));
            }

            resultados.close();
            sentencia.close();
            conexion.close();
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(null, "Error de sql: " + sqle, "Error de sql", JOptionPane.DEFAULT_OPTION);
        }
    }

    private void llenaTablas(String baseOrigen) {
        jcTabla.removeAllItems();
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://127.0.0.1/" + baseOrigen;
            conexion = DriverManager.getConnection(url, "usringenio", "usringenio");
        } catch (ClassNotFoundException cnfe) {
            JOptionPane.showMessageDialog(null, "Error de cnfe: " + cnfe, "Error de clase", JOptionPane.DEFAULT_OPTION);
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(null, "Error de sqle: " + sqle, "Error de sql", JOptionPane.DEFAULT_OPTION);
        }

        try {
            Statement sentencia = conexion.createStatement();
            String consulta = "SELECT c.relname "
                    + "FROM pg_catalog.pg_class c JOIN pg_catalog.pg_roles r ON r.oid = c.relowner "
                    + "LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace "
                    + "WHERE c.relkind ='r' AND n.nspname NOT IN ('pg_catalog', 'pg_toast') "
                    + "AND pg_catalog.pg_table_is_visible(c.oid) ORDER BY 1";
            ResultSet resultados = sentencia.executeQuery(consulta);

            while (resultados.next()) {
                vTablas.add(resultados.getString(1));
                jcTabla.addItem(resultados.getString(1));
            }

            resultados.close();
            sentencia.close();
            conexion.close();
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(null, "Error de sqle: " + sqle, "Error de sql", JOptionPane.DEFAULT_OPTION);
        }
    }

    private class GeneraSQL implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Class.forName("org.postgresql.Driver");
                String url = "jdbc:postgresql://127.0.0.1/" + jcBase.getSelectedItem();
                conexion = DriverManager.getConnection(url, "usringenio", "usringenio");
            } catch (ClassNotFoundException cnfe) {
                JOptionPane.showMessageDialog(null, "Error de cnfe: " + cnfe, "Error de clase", JOptionPane.DEFAULT_OPTION);
            } catch (SQLException sqle) {
                JOptionPane.showMessageDialog(null, "Error de sqle: " + sqle, "Error de sql", JOptionPane.DEFAULT_OPTION);
            }

            try {
                Statement sentencia = conexion.createStatement();
                String consulta = "select * from  " + jcTabla.getSelectedItem() + " limit 1";
                ResultSet resultados = sentencia.executeQuery(consulta);
                ResultSetMetaData estructura = resultados.getMetaData();
                columnas = estructura.getColumnCount();
                nombreColumna = new String[columnas];
                tamanioColumna = new int[columnas];
                tipoColumna = new String[columnas];
                for (int i = 0; i < columnas; i++) {
                    nombreColumna[i] = estructura.getColumnName(i + 1);
                    tamanioColumna[i] = estructura.getPrecision(i + 1);
                    tipoColumna[i] = estructura.getColumnTypeName(i + 1);
                }
            } catch (SQLException sqle) {
                JOptionPane.showMessageDialog(null, "Error de sqle: " + sqle, "Error de sql", JOptionPane.DEFAULT_OPTION);
            }
            // Genera el contenido
            try {
                StringBuilder sb = new StringBuilder();
                StringBuilder sbt = new StringBuilder();
                // FUNCION INSERTAR
                sb.append("CREATE OR REPLACE FUNCTION ");
                sbt.append("fn_");
                sbt.append(jcTabla.getSelectedItem());
                sbt.append("_ins(");
                for (int i = 1; i < columnas; i++) {
                    if (tipoColumna[i].equals("int2")) {
                        sbt.append("smallint");
                    }
                    if (tipoColumna[i].equals("int4") || tipoColumna[i].equals("serial")) {
                        sbt.append("integer");
                    }
                    if (tipoColumna[i].equals("varchar")) {
                        sbt.append("character varying");
                    }
                    if (tipoColumna[i].equals("timastamp")) {
                        sbt.append("timestamp");
                    }
                    if (tipoColumna[i].equals("bool")) {
                        sbt.append("boolean");
                    }
                    if (i < columnas - 1) {
                        sbt.append(", ");
                    }
                    //System.out.println(tipoColumna[i]);
                }
                sbt.append(")");
                sb.append(sbt);
                sb.append("\n");
                sb.append("RETURNS smallint AS\n");
                sb.append("$BODY$\n");
                sb.append("declare\n");
                for (int i = 1; i < nombreColumna.length; i++) {
                    sb.append("\t_");
                    sb.append(nombreColumna[i]);
                    sb.append(" alias for $");
                    sb.append((i));
                    sb.append(";\n");
                }
                sb.append("begin\n");
                sb.append("\tinsert into ");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" values (default, ");
                for (int i = 1; i < nombreColumna.length; i++) {
                    sb.append("_");
                    sb.append(nombreColumna[i]);
                    if (i < nombreColumna.length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(");\n");
                sb.append("\tif found then\n");
                sb.append("\t\treturn currval('sqc_");
                sb.append(jcTabla.getSelectedItem());
                sb.append("'); -- Se ingreso correctamente el registro\n");
                sb.append("\telse\n");
                sb.append("\t\treturn 0; -- Error de la base de datos\n");
                sb.append("\tend if;\n");
                sb.append("exception\n");
                sb.append("\twhen foreign_key_violation then\n");
                sb.append("\t\treturn -1; -- Error de violacion de FK\n");
                sb.append("\twhen unique_violation then\n");
                sb.append("\t\treturn -2; -- Error de violacion de UK\n");
                sb.append("end;\n");
                sb.append("$BODY$\n");
                sb.append("\tLANGUAGE plpgsql VOLATILE\n");
                sb.append("\tCOST 100;\n");
                sb.append("ALTER FUNCTION ");
                sb.append(sbt);
                sb.append("\n");
                sb.append("OWNER TO usringenio;\n");
                sb.append("COMMENT ON FUNCTION ");
                sb.append(sbt);
                sb.append(" IS 'Funcion que permite insertar ");
                sb.append(jcTabla.getSelectedItem());
                sb.append("'");
                sb.append("\n");
                //FUNCION SELECCIONAR
                sb.append("CREATE OR REPLACE FUNCTION ");
                sbt = new StringBuilder();
                sbt.append("fn_");
                sbt.append(jcTabla.getSelectedItem());
                sbt.append("_sel(smallint, ");
                if (tipoColumna[0].equals("int2")) {
                    sbt.append("smallint");
                }
                if (tipoColumna[0].equals("int4") || tipoColumna[0].equals("serial")) {
                    sbt.append("integer");
                }
                sbt.append(")\n");
                sb.append(sbt);
                sb.append("RETURN SETOF ");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" AS\n");
                sb.append("$BODY$\n");
                sb.append("declare\n");
                sb.append("\t_tipo alias for $1; -- Tipo de busqueda\n");
                sb.append("\t_");
                sb.append(nombreColumna[0]);
                sb.append(" alias for $2; -- Id de la tabla\n");
                sb.append("\t consulta TEXT = 'select ");
                for (int i = 0; i < nombreColumna.length; i++) {
                    sb.append(nombreColumna[i]);
                    if (i < nombreColumna.length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(" from ");
                sb.append(jcTabla.getSelectedItem());
                sb.append("';\n");
                sb.append("begin\n");
                sb.append("\tif _tipo = 0 then -- Consulta todos los registros\n");
                sb.append("\t\tconsulta = consulta;\n");
                sb.append("\telsif _tipo = 1 then -- Consulta por el id de la tabla\n");
                sb.append("\t\tconsulta = consulta || ' where ");
                sb.append(nombreColumna[0]);
                sb.append(" = ' || ");
                sb.append("_");
                sb.append(nombreColumna[0]);
                sb.append(";\n");
                sb.append("\tend if;\n");
                sb.append("\treturn query execute consulta;\n");
                sb.append("end;\n");
                sb.append("$BODY$\n");
                sb.append("\tLANGUAGE plpgsql VOLATILE\n");
                sb.append("\tCOST 100;\n");
                sb.append("ALTER FUNCTION ");
                sb.append(sbt);
                sb.append("OWNER TO usringenio;\n");
                sb.append("COMMENT ON FUNCTION ");
                sb.append(sbt);
                sb.append(" IS 'Funcion que permite consultar ");
                sb.append(jcTabla.getSelectedItem());
                sb.append("'");
                sb.append("\n");
                //FUNCION ACTUALIZAR
                sb.append("CREATE OR REPLACE FUNCTION ");
                sbt = new StringBuilder();
                sbt.append("fn_");
                sbt.append(jcTabla.getSelectedItem());
                sbt.append("_upd(");
                for (int i = 0; i < columnas; i++) {
                    if (tipoColumna[i].equals("int2")) {
                        sbt.append("smallint");
                    }
                    if (tipoColumna[i].equals("int4") || tipoColumna[i].equals("serial")) {
                        sbt.append("integer");
                    }
                    if (tipoColumna[i].equals("varchar")) {
                        sbt.append("character varying");
                    }
                    if (tipoColumna[i].equals("timastamp")) {
                        sbt.append("timestamp");
                    }
                    if (tipoColumna[i].equals("bool")) {
                        sbt.append("boolean");
                    }
                    if (i < columnas - 1) {
                        sbt.append(", ");
                    }
                    //System.out.println(tipoColumna[i]);
                }
                sbt.append(")\n");
                sb.append(sbt);
                sb.append("RETURNS smallint AS\n");
                sb.append("$BODY$\n");
                sb.append("declare\n");
                for (int i = 0; i < nombreColumna.length; i++) {
                    sb.append("\t_");
                    sb.append(nombreColumna[i]);
                    sb.append(" alias for $");
                    sb.append((i + 1));
                    sb.append(";\n");
                }
                sb.append("BEGIN\n");
                sb.append("\tupdate ");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" set ");
                for (int i = 1; i < nombreColumna.length; i++) {
                    sb.append(nombreColumna[i]);
                    sb.append(" = _");
                    sb.append(nombreColumna[i]);
                    if (i < nombreColumna.length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(" where ");
                sb.append(nombreColumna[0]);
                sb.append(" = ");
                sb.append("_");
                sb.append(nombreColumna[0]);
                sb.append(";\n");
                sb.append("\tif found then\n");
                sb.append("\t\treturn 1; -- Se actualizo correctamente\n");
                sb.append("\telse\n");
                sb.append("\t\treturn 0; -- Error de la BD\n");
                sb.append("\tend if;\n");
                sb.append("exception\n");
                sb.append("\twhen foreign_key_violation then\n");
                sb.append("\t\treturn -1; -- Error de violacion de FK\n");
                sb.append("\twhen unique_violation then\n");
                sb.append("\t\treturn -2; -- Error de violacion de UK\n");
                sb.append("end;\n");
                sb.append("$BODY$\n");
                sb.append("\tLANGUAGE plpgsql VOLATILE\n");
                sb.append("\tCOST 100;\n");
                sb.append("ALTER FUNCTION ");
                sb.append(sbt);
                sb.append("OWNER TO usringenio;\n");
                sb.append("COMMENT ON FUNCTION ");
                sb.append(sbt);
                sb.append(" IS 'Funcion que permite actualizar ");
                sb.append(jcTabla.getSelectedItem());
                sb.append("'");
                sb.append("\n");
                //FUNCION PARA BORRAR
                sb.append("CREATE OR REPLACE FUNCTION ");
                sbt = new StringBuilder();
                sbt.append("fn_");
                sbt.append(jcTabla.getSelectedItem());
                sbt.append("_del(");
                if (tipoColumna[0].equals("int2")) {
                    sbt.append("smallint");
                }
                if (tipoColumna[0].equals("int4") || tipoColumna[0].equals("serial")) {
                    sbt.append("integer");
                }
                sbt.append(")\n");
                sb.append(sbt);
                sb.append("RETURNS smallint AS\n");
                sb.append("$BODY$\n");
                sb.append("declare\n");
                sb.append("\t_");
                sb.append(nombreColumna[0]);
                sb.append(" alias for $1; -- Id de la tabla\n");
                sb.append("BEGIN\n");
                sb.append("\tdelete from ");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" where ");
                sb.append(nombreColumna[0]);
                sb.append(" = _");
                sb.append(nombreColumna[0]);
                sb.append(";\n");
                sb.append("\tif found then\n");
                sb.append("\t\treturn 1; -- Se elimino correctamente el registro\n");
                sb.append("\telse\n");
                sb.append("\t\treturn 0; -- Error de la base de datos\n");
                sb.append("\tend if;\n");
                sb.append("exception\n");
                sb.append("\twhen foreign_key_violation then\n");
                sb.append("\t\treturn -1; -- Error de violacion de FK\n");
                sb.append("end;\n");
                sb.append("$BODY$\n");
                sb.append("\tLANGUAGE plpgsql VOLATILE\n");
                sb.append("\tCOST 100;\n");
                sb.append("ALTER FUNCTION ");
                sb.append(sbt);
                sb.append("OWNER TO usringenio;\n");
                sb.append("COMMENT ON FUNCTION ");
                sb.append(sbt);
                sb.append(" IS 'Funcion que permite actualizar ");
                sb.append(jcTabla.getSelectedItem());
                sb.append("'");
                sb.append("\n");
                //CREA OBJETO JAVA
                sb.append("\n");
                sb.append("//OBJETO JAVA\n");
                sb.append("package objetos.ingenioti.org;\n\n");
                sb.append("import interfaces.ingenioti.org.IObjetoHci;\n\n");
                sb.append("public class O");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" implements IObjetoHci{\n");
                for (int i = 0; i < columnas; i++) {
                    sb.append("\tprivate ");
                    if (tipoColumna[i].equals("int2")) {
                        sb.append("short");
                    }
                    if (tipoColumna[i].equals("int4") || tipoColumna[i].equals("serial")) {
                        sb.append("int");
                    }
                    if (tipoColumna[i].equals("varchar")) {
                        sb.append("String");
                    }
                    if (tipoColumna[i].equals("timastamp")) {
                        sb.append("Calendar");
                    }
                    if (tipoColumna[i].equals("bool")) {
                        sb.append("boolean");
                    }
                    sb.append(" ");
                    sb.append(nombreColumna[i]);
                    sb.append(";\n");
                }
                sb.append("\t@Override\n");
                sb.append("\tpublic String getDescripcion(){}\n");
                sb.append("\t@Override\n");
                sb.append("\tpublic String getXML(){}\n");
                sb.append("}");
                //NEGOCIO
                sb.append("\n");
                sb.append("//NEGOCIO\n");
                sb.append("package negocio.ingenioti.org;\n");
                sb.append("import java.sql.SQLException;\n");
                sb.append("import java.util.ArrayList;\n");
                sb.append("public class N");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" extends NGeneralidades {\n");
                //INSERTAR
                sb.append("\tpublic Short insertar(O");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" obj){\n");
                sb.append("\t\tShort respuesta = 0;\n");
                sb.append("\t\ttry{\n");
                sb.append("\t\t\tconectar(\"select * from fn_");
                sb.append(jcTabla.getSelectedItem());
                sb.append("_ins(");
                for (int i = 1; i < columnas; i++) {
                    sb.append("?");
                    if (i < columnas - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(")\");\n");
                for (int i = 1; i < columnas; i++) {
                    sb.append("\t\t\tsentenciaProcedimiento.set");
                    if (tipoColumna[i].equals("int2")) {
                        sb.append("Short");
                    }
                    if (tipoColumna[i].equals("int4") || tipoColumna[i].equals("serial")) {
                        sb.append("Integer");
                    }
                    if (tipoColumna[i].equals("varchar")) {
                        sb.append("String");
                    }
                    if (tipoColumna[i].equals("timastamp")) {
                        sb.append("Date");
                    }
                    if (tipoColumna[i].equals("bool")) {
                        sb.append("Boolean");
                    }
                    sb.append("(");
                    sb.append(i);
                    sb.append(", obj.get");
                    sb.append(nombreColumna[i]);
                    sb.append("());\n");
                }
                sb.append("\t\t\tgetResultadosProcedimiento();\n");
                sb.append("\t\t\tif (resultados.next()) {\n");
                sb.append("\t\t\t\trespuesta = resultados.getShort(1);\n");
                sb.append("\t\t\t}\n");
                sb.append("\t\t} catch (SQLException sql) {\n");
                sb.append("\t\t\tSystem.err.println(\"Error en N");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" insertar: \" + sql.getMessage());\n");
                sb.append("\t\t} finally {\n");
                sb.append("\t\t\ttry{\n");
                sb.append("\t\t\t\tcerrarConexion();\n");
                sb.append("\t\t\t} catch (SQLException sqle){}\n");
                sb.append("\t\t}\n");
                sb.append("\t\treturn respuesta;\n");
                sb.append("\t}\n");
                //ACTUALIZAR
                sb.append("\tpublic Short actualizar(O");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" obj){\n");
                sb.append("\t\tShort respuesta = 0;\n");
                sb.append("\t\ttry{\n");
                sb.append("\t\t\tconectar(\"select * from fn_");
                sb.append(jcTabla.getSelectedItem());
                sb.append("_upd(");
                for (int i = 0; i < columnas; i++) {
                    sb.append("?");
                    if (i < columnas - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(")\");\n");
                for (int i = 0; i < columnas; i++) {
                    sb.append("\t\t\tsentenciaProcedimiento.set");
                    if (tipoColumna[i].equals("int2")) {
                        sb.append("Short");
                    }
                    if (tipoColumna[i].equals("int4") || tipoColumna[i].equals("serial")) {
                        sb.append("Integer");
                    }
                    if (tipoColumna[i].equals("varchar")) {
                        sb.append("String");
                    }
                    if (tipoColumna[i].equals("timastamp")) {
                        sb.append("Date");
                    }
                    if (tipoColumna[i].equals("bool")) {
                        sb.append("Boolean");
                    }
                    sb.append("(");
                    sb.append(i+1);
                    sb.append(", obj.get");
                    sb.append(nombreColumna[i]);
                    sb.append("());\n");
                }
                sb.append("\t\t\tgetResultadosProcedimiento();\n");
                sb.append("\t\t\tif (resultados.next()) {\n");
                sb.append("\t\t\t\trespuesta = resultados.getShort(1);\n");
                sb.append("\t\t\t}\n");
                sb.append("\t\t} catch (SQLException sql) {\n");
                sb.append("\t\t\tSystem.err.println(\"Error en N");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" actualizar: \" + sql.getMessage());\n");
                sb.append("\t\t} finally {\n");
                sb.append("\t\t\ttry{\n");
                sb.append("\t\t\t\tcerrarConexion();\n");
                sb.append("\t\t\t} catch (SQLException sqle){}\n");
                sb.append("\t\t}\n");
                sb.append("\t\treturn respuesta;\n");
                sb.append("\t}\n");
                //BORRAR
                sb.append("\tpublic Short borrar(O");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" obj){\n");
                sb.append("\t\tShort respuesta = 0;\n");
                sb.append("\t\ttry{\n");
                sb.append("\t\t\tconectar(\"select * from fn_");
                sb.append(jcTabla.getSelectedItem());
                sb.append("_del(?)\");\n");
                sb.append("\t\t\tsentenciaProcedimiento.set");
                if (tipoColumna[0].equals("int2")) {
                    sb.append("Short");
                }
                if (tipoColumna[0].equals("int4") || tipoColumna[0].equals("serial")) {
                    sb.append("Integer");
                }
                sb.append("(1, obj.get");
                sb.append(nombreColumna[0]);
                sb.append("());\n");
                sb.append("\t\t\tgetResultadosProcedimiento();\n");
                sb.append("\t\t\tif (resultados.next()) {\n");
                sb.append("\t\t\t\trespuesta = resultados.getShort(1);\n");
                sb.append("\t\t\t}\n");
                sb.append("\t\t} catch (SQLException sql) {\n");
                sb.append("\t\t\tSystem.err.println(\"Error en N");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" borrar: \" + sql.getMessage());\n");
                sb.append("\t\t} finally {\n");
                sb.append("\t\t\ttry{\n");
                sb.append("\t\t\t\tcerrarConexion();\n");
                sb.append("\t\t\t} catch (SQLException sqle){}\n");
                sb.append("\t\t}\n");
                sb.append("\t\treturn respuesta;\n");
                sb.append("\t}\n");
                //CONSULTAR
                sb.append("\tpublic ArrayList<O");
                sb.append(jcTabla.getSelectedItem());
                sb.append("> consultar(Short tc, O");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" obj){\n");
                sb.append("\t\tArrayList<O");
                sb.append(jcTabla.getSelectedItem());
                sb.append("> lista = new ArrayList<O");
                sb.append(jcTabla.getSelectedItem());
                sb.append(">();\n");
                sb.append("\t\ttry{\n");
                sb.append("\t\t\tconectar(\"select * from fn_");
                sb.append(jcTabla.getSelectedItem());
                sb.append("_sel(?,?)\");\n");
                sb.append("\t\t\tsentenciaProcedimiento.setShort(1, tc);\n");
                sb.append("\t\t\tsentenciaProcedimiento.set");
                if (tipoColumna[1].equals("int2")) {
                    sb.append("Short");
                }
                if (tipoColumna[1].equals("int4") || tipoColumna[1].equals("serial")) {
                    sb.append("Integer");
                }
                sb.append("(2, obj.get");
                sb.append(nombreColumna[0]);
                sb.append("());\n");
                sb.append("\t\t\tgetResultadosProcedimiento();\n");
                sb.append("\t\t\twhile(resultados.next()){\n");
                sb.append("\t\t\t\tO");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" temp = new O");
                sb.append(jcTabla.getSelectedItem());
                sb.append("();\n");
                for(int i = 0; i < columnas; i++){
                    sb.append("\t\t\t\ttemp.set");
                    sb.append(nombreColumna[i]);
                    sb.append("(resultados.get");
                    if (tipoColumna[i].equals("int2")) {
                        sb.append("Short");
                    }
                    if (tipoColumna[i].equals("int4") || tipoColumna[i].equals("serial")) {
                        sb.append("Integer");
                    }
                    if (tipoColumna[i].equals("varchar")) {
                        sb.append("String");
                    }
                    if (tipoColumna[i].equals("timastamp")) {
                        sb.append("Date");
                    }
                    if (tipoColumna[i].equals("bool")) {
                        sb.append("Boolean");
                    }
                    sb.append("(");
                    sb.append(i+1);
                    sb.append("));\n");
                }
                sb.append("\t\t\t\tlista.add(temp);\n");
                sb.append("\t\t\t}\n");
                sb.append("\t\t} catch (SQLException sql) {\n");
                sb.append("\t\t\tSystem.err.println(\"Error en N");
                sb.append(jcTabla.getSelectedItem());
                sb.append(" consultar: \" + sql.getMessage());\n");
                sb.append("\t\t} finally {\n");
                sb.append("\t\t\ttry{\n");
                sb.append("\t\t\t\tcerrarConexion();\n");
                sb.append("\t\t\t} catch (SQLException sqle){}\n");
                sb.append("\t\t}\n");
                sb.append("\t\treturn lista;\n");
                sb.append("\t}");

                archivo = new File(jtNombreArchivo.getText().trim() + ".sql");
                archivoAGenerar = new PrintWriter(new FileWriter(archivo));
                archivoAGenerar.println(sb);
                archivoAGenerar.close();
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(null, "Error de io: " + ioe, "Error de io", JOptionPane.DEFAULT_OPTION);
            }
            JOptionPane.showMessageDialog(null, "Proceso Terminado!!!", "Proceso Terminado", JOptionPane.DEFAULT_OPTION);
        }
    }

    private class GeneraPlano implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Class.forName("org.postgresql.Driver");
                String url = "jdbc:postgresql://127.0.0.1/" + jcBase.getSelectedItem();
                conexion = DriverManager.getConnection(url, "usringenio", "usringenio");
            } catch (ClassNotFoundException cnfe) {
                JOptionPane.showMessageDialog(null, "Error de cnfe: " + cnfe, "Error de clase", JOptionPane.DEFAULT_OPTION);
            } catch (SQLException sqle) {
                JOptionPane.showMessageDialog(null, "Error de sqle: " + sqle, "Error de sql", JOptionPane.DEFAULT_OPTION);
            }

            try {
                Statement sentencia = conexion.createStatement();
                String consulta = "select * from  " + jcTabla.getSelectedItem() + " limit 1";
                ResultSet resultados = sentencia.executeQuery(consulta);
                ResultSetMetaData estructura = resultados.getMetaData();
                columnas = estructura.getColumnCount();
                nombreColumna = new String[columnas];
                tamanioColumna = new int[columnas];
                tipoColumna = new String[columnas];
                for (int i = 0; i < columnas; i++) {
                    nombreColumna[i] = estructura.getColumnName(i + 1);
                    tamanioColumna[i] = estructura.getPrecision(i + 1);
                    tipoColumna[i] = estructura.getColumnTypeName(i + 1);
                }
            } catch (SQLException sqle) {
                JOptionPane.showMessageDialog(null, "Error de sqle: " + sqle, "Error de sql", JOptionPane.DEFAULT_OPTION);
            }

            try {
                archivo = new File(jtNombreArchivo.getText().trim() + ".java");
                archivoAGenerar = new PrintWriter(new FileWriter(archivo));
                archivoAGenerar.println("import javax.swing.*;");
                archivoAGenerar.println("import java.awt.*;");
                archivoAGenerar.println("import java.awt.event.*;");
                //archivoAGenerar.println("import java.lang.Object.*;");
                archivoAGenerar.println("import java.util.*;");
                archivoAGenerar.println("import java.sql.*;");
                archivoAGenerar.println("import org.postgresql.ds.*;");
                archivoAGenerar.println("import org.postgresql.ds.PGPoolingDataSource;\n");
                archivoAGenerar.println("public class " + archivo.getName().substring(0, archivo.getName().indexOf('.')) + " extends PanelDeTrabajo {\n");
                archivoAGenerar.println("\tprivate final JPanel jpInterno = new JPanel();\n");

                for (int i = 0; i < columnas; i++) {
                    archivoAGenerar.println("\tprivate final JLabel jl" + nombreColumna[i] + " = new JLabel(\"" + nombreColumna[i] + ": \");");
                }
                archivoAGenerar.println("");
                for (int i = 0; i < columnas; i++) {
                    System.out.println("Tipo de columna para: " + nombreColumna[i] + " es: " + tipoColumna[i]);
                    if (tipoColumna[i].equals("bool")) {
                        archivoAGenerar.println("\tprivate final JCheckBox jc" + nombreColumna[i] + " = new JCheckBox(\"Si/No\", true);");
                    } else if (tipoColumna[i].equals("date")) {
                        archivoAGenerar.println("\tprivate final JFormattedTextField jf" + nombreColumna[i] + " = new JFormattedTextField(new java.util.Date());");
                    } else {
                        archivoAGenerar.println("\tprivate final JTextField jt" + nombreColumna[i] + " = new JTextField(" + tamanioColumna[i] + ");");
                    }
                }
                archivoAGenerar.println("");

                archivoAGenerar.println("\tprivate HciTotal frameTrabajando;");
                archivoAGenerar.println("\tprivate BotonesUtilitarios botones = new BotonesUtilitarios();");
                archivoAGenerar.println("");

                archivoAGenerar.println("\tpublic " + archivo.getName().substring(0, archivo.getName().indexOf('.')) + "(HciTotal hciTotal) {\n");
                archivoAGenerar.println("\t\tsetNombrePanel(\"" + jtTituloPanel.getText().trim() + "\");");
                archivoAGenerar.println("\t\tsetDescripcionPanel(\"" + jtDescripcionPanel.getText().trim() + "\");");
                archivoAGenerar.println("\t\tframeTrabajando = hciTotal;");

                archivoAGenerar.println("\t\tbotones.jbCrear.addActionListener(new AccionCrear());");
                archivoAGenerar.println("\t\tbotones.jbEditar.addActionListener(new EditarAccion());");
                archivoAGenerar.println("\t\tbotones.jbBorrar.addActionListener(new BorrarAccion());");
                archivoAGenerar.println("\t\tbotones.jbBuscar.addActionListener(new BuscarAccion());");
                archivoAGenerar.println("\t\tbotones.jbListar.addActionListener(new ListarAccion());");
                archivoAGenerar.println("\t\tbotones.jbGraba.addActionListener(new GuardarAccion());");
                archivoAGenerar.println("\t\tbotones.jbCancel.addActionListener(new CancelarAccion());");
                archivoAGenerar.println("");
                archivoAGenerar.println("\t\tjt" + nombreColumna[0] + ".addActionListener(new BuscarAccionReal());");
                archivoAGenerar.println("");

                String alto = String.valueOf(columnas * 30);
                archivoAGenerar.println("\t\tjpInterno.setPreferredSize(new Dimension(300," + alto + "));");
                archivoAGenerar.println("\t\tjpInterno.setLayout(new GridLayout(" + columnas + ",2));");
                archivoAGenerar.println("\t\tjpInterno.setBorder(BorderFactory.createTitledBorder(\"" + jtTituloPanel.getText().trim() + "\"));");
                for (int i = 0; i < columnas; i++) {
                    archivoAGenerar.println("\t\tjpInterno.add(jl" + nombreColumna[i] + ");");
                    if (tipoColumna[i].equals("bool")) {
                        archivoAGenerar.println("\t\tjpInterno.add(jc" + nombreColumna[i] + ");");
                    } else if (tipoColumna[i].equals("date")) {
                        archivoAGenerar.println("\t\tjpInterno.add(jf" + nombreColumna[i] + ");");
                    } else {
                        archivoAGenerar.println("\t\tjpInterno.add(jt" + nombreColumna[i] + ");");
                    }
                }
                archivoAGenerar.println("");
                archivoAGenerar.println("\t\tdesactivaControles(jpInterno);");
                archivoAGenerar.println("\t\trevisaPermisos(frameTrabajando, botones, \"" + jtNombreObjeto.getText().trim() + "\");");
                archivoAGenerar.println("");
                archivoAGenerar.println("\t\tadd(botones, BorderLayout.NORTH);");
                archivoAGenerar.println("\t\tadd(jpInterno, BorderLayout.CENTER);");
                archivoAGenerar.println("\t}");
                archivoAGenerar.println("");

                archivoAGenerar.println("\t// Cuando se da click en el boton Crear");
                archivoAGenerar.println("\tprivate class AccionCrear implements ActionListener {");
                archivoAGenerar.println("\t\tpublic void actionPerformed(ActionEvent e) {");
                archivoAGenerar.println("\t\t\tif (getAccionPanel()==CANCELAR || getAccionPanel()==BUSCAR) {");
                archivoAGenerar.println("\t\t\t\tlimpiarControles();");
                archivoAGenerar.println("\t\t\t\tactivaControles(jpInterno);");
                archivoAGenerar.println("\t\t\t\tsetAccionPanel(CREAR);");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t}");
                archivoAGenerar.println("\t}");
                archivoAGenerar.println("");

                archivoAGenerar.println("\t// Cuando se da click en el boton Editar");
                archivoAGenerar.println("\tprivate class EditarAccion implements ActionListener {");
                archivoAGenerar.println("\t\tpublic void actionPerformed(ActionEvent e) {");
                archivoAGenerar.println("\t\t\tif (getAccionPanel()==BUSCAR && !jt" + nombreColumna[0] + ".isEnabled()");
                archivoAGenerar.println("\t\t\t   && jt" + nombreColumna[0] + ".getText()!=null && jt" + nombreColumna[0] + ".getText().length()>0) {");
                archivoAGenerar.println("\t\t\t\tsetAccionPanel(EDITAR);");
                archivoAGenerar.println("\t\t\t\tactivaControles(jpInterno);");
                archivoAGenerar.println("\t\t\t\tjt" + nombreColumna[0] + ".setEnabled(false);");
                archivoAGenerar.println("\t\t\t} else {");
                archivoAGenerar.println("\t\t\t\tString mensaje = \"Debe primero buscar el registro a editar\";");
                archivoAGenerar.println("\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Primero debe...\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t}");
                archivoAGenerar.println("\t}");

                archivoAGenerar.println("\t// Cuando se da click en el boton Borar");
                archivoAGenerar.println("\tprivate class BorrarAccion implements ActionListener {");
                archivoAGenerar.println("\t\tpublic void actionPerformed (ActionEvent e) {");
                archivoAGenerar.println("\t\t\tif (getAccionPanel()==BUSCAR && !jt" + nombreColumna[0] + ".isEnabled()");
                archivoAGenerar.println("\t\t\t   && jt" + nombreColumna[0] + ".getText()!=null && jt" + nombreColumna[0] + ".getText().length()>0) {");
                archivoAGenerar.println("\t\t\t   String mensajeb = \"<html><b><font color=red>Esta seguro que desea borrar este registro de la Base de datos?</font></b></html>\";");
                archivoAGenerar.println("\t\t\t   if (JOptionPane.showConfirmDialog(frameTrabajando, mensajeb, \"Confirmar\", JOptionPane.YES_NO_OPTION)==0) {");
                archivoAGenerar.println("\t\t\t\ttry {");
                archivoAGenerar.println("\t\t\t\t\tConnection conexion = getConexion();");
                archivoAGenerar.println("\t\t\t\t\tString consulta = \"delete from " + jcTabla.getSelectedItem() + " where " + nombreColumna[0] + " = ?\";");
                archivoAGenerar.println("\t\t\t\t\tPreparedStatement sentencia = conexion.prepareStatement(consulta);");
                if (tipoColumna[0].equals("varchar")) {
                    archivoAGenerar.println("\t\t\t\t\tsentencia.setString(1, jt" + nombreColumna[0] + ".getText().trim());");
                } else if (tipoColumna[0].equals("int4")) {
                    archivoAGenerar.println("\t\t\t\t\tsentencia.setInt(1, Integer.parseInt(jt" + nombreColumna[0] + ".getText()));");
                }
                archivoAGenerar.println("\t\t\t\t\tint resultados = sentencia.executeUpdate();");
                archivoAGenerar.println("\t\t\t\t\tif (resultados!=1) {");
                archivoAGenerar.println("\t\t\t\t\t\tString mensaje = \"Atencion, no fue posible borrar el registro\";");
                archivoAGenerar.println("\t\t\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Error de Borrado\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t\t\t\t} else {");
                archivoAGenerar.println("\t\t\t\t\t\tString mensaje = \"Registro borrado de la Base de Datos\";");
                archivoAGenerar.println("\t\t\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Registro Borrado\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t\t\t\t}");
                archivoAGenerar.println("\t\t\t\t\tsentencia.close();");
                archivoAGenerar.println("\t\t\t\t\tconexion.close();");
                archivoAGenerar.println("\t\t\t\t} catch (SQLException sqle) {");
                archivoAGenerar.println("\t\t\t\t\tString mensaje = \"Error de sql: \"+sqle;");
                archivoAGenerar.println("\t\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Error de sql\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t\t\t}");
                archivoAGenerar.println("\t\t\t\tlimpiarControles();");
                archivoAGenerar.println("\t\t\t   }");
                archivoAGenerar.println("\t\t\t} else {");
                archivoAGenerar.println("\t\t\t\tString mensaje = \"Debe primero buscar el registro a eliminar\";");
                archivoAGenerar.println("\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Primero debe...\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t}");
                archivoAGenerar.println("\t}");

                archivoAGenerar.println("\t// Cuando se da click en el boton Buscar");
                archivoAGenerar.println("\tprivate class BuscarAccion implements ActionListener {");
                archivoAGenerar.println("\t\tpublic void actionPerformed(ActionEvent e) {");
                archivoAGenerar.println("\t\t\tif (getAccionPanel()==CANCELAR || getAccionPanel()==BUSCAR) {");
                archivoAGenerar.println("\t\t\t\tjt" + nombreColumna[0] + ".setEnabled(true);");
                archivoAGenerar.println("\t\t\t\tsetAccionPanel(BUSCAR);");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t}");
                archivoAGenerar.println("\t}");

                archivoAGenerar.println("\t// Cuando da enter en el campo de Busqueda");
                archivoAGenerar.println("\tprivate class BuscarAccionReal implements ActionListener {");
                archivoAGenerar.println("\t\tpublic void actionPerformed (ActionEvent e) {");
                archivoAGenerar.println("\t\t\tif (getAccionPanel()==BUSCAR) {");
                archivoAGenerar.println("\t\t\t\tif (jt" + nombreColumna[0] + ".getText()==null || jt" + nombreColumna[0] + ".getText().length()==0) {");
                archivoAGenerar.println("\t\t\t\t\tString mensaje = \"El campo no debe estar vacio\";");
                archivoAGenerar.println("\t\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Error al Buscar\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t\t\t} else {");
                archivoAGenerar.println("\t\t\t\t\ttry {");
                archivoAGenerar.println("\t\t\t\t\t\tConnection conexion = getConexion();");
                archivoAGenerar.println("\t\t\t\t\t\tStatement sentencia = conexion.createStatement();");
                archivoAGenerar.print("\t\t\t\t\t\tString consulta = \"select * from " + jcTabla.getSelectedItem() + " where " + nombreColumna[0]);
                if (tipoColumna[0].equals("varchar")) {
                    archivoAGenerar.println(" = '\"+jt" + nombreColumna[0] + ".getText().trim()+\"'\";");
                } else {
                    archivoAGenerar.println(" = \"+Integer.parseInt(jt" + nombreColumna[0] + ".getText());");
                }
                archivoAGenerar.println("\t\t\t\t\t\tResultSet resultados = sentencia.executeQuery(consulta);");
                archivoAGenerar.println("");
                archivoAGenerar.println("\t\t\t\t\t\tif (resultados.next()) {");
                for (int i = 0; i < columnas; i++) {
                    if (tipoColumna[i].equals("varchar") || tipoColumna[i].equals("int4")) {
                        archivoAGenerar.println("\t\t\t\t\t\t\tjt" + nombreColumna[i] + ".setText(resultados.getString(\"" + nombreColumna[i] + "\"));");
                    } else if (tipoColumna[i].equals("date")) {
                        archivoAGenerar.println("\t\t\t\t\t\t\tjf" + nombreColumna[i] + ".setText(resultados.getDate(\"" + nombreColumna[i] + "\"));");
                    } else if (tipoColumna[i].equals("bool")) {
                        archivoAGenerar.println("\t\t\t\t\t\t\tjc" + nombreColumna[i] + ".setSelected(resultados.getBoolean(\"" + nombreColumna[i] + "\"));");
                    }
                }
                archivoAGenerar.println("");
                archivoAGenerar.println("\t\t\t\t\t\t\tjt" + nombreColumna[0] + ".setEnabled(false);");
                archivoAGenerar.println("\t\t\t\t\t\t} else {");
                archivoAGenerar.println("\t\t\t\t\t\t\tString mensaje = \"<html><b>Registro No encontrado</b></html>\";");
                archivoAGenerar.println("\t\t\t\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Registro No encontrado\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t\t\t\t\t\tlimpiarControles();");
                archivoAGenerar.println("\t\t\t\t\t\t}");
                archivoAGenerar.println("\t\t\t\t\t\tresultados.close();");
                archivoAGenerar.println("\t\t\t\t\t\tsentencia.close();");
                archivoAGenerar.println("\t\t\t\t\t\tconexion.close();");
                archivoAGenerar.println("\t\t\t\t\t} catch (SQLException sqle) {");
                archivoAGenerar.println("\t\t\t\t\t\tString mensaje = \"Error de sql: \"+sqle;");
                archivoAGenerar.println("\t\t\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Error de sql\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t\t\t\t}");
                archivoAGenerar.println("\t\t\t\t}");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t}");
                archivoAGenerar.println("\t}");

                archivoAGenerar.println("\t// Cuando se da click en el boton Cancelar");
                archivoAGenerar.println("\tprivate class CancelarAccion implements ActionListener {");
                archivoAGenerar.println("\t\tpublic void actionPerformed(ActionEvent e) {");
                archivoAGenerar.println("\t\t\tif (JOptionPane.showConfirmDialog(frameTrabajando, \"Esta seguro de Cancelar la Accion?\", \"Cancelar Accion\", JOptionPane.YES_NO_OPTION)==0){");
                archivoAGenerar.println("\t\t\t\tlimpiarControles();");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t}");
                archivoAGenerar.println("\t}");
                archivoAGenerar.println("");

                archivoAGenerar.println("\t// Lista la informacion de la Tabla");
                archivoAGenerar.println("\tprivate class ListarAccion implements ActionListener {");
                archivoAGenerar.println("\t\tpublic void actionPerformed(ActionEvent e) {");
                archivoAGenerar.println("\t\t\tif (getAccionPanel()!=CREAR && getAccionPanel()!=EDITAR) {");
                archivoAGenerar.println("\t\t\t\tframeTrabajando.setPanel(new " + archivo.getName().substring(0, archivo.getName().indexOf('.')) + "Lista(frameTrabajando));");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t}");
                archivoAGenerar.println("\t}");
                archivoAGenerar.println("");

                archivoAGenerar.println("\t// Guarda la Informacion");
                archivoAGenerar.println("\tprivate class GuardarAccion implements ActionListener {");
                archivoAGenerar.println("\t    public void actionPerformed(ActionEvent e) {");
                archivoAGenerar.println("");
                archivoAGenerar.println("\t       // Si la accion es Crear o Editar");
                archivoAGenerar.println("\t       if (getAccionPanel()==CREAR || getAccionPanel()==EDITAR) {");
                archivoAGenerar.println("\t\t// Se verifican que todos los campos contengan datos");
                archivoAGenerar.print("\t\tif (");
                for (int i = 0; i < columnas; i++) {
                    if (tipoColumna[i].equals("varchar") || tipoColumna[i].equals("int4")) {
                        archivoAGenerar.print("(jt" + nombreColumna[i] + ".getText() == null) || (jt" + nombreColumna[i] + ".getText().length()==0)");
                    } else if (tipoColumna[i].equals("date")) {
                        archivoAGenerar.print("(jf" + nombreColumna[i] + ".getText() == null) || (jf" + nombreColumna[i] + ".getText().length()==0)");
                    }
                    if (columnas % 2 == 0) {
                        if (i < columnas - 1) {
                            archivoAGenerar.println(" || ");
                            archivoAGenerar.print("\t\t\t");
                        }
                    } else {
                        if (i < columnas - 2) {
                            archivoAGenerar.println(" || ");
                            archivoAGenerar.print("\t\t\t");
                        }
                    }
                }
                archivoAGenerar.println(") {");
                archivoAGenerar.println("\t\t\tString mensaje = \"<html><b>Todos los campos son obligatorios</b></html>\";");
                archivoAGenerar.println("\t\t\tJOptionPane.showMessageDialog(null, mensaje, \"Campos incompletos\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t} else {");
                archivoAGenerar.println("\t\t\ttry {");
                archivoAGenerar.println("\t\t\t\tConnection conexion = getConexion();");
                archivoAGenerar.println("\t\t\t\t// Variable para generar el mensaje de pantalla");
                archivoAGenerar.println("\t\t\t\tString mensaje = \"\";");
                archivoAGenerar.println("\t\t\t\t// Variable para generar el sql de guardado");
                archivoAGenerar.println("\t\t\t\tString consulta = \"\";");
                archivoAGenerar.println("\t\t\t\tPreparedStatement sentencia = null;");
                archivoAGenerar.println("");
                archivoAGenerar.println("\t\t\t\t// Si la accion es Crear (1)");
                archivoAGenerar.println("\t\t\t\tif (getAccionPanel()==CREAR) {");
                archivoAGenerar.print("\t\t\t\t   consulta = \"insert into " + jcTabla.getSelectedItem() + " values (");
                for (int i = 0; i < columnas; i++) {
                    archivoAGenerar.print("?");
                    if (i < columnas - 1) {
                        archivoAGenerar.print(",");
                    }
                }
                archivoAGenerar.println(")\";");
                archivoAGenerar.println("\t\t\t\t   sentencia = conexion.prepareStatement(consulta);");
                for (int i = 0; i < columnas; i++) {
                    if (tipoColumna[i].equals("varchar")) {
                        archivoAGenerar.println("\t\t\t\t   sentencia.setString(" + (i + 1) + ", jt" + nombreColumna[i] + ".getText().trim());");
                    } else if (tipoColumna[i].equals("int4")) {
                        archivoAGenerar.println("\t\t\t\t   sentencia.setInt(" + (i + 1) + ", Integer.parseInt(jt" + nombreColumna[i] + ".getText().trim()));");
                    } else if (tipoColumna[i].equals("date")) {
                        archivoAGenerar.println("\t\t\t\t   sentencia.setDate(" + (i + 1) + ", java.sql.Date.valueOf(jf" + nombreColumna[i] + ".getText()));");
                    } else if (tipoColumna[i].equals("bool")) {
                        archivoAGenerar.println("\t\t\t\t   sentencia.setBoolean(" + (i + 1) + ", jc" + nombreColumna[i] + ".isSelected());");
                    }
                }
                archivoAGenerar.println("\t\t\t\t   mensaje = \"Ingreso\";");
                archivoAGenerar.println("\t\t\t\t} else if (getAccionPanel()==EDITAR) {");
                archivoAGenerar.print("\t\t\t\t   consulta = \"update " + jcTabla.getSelectedItem() + " set ");
                // Aqui se cuenta el i desde uno porque la primera columna NO se edita
                for (int i = 1; i < columnas; i++) {
                    archivoAGenerar.print(nombreColumna[i] + " = ?");
                    if (i < columnas - 1) {
                        archivoAGenerar.print(", ");
                    }
                }
                archivoAGenerar.println(" where " + nombreColumna[0] + " = ?\";");
                archivoAGenerar.println("\t\t\t\t   sentencia = conexion.prepareStatement(consulta);");
                // Aqui se cuenta el i desde uno porque la primera columna NO se edita
                for (int i = 1; i < columnas; i++) {
                    if (tipoColumna[i].equals("varchar")) {
                        archivoAGenerar.println("\t\t\t\t   sentencia.setString(" + i + ", jt" + nombreColumna[i] + ".getText().trim());");
                    } else if (tipoColumna[i].equals("int4")) {
                        archivoAGenerar.println("\t\t\t\t   sentencia.setInt(" + i + ", Integer.parseInt(jt" + nombreColumna[i] + ".getText().trim()));");
                    } else if (tipoColumna[i].equals("date")) {
                        archivoAGenerar.println("\t\t\t\t   sentencia.setDate(" + i + ", java.sql.Date.valueOf(jf" + nombreColumna[i] + ".getText()));");
                    } else if (tipoColumna[i].equals("bool")) {
                        archivoAGenerar.println("\t\t\t\t   sentencia.setBoolean(" + i + ", jc" + nombreColumna[i] + ".isSelected());");
                    }
                }
                if (tipoColumna[0].equals("varchar")) {
                    archivoAGenerar.println("\t\t\t\t   sentencia.setString(" + columnas + ", jt" + nombreColumna[0] + ".getText().trim());");
                } else if (tipoColumna[0].equals("int4")) {
                    archivoAGenerar.println("\t\t\t\t   sentencia.setInt(" + columnas + ", Integer.parseInt(jt" + nombreColumna[0] + ".getText().trim()));");
                }
                archivoAGenerar.println("\t\t\t\t   mensaje = \"Actualizo\";");
                archivoAGenerar.println("\t\t\t\t}");
                archivoAGenerar.println("");

                archivoAGenerar.println("\t\t\t\tint resultados = sentencia.executeUpdate();");
                archivoAGenerar.println("");
                archivoAGenerar.println("\t\t\t\tif (resultados != 1) {");
                archivoAGenerar.println("\t\t\t\t\tmensaje = \"No se \"+mensaje+\" correctamente el registro\";");
                archivoAGenerar.println("\t\t\t\t\tJOptionPane.showMessageDialog(null, mensaje, \"Error\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t\t\t} else {");
                archivoAGenerar.println("\t\t\t\t\tmensaje = \"El Registro se \"+mensaje+\" correctamente\";");
                archivoAGenerar.println("\t\t\t\t\tJOptionPane.showMessageDialog(null, mensaje, \"Usuario Creado\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t\t\t\tlimpiarControles();");
                archivoAGenerar.println("\t\t\t\t}");
                archivoAGenerar.println("\t\t\t\tsentencia.close();");
                archivoAGenerar.println("\t\t\t\tconexion.close();");
                archivoAGenerar.println("\t\t\t} catch (SQLException sqle) {");
                archivoAGenerar.println("\t\t\t\tString mensaje = \"Error de sql \"+sqle;");
                archivoAGenerar.println("\t\t\t\tJOptionPane.showMessageDialog(null, mensaje, \"Error de sql\", JOptionPane.DEFAULT_OPTION);");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t}");
                archivoAGenerar.println("\t       }");
                archivoAGenerar.println("\t    }");
                archivoAGenerar.println("\t}");
                archivoAGenerar.println("");

                archivoAGenerar.println("\t// Este proceso limpia los controles y vuelve a cero");
                archivoAGenerar.println("\tprivate void limpiarControles(){");
                for (int i = 0; i < columnas; i++) {
                    if (tipoColumna[i].equals("date")) {
                        archivoAGenerar.println("\t\tjf" + nombreColumna[i] + ".setText(new java.util.Date());");
                    } else if (tipoColumna[i].equals("varchar") || tipoColumna[i].equals("int4")) {
                        archivoAGenerar.println("\t\tjt" + nombreColumna[i] + ".setText(null);");
                    }
                }
                archivoAGenerar.println("\t\tsetAccionPanel(CANCELAR);");
                archivoAGenerar.println("\t\tdesactivaControles(jpInterno);");
                archivoAGenerar.println("\t}");
                archivoAGenerar.println("");

                archivoAGenerar.println("\t//Este proceso crea una conexion desde la pisina de conexiones previamente creada");
                archivoAGenerar.println("\tprivate Connection getConexion() {");
                archivoAGenerar.println("\t\tConnection conexionCreada = null;");
                archivoAGenerar.println("\t\ttry {");
                archivoAGenerar.println("\t\t\tif (frameTrabajando.pisinaDeConexiones == null) { // si la pisina esta nula");
                archivoAGenerar.println("\t\t\t\tframeTrabajando.pisinaDeConexiones = frameTrabajando.conexionesHciTotal();");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t\tif (frameTrabajando.pisinaDeConexiones==null) {");
                archivoAGenerar.println("\t\t\t\tString mensaje = (\"Lo siento, no pude crear la pisina de conexiones\");");
                archivoAGenerar.println("\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Error de conexion\", JOptionPane.INFORMATION_MESSAGE);");
                archivoAGenerar.println("\t\t\t} else {");
                archivoAGenerar.println("\t\t\t\tconexionCreada = frameTrabajando.pisinaDeConexiones.getConnection();");
                archivoAGenerar.println("\t\t\t\tif (conexionCreada == null) {");
                archivoAGenerar.println("\t\t\t\t\tString mensaje = \"Lo siento, no pude obtener la conexion\";");
                archivoAGenerar.println("\t\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Error de conexion\", JOptionPane.INFORMATION_MESSAGE);");
                archivoAGenerar.println("\t\t\t\t}");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t} catch (SQLException sqle) {");
                archivoAGenerar.println("\t\t\tJOptionPane.showMessageDialog(frameTrabajando, \"Error de sql: \"+sqle, \"Error\", JOptionPane.INFORMATION_MESSAGE);");
                archivoAGenerar.println("\t\t}");
                archivoAGenerar.println("\t\treturn conexionCreada;");
                archivoAGenerar.println("\t}");
                archivoAGenerar.println("");

                archivoAGenerar.println("}");
                archivoAGenerar.close();

                // Aqui se genera el archivo del listado
                archivo = null;
                archivo = new File(jtNombreArchivo.getText().trim() + "Lista.java");
                archivoAGenerar = null;
                archivoAGenerar = new PrintWriter(new FileWriter(archivo));

                archivoAGenerar.println("import javax.swing.*;");
                archivoAGenerar.println("import java.awt.*;");
                archivoAGenerar.println("import java.awt.event.*;");
                archivoAGenerar.println("import java.util.*;");
                archivoAGenerar.println("import java.sql.*;");
                archivoAGenerar.println("import org.postgresql.ds.*;");
                archivoAGenerar.println("import org.postgresql.ds.PGPoolingDataSource;\n");
                archivoAGenerar.println("public class " + archivo.getName().substring(0, archivo.getName().indexOf('.')) + " extends PanelDeTrabajo {\n");
                archivoAGenerar.println("\tprivate final JLabel jlOrden = new JLabel(\"Seleccione el Orden:\");");
                archivoAGenerar.println("\tprivate final JComboBox jcCampos = new JComboBox();");
                archivoAGenerar.println("\tprivate final JButton jbGenerar = new JButton(\"Generar\");");
                archivoAGenerar.println("\tprivate final JEditorPane jeListado = new JEditorPane();");
                archivoAGenerar.println("\tprivate final JScrollPane barrasListado;");
                archivoAGenerar.println("\tprivate JProgressBar barraProgreso = new JProgressBar();");
                archivoAGenerar.println("");

                archivoAGenerar.println("\tprivate final JPanel jpInterno = new JPanel();");
                archivoAGenerar.println("\tprivate final JPanel jpConfigura = new JPanel();");
                archivoAGenerar.println("");

                archivoAGenerar.println("\tprivate HciTotal frameTrabajando;");
                archivoAGenerar.println("");

                archivoAGenerar.println("\tprivate String tabla = null; // Este se utiliza para generar el informe en html");
                archivoAGenerar.println("");

                archivoAGenerar.println("\tpublic " + archivo.getName().substring(0, archivo.getName().indexOf('.')) + "(HciTotal hciTotal) {");
                archivoAGenerar.println("");
                archivoAGenerar.println("\t\tsetNombrePanel(\"Listado de " + jtTituloPanel.getText().trim() + "\");");
                archivoAGenerar.println("\t\tsetDescripcionPanel(\"Modulo para listar " + jtDescripcionPanel.getText().trim() + "\");");
                archivoAGenerar.println("\t\tframeTrabajando = hciTotal;\n");

                archivoAGenerar.println("\t\tbarraProgreso.setString(\"Buscando...\");");
                archivoAGenerar.println("\t\tjeListado.setEditable(false);");
                archivoAGenerar.println("\t\tjeListado.setContentType(\"text/html\");\n");

                archivoAGenerar.println("\t\tbarrasListado = new JScrollPane(jeListado);");
                archivoAGenerar.println("\t\tbarrasListado.setPreferredSize(new Dimension(750,440));");
                archivoAGenerar.println("\t\tbarrasListado.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);");
                archivoAGenerar.println("\t\tbarrasListado.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);\n");

                archivoAGenerar.println("\t\tjbGenerar.addActionListener(new ListarAccion());\n");

                archivoAGenerar.println("\t\tllenaCampos();\n");

                archivoAGenerar.println("\t\tjpInterno.setPreferredSize(new Dimension(760,460));\n");

                archivoAGenerar.println("\t\tjpConfigura.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));");
                archivoAGenerar.println("\t\tjpConfigura.add(jlOrden);");
                archivoAGenerar.println("\t\tjpConfigura.add(jcCampos);");
                archivoAGenerar.println("\t\tjpConfigura.add(jbGenerar);");
                archivoAGenerar.println("\t\tjpConfigura.add(barraProgreso);\n");

                archivoAGenerar.println("\t\tjpInterno.setBorder(BorderFactory.createTitledBorder(\"Listado de " + jtTituloPanel.getText().trim() + "\"));");
                archivoAGenerar.println("\t\tjpInterno.add(barrasListado);\n");

                archivoAGenerar.println("\t\tadd(jpConfigura, BorderLayout.NORTH);");
                archivoAGenerar.println("\t\tadd(jpInterno, BorderLayout.CENTER);\n");

                archivoAGenerar.println("\t}\n");

                archivoAGenerar.println("\t// Llena el combobox con los campos de la tabla");
                archivoAGenerar.println("\tprivate void llenaCampos() {");
                for (int i = 0; i < columnas; i++) {
                    archivoAGenerar.println("\t\tjcCampos.addItem(\"" + nombreColumna[i] + "\");");
                }
                archivoAGenerar.println("\t}\n");

                archivoAGenerar.println("\t// Lista la informacion de la Tabla");
                archivoAGenerar.println("\tprivate class ListarAccion implements ActionListener {");
                archivoAGenerar.println("\t\tpublic void actionPerformed (ActionEvent e) {");
                archivoAGenerar.println("\t\t\tbarraProgreso.setIndeterminate(true);");
                archivoAGenerar.println("\t\t\tbarraProgreso.setStringPainted(true);");
                archivoAGenerar.println("\t\t\tString consulta = \"select * from " + jcTabla.getSelectedItem() + " order by \"+(jcCampos.getSelectedIndex()+1);");
                archivoAGenerar.println("\t\t\ttry {");
                archivoAGenerar.println("\t\t\t\tConnection conexion = getConexion();");
                archivoAGenerar.println("\t\t\t\tStatement sentencia = conexion.createStatement();");
                archivoAGenerar.println("\t\t\t\tResultSet resultados = sentencia.executeQuery(consulta);");
                archivoAGenerar.println("\t\t\t\tint fila = 0;\n");

                archivoAGenerar.println("\t\t\t\ttabla = \"<center><table>\\n\";");
                archivoAGenerar.println("\t\t\t\ttabla += \"<tr>\";");
                for (int i = 0; i < columnas; i++) {
                    archivoAGenerar.println("\t\t\t\ttabla += \"<th class=\\\"titulotabla\\\">" + nombreColumna[i] + "</th>\";");
                }
                archivoAGenerar.println("\t\t\t\ttabla += \"</tr>\\n\";");
                archivoAGenerar.println("\t\t\t\twhile (resultados.next()) {");
                archivoAGenerar.println("\t\t\t\t\tfila++;");
                archivoAGenerar.println("\t\t\t\t\tif (fila%2==0) {");
                archivoAGenerar.println("\t\t\t\t\t\ttabla += \"<tr>\";");
                archivoAGenerar.println("\t\t\t\t\t} else {");
                archivoAGenerar.println("\t\t\t\t\t\ttabla += \"<tr class=\\\"filaimpar\\\">\";");
                archivoAGenerar.println("\t\t\t\t\t}");
                for (int i = 0; i < columnas; i++) {
                    if (tipoColumna[i].equals("varchar")) {
                        archivoAGenerar.println("\t\t\t\t\ttabla += \"<td>\"+resultados.getString(" + (i + 1) + ")+\"</td>\";");
                    } else if (tipoColumna[i].equals("int4")) {
                        archivoAGenerar.println("\t\t\t\t\ttabla += \"<td>\"+resultados.getInt(" + (i + 1) + ")+\"</td>\";");
                    } else if (tipoColumna[i].equals("date")) {
                        archivoAGenerar.println("\t\t\t\t\ttabla += \"<td>\"+resultados.getDate(" + (i + 1) + ")+\"</td>\";");
                    } else if (tipoColumna[i].equals("bool")) {
                        archivoAGenerar.println("\t\t\t\t\ttabla += \"<td>\"+resultados.getBoolean(" + (i + 1) + ")+\"</td>\";");
                    }
                }
                archivoAGenerar.println("\t\t\t\t\ttabla += \"</tr>\\n\";");
                archivoAGenerar.println("\t\t\t\t}");
                archivoAGenerar.println("\t\t\t\ttabla += \"</table></center>\\n\";");
                archivoAGenerar.println("");

                archivoAGenerar.println("\t\t\t\tresultados.close();");
                archivoAGenerar.println("\t\t\t\tsentencia.close();");
                archivoAGenerar.println("\t\t\t\tconexion.close();");
                archivoAGenerar.println("\t\t\t} catch (SQLException sqle) {");
                archivoAGenerar.println("\t\t\t\tString mensaje = \"Error de sql: \"+sqle;");
                archivoAGenerar.println("\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Error de sql\", JOptionPane.INFORMATION_MESSAGE);");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t\tString mostrar = frameTrabajando.LasUtilidades.getTituloHtml(\"Listado de " + jcTabla.getSelectedItem() + "\");");
                archivoAGenerar.println("\t\t\tmostrar += tabla;");
                archivoAGenerar.println("\t\t\tmostrar += frameTrabajando.LasUtilidades.getPieHtml();");
                archivoAGenerar.println("\t\t\tjeListado.setText(mostrar);");
                archivoAGenerar.println("\t\t\tbarraProgreso.setIndeterminate(false);");
                archivoAGenerar.println("\t\t\tbarraProgreso.setStringPainted(false);");
                archivoAGenerar.println("\t\t\t//System.out.println(mostrar);");
                archivoAGenerar.println("\t\t}");
                archivoAGenerar.println("\t}\n");

                archivoAGenerar.println("\t// Este proceso crea una conexion desde la piscina de conexiones previamente creada");
                archivoAGenerar.println("\tprivate Connection getConexion() {\n");

                archivoAGenerar.println("\t\tConnection conexionCreada = null;");
                archivoAGenerar.println("\t\ttry {");
                archivoAGenerar.println("\t\t\tif (frameTrabajando.pisinaDeConexiones == null) { // si la pisina esta nula");
                archivoAGenerar.println("\t\t\t\tframeTrabajando.pisinaDeConexiones = frameTrabajando.conexionesHciTotal();");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t\tif (frameTrabajando.pisinaDeConexiones==null) {");
                archivoAGenerar.println("\t\t\t\tString mensaje = (\"Lo siento, no pude crear la pisina de conexiones\");");
                archivoAGenerar.println("\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Error de conexion\", JOptionPane.INFORMATION_MESSAGE);");
                archivoAGenerar.println("\t\t\t} else {");
                archivoAGenerar.println("\t\t\t\tconexionCreada = frameTrabajando.pisinaDeConexiones.getConnection();");
                archivoAGenerar.println("\t\t\t\tif (conexionCreada == null) {");
                archivoAGenerar.println("\t\t\t\t\tString mensaje = \"Lo siento, no pude obtener la conexion\";");
                archivoAGenerar.println("\t\t\t\t\tJOptionPane.showMessageDialog(frameTrabajando, mensaje, \"Error de conexion\", JOptionPane.INFORMATION_MESSAGE);");
                archivoAGenerar.println("\t\t\t\t}");
                archivoAGenerar.println("\t\t\t}");
                archivoAGenerar.println("\t\t} catch (SQLException sqle) {");
                archivoAGenerar.println("\t\t\tJOptionPane.showMessageDialog(frameTrabajando, \"Error de sql: \"+sqle, \"Error\", JOptionPane.INFORMATION_MESSAGE);");
                archivoAGenerar.println("\t\t}");
                archivoAGenerar.println("\t\treturn conexionCreada;");
                archivoAGenerar.println("\t}\n");

                archivoAGenerar.println("}");

                archivoAGenerar.close();
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(null, "Error de io: " + ioe, "Error de io", JOptionPane.DEFAULT_OPTION);
            }
            JOptionPane.showMessageDialog(null, "Proceso Terminado!!!", "Proceso Terminado", JOptionPane.DEFAULT_OPTION);
        }
    }

    // Accion de escojer el archivo
    private class SeleccionaArchivo implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivos java", "java");
            fc.setFileFilter(filtro);
            int guarda = fc.showSaveDialog(jpPanel);
            if (guarda == JFileChooser.APPROVE_OPTION) {
                jtNombreArchivo.setText(fc.getSelectedFile().getAbsolutePath());
            }
        }
    }

    public static void main(String[] args) {
        Asistente miAsistente = new Asistente();
        miAsistente.setLocationRelativeTo(null);
        miAsistente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
