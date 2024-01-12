package com.mycompany.garaje2;

import java.awt.*;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.sound.sampled.*;
import javax.sound.sampled.LineEvent.Type;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Garaje2 {

    private Connection connection;
    private JFrame frame;
    private Clip[] audioClips;
    private int currentClipIndex = 0;
    private double COSTE_MINUTO = 0.042;

    public Garaje2() {
        try {
            String dbURL = "jdbc:mysql://localhost:3306/Garage";
            String username = "root";
            String password = "";
            connection = DriverManager.getConnection(dbURL, username, password);
        } catch (SQLException e) {
            handleError("Error al conectar a la base de datos.", e);
        }
        setLookAndFeel();
        initializeFrame();
        createUI();
        initializeAudioClips();
        playCurrentAudioClip();
        audioClips[currentClipIndex].addLineListener(event -> {
            if (event.getType() == Type.STOP) {
                playNextAudioClip();
            }
        });
    }

    private void handleError(String message, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, message + "\nPor favor, inténtelo de nuevo.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            handleError("Error al configurar el aspecto del sistema.", e);
        }
    }

    private void initializeFrame() {
        frame = new JFrame("Gestión de Garaje");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel background = new JLabel(new ImageIcon("Garaje.jpg"));
        background.setLayout(new BorderLayout());
        frame.setContentPane(background);
        ImageIcon icon = new ImageIcon("Lambo.png");
        frame.setIconImage(icon.getImage());
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                stopAudioClip();
            }
        });
        frame.setVisible(true);
    }

    private void createUI() {
        // Crear la interfaz de usuario con un menú
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Opciones");

        // Opción 1: Mostrar usuario del Seat Ibiza
        JMenuItem option1 = new JMenuItem("Mostrar usuario del Seat Ibiza");
        option1.addActionListener(e -> mostrarUsuarioSeatIbiza());
        menu.add(option1);

        // Opción 2: Mostrar número de coches y modelo de coche que utiliza Jonas
        JMenuItem option2 = new JMenuItem("Mostrar coches y modelo de Jonas");
        option2.addActionListener(e -> mostrarCochesJonas());
        menu.add(option2);

        // Opción 3: Mostrar plazas libres
        JMenuItem option3 = new JMenuItem("Mostrar plazas libres");
        option3.addActionListener(e -> mostrarPlazasLibres());
        menu.add(option3);

        // Opción 4: Agregar coche
        JMenuItem option4 = new JMenuItem("Agregar Coche");
        option4.addActionListener(e -> agregarCoche());
        menu.add(option4);

        // Opción 5: Agregar plaza
        JMenuItem option5 = new JMenuItem("Agregar Plaza");
        option5.addActionListener(e -> agregarPlaza());
        menu.add(option5);

        // Opción 6: Borrar plaza
        JMenuItem option6 = new JMenuItem("Borrar Plaza");
        option6.addActionListener(e -> borrarPlaza());
        menu.add(option6);

        JMenuItem introducirCocheMenuItem = new JMenuItem("Introducir Coche");
        introducirCocheMenuItem.addActionListener(e -> introducirCoche());
        menu.add(introducirCocheMenuItem);

        JMenuItem introducirUsuarioMenuItem = new JMenuItem("Introducir Usuario");
        introducirUsuarioMenuItem.addActionListener(e -> introducirUsuario());
        menu.add(introducirUsuarioMenuItem);

        JMenuItem seleccionarPlazaMenuItem = new JMenuItem("Seleccionar Plaza");
        seleccionarPlazaMenuItem.addActionListener(e -> seleccionarPlaza());
        menu.add(seleccionarPlazaMenuItem);

        JMenuItem mostrarUsuariosMenuItem = new JMenuItem("Mostrar Usuarios");
        mostrarUsuariosMenuItem.addActionListener(e -> mostrarUsuariosGaraje());
        menu.add(mostrarUsuariosMenuItem);

        //Eleegir canción
        JMenu songsMenu = new JMenu("Canciones");
        menuBar.add(songsMenu);

        JMenuItem playSongsMenuItem = new JMenuItem("Elegir canción");
        playSongsMenuItem.addActionListener(e -> playSongs());
        songsMenu.add(playSongsMenuItem);
        // Agregar el menú a la barra de menú
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton ocupacionParkingButton = new JButton("Ocupación Parking");
        ocupacionParkingButton.addActionListener(e -> mostrarUsuariosGaraje());
        buttonPanel.add(ocupacionParkingButton);

        JButton minutosButton = new JButton("Minutos");
        minutosButton.addActionListener(e -> calcularMinutos());
        buttonPanel.add(minutosButton);

        JButton facturacionButton = new JButton("Facturación Parking");
        facturacionButton.addActionListener(e -> mostrarFacturacion());
        buttonPanel.add(facturacionButton);

        JButton pagoButton = new JButton("Pagar");
        pagoButton.addActionListener(e -> mostrarPago());
        buttonPanel.add(pagoButton);

        JButton facturacionAbonosButton = new JButton("Facturación Parking (Abonos)");
        facturacionAbonosButton.addActionListener(e -> mostrarFacturacionAbonos());
        buttonPanel.add(facturacionAbonosButton);

        JButton playSongsButton = new JButton("Elegir canción");
        playSongsButton.addActionListener(e -> playSongs());
        buttonPanel.add(playSongsButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);

    }

    private void mostrarPago() {
        // Crear un JDialog para la entrada de datos de pago
        JDialog pagoDialog = new JDialog(frame, "Pago", true);
        pagoDialog.setSize(400, 300);
        pagoDialog.setLocationRelativeTo(frame);

        // Crear un panel para la entrada de datos de pago
        JPanel pagoPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        pagoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Añadir etiquetas y campos de texto al panel
        JTextField dniField = new JTextField();
        JTextField nombreField = new JTextField();
        JTextField apellidosField = new JTextField();
        JTextField numTarjetaField = new JTextField();
        JComboBox<Integer> numeroCocheComboBox = new JComboBox<>();

        // Cargar números de coche en el combo box
        cargarNumerosDeCoche(numeroCocheComboBox);

        pagoPanel.add(new JLabel("DNI:"));
        pagoPanel.add(dniField);

        pagoPanel.add(new JLabel("Nombre:"));
        pagoPanel.add(nombreField);

        pagoPanel.add(new JLabel("Apellidos:"));
        pagoPanel.add(apellidosField);

        pagoPanel.add(new JLabel("Número de Tarjeta:"));
        pagoPanel.add(numTarjetaField);

        pagoPanel.add(new JLabel("Número de Coche:"));
        pagoPanel.add(numeroCocheComboBox);

        // Añadir botón de pago
        JButton pagarButton = new JButton("Realizar Pago");
        pagarButton.addActionListener(e -> {
            try {
                String dni = dniField.getText();
                String nombre = nombreField.getText();
                String apellidos = apellidosField.getText();
                String numTarjeta = numTarjetaField.getText();
                int numeroCoche = (int) numeroCocheComboBox.getSelectedItem();

                // Validar campos antes de procesar el pago
                if (validarCamposPago(dni, nombre, apellidos, numTarjeta)) {
                    // Calcular el importe del pago
                    double importe = calcularImportePago(dni);

                    // Mostrar detalles del pago al usuario
                    String mensaje = String.format("Importe a pagar: %.2f euros", importe);
                    JOptionPane.showMessageDialog(frame, mensaje, "Detalles del Pago", JOptionPane.INFORMATION_MESSAGE);

                    // Realizar el pago y actualizar la tabla Abonos
                    realizarPago(dni, nombre, apellidos, numTarjeta, importe, numeroCoche);

                    // Cerrar el diálogo después de realizar el pago
                    pagoDialog.dispose();
                }
            } catch (NumberFormatException | SQLException ex) {
                handleError("Error al procesar el pago.", ex);
            }
        });

        pagoPanel.add(new JLabel());  // Espaciador
        pagoPanel.add(pagarButton);

        pagoDialog.add(pagoPanel, BorderLayout.CENTER);
        pagoDialog.setVisible(true);
    }

    private boolean validarCamposPago(String dni, String nombre, String apellidos, String numTarjeta) {
        // Validar DNI con una expresión regular simple
        String dniRegex = "\\d{8}[A-HJ-NP-TV-Z]";
        if (!Pattern.matches(dniRegex, dni)) {
            JOptionPane.showMessageDialog(frame, "Formato de DNI incorrecto. Introduzca un DNI válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validar que los campos no estén vacíos
        if (dni.isEmpty() || nombre.isEmpty() || apellidos.isEmpty() || numTarjeta.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Todos los campos son obligatorios. Por favor, llénelos todos.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validar número de tarjeta con una expresión regular simple
        String tarjetaRegex = "\\d{16}";
        if (!Pattern.matches(tarjetaRegex, numTarjeta)) {
            JOptionPane.showMessageDialog(frame, "Formato de número de tarjeta incorrecto. Introduzca un número de tarjeta válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void mostrarFacturacionAbonos() {
        try {
            String totalImportesQuery = "SELECT SUM(importe) AS TotalImportes FROM Abonos";
            String usuariosPagadosQuery = "SELECT COUNT(*) AS UsuariosPagados FROM Abonos WHERE pagado = true";

            executeAndDisplayQuery(totalImportesQuery);
            executeAndDisplayQuery(usuariosPagadosQuery);
        } catch (SQLException e) {
            handleError("Error al mostrar la facturación de abonos del parking.", e);
        }
    }

    private double calcularImportePago(String dni) throws SQLException {
        // Consulta para obtener la cantidad de minutos de estancia de un usuario
        String minutosQuery = "SELECT SUM(minutos_estancia) AS totalMinutos FROM Pagos WHERE numero_coche IN (SELECT numero_coche FROM Usuarios WHERE dni = ?)";

        // Calcular el importe usando la tarifa establecida
        double importe = 0.0;

        try ( PreparedStatement statement = connection.prepareStatement(minutosQuery)) {
            statement.setString(1, dni);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int totalMinutos = resultSet.getInt("totalMinutos");

                // Aplicar la tarifa
                importe = calcularCosteParking(totalMinutos);
            }
        }

        return importe;
    }

    private void realizarPago(String dni, String nombre, String apellidos, String numTarjeta, double importe, int numeroCoche) throws SQLException {
        // Consulta para insertar el pago en la tabla Abonos
        String insertPagoQuery = "INSERT INTO Abonos (dni, nombre, apellidos, num_tarjeta, importe, pagado, numero_coche) VALUES (?, ?, ?, ?, ?, false, ?)";

        try ( PreparedStatement statement = connection.prepareStatement(insertPagoQuery)) {
            statement.setString(1, dni);
            statement.setString(2, nombre);
            statement.setString(3, apellidos);
            statement.setString(4, numTarjeta);
            statement.setDouble(5, importe);
            statement.setInt(6, numeroCoche);

            // Ejecutar la inserción
            statement.executeUpdate();
        } catch (SQLException e) {
            // Si hay un error, lanzar una excepción para manejarlo en el método llamador
            throw new SQLException("Error al realizar el pago.", e);
        }
    }

    private void mostrarUsuariosGaraje() {
        try {
            String query = "SELECT * FROM Usuarios";
            executeAndDisplayQuery(query);
        } catch (SQLException e) {
            handleError("Error al mostrar usuarios del garaje.", e);
        }
    }

    private void mostrarFacturacion() {
        try {
            String totalImportesQuery = "SELECT SUM(importe) AS TotalImportes FROM Abonos";
            String usuariosPagadosQuery = "SELECT COUNT(*) AS UsuariosPagados FROM Abonos WHERE pagado = true";

            executeAndDisplayQuery(totalImportesQuery);
            executeAndDisplayQuery(usuariosPagadosQuery);
        } catch (SQLException e) {
            handleError("Error al mostrar la facturación del parking.", e);
        }
    }

    private void calcularMinutos() {
        // Crear un JDialog para la entrada de minutos
        JDialog minutosDialog = new JDialog(frame, "Calcular Minutos", true);
        minutosDialog.setLayout(new BorderLayout());
        minutosDialog.setSize(300, 200);
        minutosDialog.setLocationRelativeTo(frame);

        // Crear un panel para la entrada de minutos
        JPanel minutosPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        minutosPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Añadir etiquetas y componentes al panel
        JTextField horaEntradaField = new JTextField();
        JTextField horaSalidaField = new JTextField();
        JComboBox<Integer> numeroCocheComboBox = new JComboBox<>();

        // Cargar números de coche en el combo box
        cargarNumerosDeCoche(numeroCocheComboBox);

        minutosPanel.add(new JLabel("Hora de Entrada (HH:mm):"));
        minutosPanel.add(horaEntradaField);

        minutosPanel.add(new JLabel("Hora de Salida (HH:mm):"));
        minutosPanel.add(horaSalidaField);

        minutosPanel.add(new JLabel("Número de Coche:"));
        minutosPanel.add(numeroCocheComboBox);

        // Añadir botón de cálculo
        JButton calcularButton = new JButton("Calcular");
        calcularButton.addActionListener(e -> {
            try {
                String horaEntradaStr = horaEntradaField.getText();
                String horaSalidaStr = horaSalidaField.getText();
                int numeroCoche = (int) numeroCocheComboBox.getSelectedItem();

                // Calcular minutos de estancia
                int minutos = calcularMinutosEstancia(horaEntradaStr, horaSalidaStr);

                // Actualizar la tabla Pagos con la información
                double coste = calcularCosteParking(minutos);
                actualizarTablaPagos(numeroCoche, minutos, coste);

                // Cerrar el diálogo después de calcular
                minutosDialog.dispose();
            } catch (NumberFormatException | SQLException ex) {
                handleError("Error al calcular minutos de estancia.", ex);
            }
        });

        minutosPanel.add(new JLabel());  // Espaciador
        minutosPanel.add(calcularButton);

        minutosDialog.add(minutosPanel, BorderLayout.CENTER);
        minutosDialog.setVisible(true);
    }

    private int calcularMinutosEstancia(String horaEntradaStr, String horaSalidaStr) throws SQLException {
        // Convertir las cadenas de tiempo a objetos Time
        Time horaEntrada = Time.valueOf(horaEntradaStr + ":00");
        Time horaSalida = Time.valueOf(horaSalidaStr + ":00");

        // Calcular la diferencia en minutos
        long diferenciaMillis = horaSalida.getTime() - horaEntrada.getTime();
        int minutos = (int) (diferenciaMillis / (60 * 1000));

        return minutos;
    }

    private double calcularCosteParking(int minutos) {
        return minutos * COSTE_MINUTO;
    }

    private void cargarNumerosDeCoche(JComboBox<Integer> comboBox) {
        try {
            // Consulta para obtener los números de coche disponibles
            String query = "SELECT Numero_Coche FROM Coches";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            // Limpia el combo box antes de agregar nuevos elementos
            comboBox.removeAllItems();

            // Agrega los números de coche al combo box
            while (resultSet.next()) {
                int numeroCoche = resultSet.getInt("Numero_Coche");
                comboBox.addItem(numeroCoche);
            }

            // Cierra los recursos de la base de datos
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            handleError("Error al cargar números de coche", e);
        }
    }

    private void actualizarTablaPagos(int numeroCoche, int minutos, double coste) throws SQLException {
        // Consulta para insertar el pago en la tabla Pagos
        String insertPagoQuery = "INSERT INTO Pagos (numero_coche, minutos_estancia, coste) VALUES (?, ?, ?)";

        try ( PreparedStatement statement = connection.prepareStatement(insertPagoQuery)) {
            statement.setInt(1, numeroCoche);
            statement.setInt(2, minutos);
            statement.setDouble(3, coste);

            // Ejecutar la inserción
            statement.executeUpdate();
        } catch (SQLException e) {
            // Si hay un error, lanzar una excepción para manejarlo en el método llamador
            throw new SQLException("Error al actualizar la tabla Pagos.", e);
        }
    }

    private void seleccionarPlaza() {
        try {
            // Consulta para obtener las plazas de garage libres
            String query = "SELECT Numero FROM Plazas_Garaje WHERE TipoDePlaza = 'libre'";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            // Crear un array de strings para los números de plaza disponibles
            ArrayList<String> plazaNumbers = new ArrayList<>();
            while (resultSet.next()) {
                plazaNumbers.add(resultSet.getString("Numero"));
            }

            // Mostrar un JOptionPane para seleccionar una plaza libre
            String selectedPlaza = (String) JOptionPane.showInputDialog(frame, "Seleccione una plaza:", "Plazas Disponibles", JOptionPane.QUESTION_MESSAGE, null, plazaNumbers.toArray(), null);

            if (selectedPlaza != null) {
                // Verificar si la plaza seleccionada está ocupada
                if (plazaOcupada(selectedPlaza)) {
                    // Mostrar mensaje de que la plaza está ocupada
                    JOptionPane.showMessageDialog(frame, "La plaza seleccionada está ocupada. Por favor, elija otra plaza.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    int numeroCoche = inputInt("Ingrese el número del coche:");

                    // Consulta para actualizar la plaza a ocupada
                    String updateQuery = "UPDATE Plazas_Garaje SET Onuse = 'ocupada', Numero_Coche = ? WHERE Numero = ?";
                    executeUpdateQuery(updateQuery, numeroCoche, selectedPlaza);

                    // Mostrar mensaje de éxito
                    JOptionPane.showMessageDialog(frame, "Coche estacionado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            handleError("Error al seleccionar la plaza de garage.", e);
        } catch (Exception e) {
            handleError("Error al seleccionar la plaza.", e);
        }
    }

// Nuevo método para verificar si la plaza seleccionada está ocupada
    private boolean plazaOcupada(String selectedPlaza) throws SQLException {
        // Consulta para verificar si la plaza está ocupada
        String query = "SELECT Onuse FROM Plazas_Garaje WHERE Numero = ?";
        try ( PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, selectedPlaza);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() && resultSet.getString("Onuse").equalsIgnoreCase("ocupada");
        }
    }

    private void introducirUsuario() {
        try {
            String nombre = JOptionPane.showInputDialog(frame, "Ingrese el nombre del usuario:");
            String apellidos = JOptionPane.showInputDialog(frame, "Ingrese los apellidos del usuario:");
            int numeroCoche = inputInt("Ingrese el número del coche:");

            // Verificar si el número de coche existe en la tabla Coches
            if (cocheExists(numeroCoche)) {
                // Consulta para agregar el nuevo usuario a la base de datos
                String insertQuery = "INSERT INTO Usuarios (Nombre, Apellidos, Numero_Coche) VALUES (?, ?, ?)";
                executeUpdateQuery(insertQuery, nombre, apellidos, numeroCoche);

                // Mostrar mensaje de éxito
                JOptionPane.showMessageDialog(frame, "Usuario introducido exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Coche no introducido. Por favor, introduzca primero el coche.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            handleError("Error al introducir el usuario en la base de datos.", e);
        } catch (Exception e) {
            handleError("Error al introducir el usuario.", e);
        }
    }

    private boolean cocheExists(int numeroCoche) throws SQLException {
        // Consulta para verificar si el número de coche existe en la tabla Coches
        String query = "SELECT 1 FROM Coches WHERE Numero_Coche = ?";
        try ( PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, numeroCoche);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    private void introducirCoche() {
        try {
            String marca = JOptionPane.showInputDialog(frame, "Ingrese la marca del coche:");
            String modelo = JOptionPane.showInputDialog(frame, "Ingrese el modelo del coche:");
            int año = inputInt("Ingrese el año del coche:");

            // Consulta para agregar el nuevo coche a la base de datos
            String insertQuery = "INSERT INTO Coches (Marca, Modelo, Año) VALUES (?, ?, ?)";
            executeUpdateQuery(insertQuery, marca, modelo, año);

            // Mostrar mensaje de éxito
            JOptionPane.showMessageDialog(frame, "Coche introducido exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            handleError("Error al introducir el coche en la base de datos.", e);
        } catch (Exception e) {
            handleError("Error al introducir el coche.", e);
        }
    }

    private void mostrarUsuarioSeatIbiza() {
        try {
            // Consulta para obtener el nombre y apellidos del usuario que utiliza el Seat Ibiza
            String query = "SELECT Nombre, Apellidos FROM Usuarios WHERE Numero_Coche = (SELECT Numero_Coche FROM Coches WHERE Modelo = 'Ibiza')";
            executeAndDisplayQuery(query);
        } catch (SQLException e) {
            handleError("Error al ejecutar la consulta.", e);
        }
    }

    private void mostrarCochesJonas() {
        try {
            // Consulta para obtener el número de coches y el modelo de coche que utiliza Jonas
            String query = "SELECT COUNT(Numero_Coche) AS 'Número de Coches', Modelo FROM Usuarios JOIN Coches USING (Numero_Coche) WHERE Nombre = 'Jonas'";
            executeAndDisplayQuery(query);
        } catch (SQLException e) {
            handleError("Error al ejecutar la consulta.", e);
        }
    }

    private void mostrarPlazasLibres() {
        try {
            // Consulta para obtener los números de las plazas libres
            String query = "SELECT Numero FROM Plazas_Garaje WHERE TipoDePlaza = 'libre'";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            // Crear un modelo de tabla para los datos de plazas libres
            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.addColumn("Plaza Libre");

            // Llenar el modelo con datos de la consulta
            while (resultSet.next()) {
                Object[] row = {resultSet.getString("Numero")};
                tableModel.addRow(row);
            }

            // Crear un JTable con el modelo de la tabla
            JTable table = new JTable(tableModel);
            table.setFont(new Font("Courier New", Font.PLAIN, 14));

            // Mostrar el JTable en el dialogo - lmao
            JOptionPane.showMessageDialog(frame, new JScrollPane(table), "Plazas Libres", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            handleError("Error al ejecutar la consulta.", e);
        }
    }

    private void agregarCoche() {
        try {
            // Solicitar al usuario la información del nuevo coche
            String marca = JOptionPane.showInputDialog(frame, "Ingrese la marca del coche:");
            String modelo = JOptionPane.showInputDialog(frame, "Ingrese el modelo del coche:");
            int año = inputInt("Ingrese el año del coche:");

            // Consulta para agregar el nuevo coche a la base de datos sin especificar Numero_Coche
            String insertQuery = "INSERT INTO Coches (Marca, Modelo, Año) VALUES (?, ?, ?)";
            executeUpdateQuery(insertQuery, marca, modelo, año);

            // Mostrar mensaje de éxito
            JOptionPane.showMessageDialog(frame, "Coche agregado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            // Mostrar un mensaje específico en caso de error de SQL
            handleError("Error al agregar el coche a la base de datos.", e);
        } catch (Exception e) {
            // Mostrar un mensaje general en caso de cualquier otra excepción
            handleError("Error al agregar el coche.", e);
        }
    }

    private void agregarPlaza() {
        try {
            // Solicitar al usuario la información de la nueva plaza
            int numeroPlaza = inputInt("Ingrese el número de la plaza:");
            String tipoPlaza = JOptionPane.showInputDialog(frame, "Ingrese el tipo de plaza (libre, reservada, ocupada, disabled, Onuse):");
            int numeroCoche = inputInt("Ingrese el número del coche:");

            // Consulta para agregar la nueva plaza a la base de datos
            String insertQuery = "INSERT INTO Plazas_Garaje (Numero, TipoDePlaza, Numero_Coche) VALUES (?, ?, ?)";
            executeUpdateQuery(insertQuery, numeroPlaza, tipoPlaza, numeroCoche);

            // Mostrar mensaje de éxito
            JOptionPane.showMessageDialog(frame, "Plaza agregada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            handleError("Error al agregar la plaza.", e);
        }
    }

    private void borrarPlaza() {
        try {
            // Solicitar al usuario el número de la plaza que desea borrar
            int numeroPlaza = inputInt("Ingrese el número de la plaza que desea borrar:");

            // Confirmar con el usuario antes de borrar la plaza
            int confirmacion = JOptionPane.showConfirmDialog(frame, "¿Está seguro de que desea borrar la plaza " + numeroPlaza + "?", "Confirmar Borrado", JOptionPane.YES_NO_OPTION);

            if (confirmacion == JOptionPane.YES_OPTION) {
                // Consulta para borrar la plaza de la base de datos
                String deleteQuery = "DELETE FROM Plazas_Garaje WHERE Numero = ?";
                executeUpdateQuery(deleteQuery, numeroPlaza);
                JOptionPane.showMessageDialog(frame, "Plaza borrada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            handleError("Error al borrar la plaza.", e);
        }
    }

    private int inputInt(String message) {
        int input = 0;
        boolean validInput = false;

        do {
            try {
                String userInput = JOptionPane.showInputDialog(frame, message);
                if (userInput == null) {
                    return -1;
                }

                input = Integer.parseInt(userInput);
                validInput = true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Por favor, ingrese un valor numérico válido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } while (!validInput);

        return input;
    }

    private void executeAndDisplayQuery(String query, Object... parameters) throws SQLException {
        DefaultTableModel tablaModelo = new DefaultTableModel();
        try ( PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            ResultSet resultSet = statement.executeQuery();
            int contarCol = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= contarCol; i++) {
                tablaModelo.addColumn(resultSet.getMetaData().getColumnName(i));
            }
            while (resultSet.next()) {
                Object[] row = new Object[contarCol];
                for (int i = 1; i <= contarCol; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                tablaModelo.addRow(row);
            }
            JTable table = new JTable(tablaModelo);
            table.setFont(new Font("Courier New", Font.PLAIN, 14));
            JOptionPane.showMessageDialog(frame, new JScrollPane(table), "Resultados de la consulta", JOptionPane.PLAIN_MESSAGE);
            table.setFont(new Font("Courier New", Font.PLAIN, 14));
        }
    }

    private void executeUpdateQuery(String query, Object... parameters) throws SQLException {
        try ( PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            statement.executeUpdate();
        }
    }

    private void playSongs() {
        String[] songNames = {"Kehlani - Nights Like This.wav", "IDWMAT.wav", "Iris.wav", "MJTCU.wav", "Hoy.wav", "DJGFU.wav",
            "Flaca.wav", "ElMundoTrasElCristal.wav", "TheReason.wav", "ChasingCars.wav"};

        String songChoice = (String) JOptionPane.showInputDialog(frame, "Elige una canción:", "Canciones", JOptionPane.QUESTION_MESSAGE, null, songNames, songNames[0]);

        if (songChoice != null) {
            stopAudioClip();
            for (int i = 0; i < audioClips.length; i++) {
                if (songChoice.equals(songNames[i])) {
                    currentClipIndex = i;
                    playCurrentAudioClip();
                    break;
                }
            }
        }
    }

    private void stopAudioClip() {
        audioClips[currentClipIndex].stop();
    }

    private void initializeAudioClips() {
        audioClips = new Clip[10];

        try {
            String[] songNames = {"Kehlani - Nights Like This.wav", "IDWMAT.wav", "Iris.wav", "MJTCU.wav", "Hoy.wav", "DJGFU.wav",
                "Flaca.wav", "ElMundoTrasElCristal.wav", "TheReason.wav", "ChasingCars.wav"};

            for (int i = 0; i < audioClips.length; i++) {
                File audioFile = new File(songNames[i]);

                if (!audioFile.exists()) {
                    handleError("Archivo de audio no encontrado: " + audioFile.getName(), null);
                    return;
                }

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                audioClips[i] = AudioSystem.getClip();
                audioClips[i].open(audioInputStream);
            }

        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            handleError("Error al inicializar los clips de audio.", e);
        }
    }

    private void playNextAudioClip() {
        currentClipIndex = (currentClipIndex + 1) % audioClips.length;
        playCurrentAudioClip();
    }

    private void playCurrentAudioClip() {
        audioClips[currentClipIndex].setFramePosition(0);
        audioClips[currentClipIndex].start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Garaje2::new);
    }
}
