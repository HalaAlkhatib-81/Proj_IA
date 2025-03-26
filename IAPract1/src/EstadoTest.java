import IA.Red.*;
import aima.search.uninformed.BreadthFirstSearch;

import java.util.*;

public class EstadoTest implements Cloneable{
    /* ===================== ATRIBUTOS ===================== */

    // Sensores y Centros de Datos
    private Sensores sensores;
    private CentrosDatos centros;
    public static int semillaSensores;
    public static int semillaCentros;

    // Información de las conexiones
    private int[] aQuienTransmito;           // Indica a qué nodo está conectado cada sensor
    private double[] capacidadRestante;      // Capacidad restante de cada nodo (sensor o centro)
    private ArrayList<ArrayList<Integer>> quienMeTransmite;

    // Matriz de posicionamiento de sensores y centros en el tablero 100x100
    private int[][] tablero;

    // Métricas de coste e información transmitida
    private double coste = 0;
    private double info = 0;
    public static double a, b;

    /* ===================== CONSTRUCTORES ===================== */

    /**
     * Constructora básica.
     *
     * Da todos los atributos de la clase inicializados correctamente
     * en base a el número de sensores y de centros y junto a las semillas.
     */
    public EstadoTest(int nSensores, int nCentros) {
        this.sensores = new Sensores(nSensores, semillaSensores);
        this.centros = new CentrosDatos(nCentros, semillaCentros);

        //Al inicio ningún sensor transmite a nadie
        this.aQuienTransmito = new int[nSensores];
        Arrays.fill(this.aQuienTransmito, -1);

        this.capacidadRestante = new double[nSensores + nCentros];
        // Inicializar capacidades de sensores a su capacidad de transmitir * 2
        for (int i = 0; i < nSensores; ++i) {
            double capacidadSensor = this.sensores.get(i).getCapacidad() * 2;
            this.capacidadRestante[i] = capacidadSensor;
        }
        // Inicializar las capacidades de los centros a 150
        Arrays.fill(this.capacidadRestante, nSensores, nSensores + nCentros, (double) 150);

        // Al inicio ningún sensor transmite a nadie
        this.quienMeTransmite = new ArrayList<>();
        for (int i = 0; i < nSensores + nCentros; ++i) {
            this.quienMeTransmite.add(new ArrayList<>());
        }

        //Inicializamos el tablero con las posiciones de cada elemento.
        this.tablero = new int[100][100];
        for (int i = 0; i < nSensores; ++i) {
            int x = sensores.get(i).getCoordX();
            int y = sensores.get(i).getCoordY();
            tablero[x][y] = i;
        }
        for (int i = 0; i < nCentros; ++i) {
            int x = centros.get(i).getCoordX();
            int y = centros.get(i).getCoordY();
            tablero[x][y] = nSensores + i;
        }
    }

    /**
     * Constructora con variables inicializadas.
     *
     * Se asume que las variables pasadas como parámetros han sido previamente
     * inicializadas correctamente. Se hacen Shallow Copys de los parámetros.
     */
    public EstadoTest(Sensores s, CentrosDatos c, int[] receptores, double[] cRestante,
                      ArrayList<ArrayList<Integer>> transmisores, int[][] tablero,
                      double coste, double info) {
        this.sensores = s;
        this.centros = c;
        this.aQuienTransmito = receptores;
        this.capacidadRestante = cRestante;
        this.quienMeTransmite = transmisores;
        this.coste = coste;
        this.info = info;
        this.tablero = tablero;
    }

    /**
     * Clone
     */
    @Override
    public EstadoTest clone() {
        int nSensores = this.sensores.size();
        int nCentros = this.centros.size();
        Sensores newSensores = new Sensores(nSensores, semillaSensores);
        CentrosDatos newCentros = new CentrosDatos(nCentros, semillaCentros);

        int[] newAQuienTransmito = this.aQuienTransmito.clone();
        double[] newCapacidadRestante = this.capacidadRestante.clone();
        ArrayList<ArrayList<Integer>> newQuienMeTransmite = new ArrayList<>(nSensores + nCentros);
        for (ArrayList<Integer> lista : this.quienMeTransmite) {
            newQuienMeTransmite.add(new ArrayList<>(lista)); // Crear una nueva instancia para cada sublista
        }
        int[][] newTablero = new int[100][100];
        for (int i = 0; i < 100; ++i)
            newTablero[i] = this.tablero[i].clone();

        double newCoste = this.coste;
        double newInfo = this.info;
        return new EstadoTest(newSensores, newCentros, newAQuienTransmito, newCapacidadRestante,
                              newQuienMeTransmite, newTablero, newCoste, newInfo);
    }

    /**
     * Genera un estado inicial a partir de la estrategia seleccionada por el usuario:
     * if (Greedy) Se genera un estado en donde los sensores se intentan conectar
     * al centro más cercano y a un sensor en caso que no hayan centros disponibles.
     * En caso que no haya ni centros ni sensores disponibles, se queda sin transmitir a nadie.
     *
     * else Se genera un estado a partir de eventos aleatorios.
     */
    public EstadoTest generarEstadoInicial(boolean greedy) {
        if (greedy) {
            generarEstadoCercania();
        }
        else {
            generarEstadoAleatorio();
        }
    }

    /**
     * Se genera un estado usando la estrategia de cercanía.
     * Primeramente se intenta conectar a cada sensor al centro disponible más cercano.
     *
     * Si no hay centros disponibles, Intenta conectarse al sensor disponible más cercano.
     * En caso de no haber sensores disponibles, no se conecta a nada.
     */
    private void generarEstadoCercania() {
        int nSensores = this.sensores.size();
        int nCentros = this.centros.size();

        /*
        Intentamos conectar a cada sensor a cualquier centro por orden de cercanía.
         */
        for (int i = 0; i < nSensores; ++i) {
            int x = this.sensores.get(i).getCoordX();
            int y = this.sensores.get(i).getCoordY();

            //Busca por niveles a quién conectarse.
            int idCentroReceptor = buscarEnTablero(x, y, 'C');
            if (idCentroReceptor != -1) {
                conectar(i, idCentroReceptor);
            }
            else {
                int idSensorReceptor = buscarEnTablero(x, y, 'C');
                if (idSensorReceptor != -1) {
                    conectar(i, idCentroReceptor);
                }
            }
        }
    }

    private void generarEstadoAleatorio() {

    }

    /**
     * @param posX es la posición X del sensor transmisor en el tablero
     * @param posY es la posición Y del sensor transmisor en el tablero
     * @param objetivo Si es 'C', se buscará el centro VÁLIDO más cercano.
     *                 En caso que sea 'S' se busca el sensor VÁLIDO más cercano.
     * @return el ID de el objetivo encontrado. -1 en caso de no haber encontrado a ninguno.
     */
    private int buscarEnTablero(int posX, int posY, char objetivo) {
        boolean[][] visitado =
    }

    /* ===================== MÉTODOS DE INICIALIZACIÓN ===================== */

    public double getCoste(){
        return coste;
    }
    public double getInfoPerdida(){
        return info;
    }


    /* ===================== VERIFICACIONES Y UTILIDADES ===================== */
    private Sensores getSensores() {
        return this.sensores;
    }

    private CentrosDatos getCentros() {
        return this.centros;
    }

    private boolean es_sensor(int sensorId) {
        return sensorId >= 0 && sensorId < sensor.size();
    }

    private boolean es_centro(int centroId) {
        return centroId >= sensor.size() && centroId < sensor.size()+centros.size();
    }

    private boolean es_valido_sensor(int sensorId) {
        return contador_conexiones[sensorId] < 3 && conexion_con_centro(sensorId);
    }

    private boolean es_valido_centros(int centroId) {
        return contador_conexiones[centroId] < 25;
    }

    public boolean conexion_con_centro(int sensorId) {
        boolean[] visitado = new boolean[sensor.size()];
        Arrays.fill(visitado, false);
        return dfs(sensorId, visitado);
    }

    private boolean dfs(int sensorId, boolean[] visitado) {
        if (es_centro(conexiones[sensorId])) {
            return true;
        }
        else if (!visitado[sensorId]) {
            visitado[sensorId] = true;
            int v = conexiones[sensorId];
            return v != -1 && dfs(v, visitado); //si v = -1 puede que ese sensor no tenga ninguna conexión
        }
        return false;
    }

    private double dist(int id1, int id2, boolean escentro) {
        double x1, y1, x2, y2;

        /* id1 siempre será sensor */

        x1 = sensor.get(id1).getCoordX();
        y1 = sensor.get(id1).getCoordY();

        if (escentro){
            x2 = centros.get(id2).getCoordX();
            y2 = centros.get(id2).getCoordY();
        }
        else {
            x2 = sensor.get(id2).getCoordX();
            y2 = sensor.get(id2).getCoordY();
        }
        return Math.sqrt((Math.pow(x1-x2, 2)) + (Math.pow(y1-y2,2)));
    }

    private boolean pos_valida(int[][]tablero, int x, int y) {
        int filas = tablero.length;
        int columnas = tablero[0].length;
        return x >= 0 && x < filas && y >= 0 && y < columnas;
    }

    private ArrayList<Integer> bfs(int sensorId, boolean[][] visitado) {
        Queue<Pair<Integer, Integer>> cola = new LinkedList<>();
        cola.add(new Pair<>(sensor.get(sensorId).getCoordX(), sensor.get(sensorId).getCoordY()));
        visitado[sensor.get(sensorId).getCoordX()][sensor.get(sensorId).getCoordY()] = true;
        ArrayList<Integer> cercanos = new ArrayList<>();

        while (!cola.isEmpty()) {
            Pair<Integer, Integer> actual = cola.poll();
            int posX = actual.first;
            int posY = actual.second;
            for (int i = 0; i < 4; ++i) {
                int x_next = posX + X[i];
                int y_next = posY + Y[i];
                if (pos_valida(tablero, x_next, y_next)) {
                    if (tablero[x_next][y_next] != -1 ) {
                        cercanos.add(tablero[x_next][y_next]);
                    }
                    if (!visitado[x_next][y_next]) {
                        cola.add(new Pair<>(x_next, y_next));
                        visitado[x_next][y_next] = true;
                    }
                }
            }
        }
        return cercanos;
    }

    private void actualizar_capacidadRestante_centro(int sensorId, int centroId) {
        /*if (sensor.get(sensorId).getCapacidad() > capacidadRestante[centroId]) {
            //info_perdida += (int) sensor.get(sensorId).getCapacidad() - (int) capacidadRestante[centroId];
            capacidadRestante[centroId] = 0;
        }
        else capacidadRestante[centroId] -= sensor.get(sensorId).getCapacidad();*/
        capacidadRestante[centroId] = Math.max(0.0, capacidadRestante[centroId] - sensor.get(sensorId).getCapacidad());
    }

    //el 1 le envía al 2
    private void actualizar_capacidadRestante_sensor(int sensorId1, int sensorId2) {
        if (sensor.get(sensorId1).getCapacidad() > capacidadRestante[sensorId2]) {
            info += (int) sensor.get(sensorId1).getCapacidad() - (int) capacidadRestante[sensorId2];
            capacidadRestante[sensorId2] = 0;
            sensor.get(sensorId2).setCapacidad((int) sensor.get(sensorId2).getCapacidad() + (int) capacidadRestante[sensorId2]);
        }
        else {
            capacidadRestante[sensorId2] -= sensor.get(sensorId1).getCapacidad();
            sensor.get(sensorId2).setCapacidad((int) sensor.get(sensorId2).getCapacidad() + (int) sensor.get(sensorId1).getCapacidad());
        }
    }

    private void actualizar_conexiones_sensor(int sensorId1, int sensorId2) {
        conexiones[sensorId1] = sensorId2;
        ++contador_conexiones[sensorId2];
    }

    private void actualizar_conexiones_centro(int sensorId, int centroId) {
        conexiones[sensorId] = centroId;
        ++contador_conexiones[centroId];
    }

    public void crear_conexion (int id1, int id2) {
        if (es_centro(id1) || conexiones[id1] == id2) return;
        if(!es_centro(id2) && !es_valido_sensor(id2)) return;
        if (conexiones[id1] != -1) romper_conexion(id1);

        conexiones[id1] = id2;
        contador_conexiones[id2]++;
        //capacidadRestante[id2] -= sensor.get(id1).getCapacidad();
    }

    public void romper_conexion (int id1) {
        if (es_centro(id1) || conexiones[id1] == -1) return;
        contador_conexiones[conexiones[id1]]--;
        conexiones[id1] = -1;
    }

    /* ===================== SOLUCIÓN VÁLIDA ===================== */
    private boolean solucionInicialValida() {
        // Verificar que cada sensor tenga una conexión válida
        for (int i = 0; i < sensor.size(); i++) {
            if (conexiones[i] == -1) { // Si un sensor no tiene conexión
                System.out.println("Error: Sensor " + i + " no tiene conexión.");
                return false;
            }
        }

        // Verificar que ningún sensor exceda el número máximo de conexiones permitidas
        for (int i = 0; i < sensor.size(); i++) {
            if (contador_conexiones[i] > 3) {
                System.out.println("Error: Sensor " + i + " tiene más de 3 conexiones.");
                return false;
            }
        }

        // Verificar que ningún centro de datos tenga más de 24 conexiones
        for (int i = sensor.size(); i < sensor.size() + centros.size(); i++) {
            if (contador_conexiones[i] > 25) {
                System.out.println("Error: Centro " + (i - sensor.size()) + " tiene más de 25 conexiones.");
                return false;
            }
        }

        // Verificar que la capacidad restante de sensores y centros no sea negativa
        for (int i = 0; i < sensor.size() + centros.size(); i++) {
            if (capacidadRestante[i] < 0) {
                System.out.println("Error: Nodo " + i + " tiene capacidad restante negativa.");
                return false;
            }
        }

        // Verificar que cada sensor pueda llegar a un centro de datos
        for (int i = 0; i < sensor.size(); i++) {
            if (!conexion_con_centro(i)) {
                System.out.println("Error: Sensor " + i + " no puede transmitir su información a un centro.");
                return false;
            }
        }
        return true; // Si todas las verificaciones se cumplen, la solución inicial es válida.
    }

    /* ===================== OPERADORES DE BÚSQUEDA LOCAL ===================== */
    //operador1: intercambio de conexiones entre dos sensores
    public boolean swap(int id1, int id2) {
        if (!es_sensor(id1) || !es_sensor(id2) || conexiones[id1] == -1 || conexiones[id2] == -1 || id1 == id2) return false;
        else if (conexiones[id1] == id2 || conexiones[id2] == id1 || !es_valido_sensor(id1) || !es_valido_sensor(id2) || conexiones[id1] == conexiones[id2] ) return false;
        int conexion1 = conexiones[id1];
        int conexion2 = conexiones[id2];

        capacidadRestante[conexion1] += sensor.get(id1).getCapacidad();
        if(!es_centro(conexion1)) sensor.get(conexion1).setCapacidad((int) (sensor.get(conexion1).getCapacidad() - (int) sensor.get(id1).getCapacidad()));

        capacidadRestante[conexion2] += sensor.get(id2).getCapacidad();
        if(!es_centro(conexion2)) sensor.get(conexion2).setCapacidad((int) (sensor.get(conexion2).getCapacidad() - (int) sensor.get(id2).getCapacidad()));

        romper_conexion(id1);
        romper_conexion(id2);

        crear_conexion(id1, conexion2);
        crear_conexion(id2, conexion1);

        if(!es_centro(conexion2)) actualizar_capacidadRestante_sensor(id1, conexion2);
        else actualizar_conexiones_centro(id1, conexion2);
        if(!es_centro(conexion1)) actualizar_capacidadRestante_sensor(id2, conexion1);
        else actualizar_conexiones_centro(id2,conexion1);

        return true;
    }

    //operador2: mueve la conexión de un sensor a otro sensor o centro
    public boolean moverConexion(int sensorId, int nuevoDestino) {
        if (!es_sensor(sensorId) || conexiones[sensorId] == -1 || sensorId == nuevoDestino || nuevoDestino == conexiones[sensorId]) return false;
        if ((es_centro(nuevoDestino) && !es_valido_centros(nuevoDestino)) || (es_sensor(nuevoDestino) && !es_valido_sensor(nuevoDestino))) return false;

        int conexionAntigua = conexiones[sensorId];
        capacidadRestante[conexionAntigua] += sensor.get(sensorId).getCapacidad();
        if(es_sensor(conexionAntigua)) sensor.get(conexionAntigua).setCapacidad((int) (sensor.get(conexionAntigua).getCapacidad() - (int) sensor.get(sensorId).getCapacidad()));

        romper_conexion(sensorId);
        crear_conexion(sensorId, nuevoDestino);

        if (es_centro(nuevoDestino)) {
            actualizar_capacidadRestante_centro(sensorId, nuevoDestino);
        } else {
            actualizar_capacidadRestante_sensor(sensorId, nuevoDestino);
        }
        return true;
    }

    /* ===================== ESTADOS INICIALES ===================== */

    /* esta función genera el estado inicial aleatoriamente */
    public void estado_inicial_random () {
        Random rand = new Random();

        for (int i = 0; i < sensor.size(); i++) {
            int centroId = sensor.size() + rand.nextInt(centros.size());
            if (es_valido_centros(centroId)) {
                actualizar_conexiones_centro(i, centroId);
                actualizar_capacidadRestante_centro(i, centroId);
            } else {
                //si no hay un centro valido me conecto a un sensor random pero tengo que mirar que sea válido y que no sea el mismo
                //sensor porque no me puedo conectar a mi mismo
                int intentos = 0; //evitamos quedarnos en un bucle infinito
                int sensorId = rand.nextInt(sensor.size());
                while ((!es_valido_sensor(sensorId) || sensorId == i) && intentos < sensor.size()) {
                    sensorId = rand.nextInt(sensor.size());
                    ++intentos;
                }
                if (intentos == sensor.size()) {
                    System.out.println("Advertencia: No se encontró conexión válida para el sensor " + i);
                    continue;
                }
                actualizar_conexiones_sensor(i, sensorId);
                actualizar_capacidadRestante_sensor(i, sensorId);
            }

        }
    }

    /* esta función genera el estado inicial conectando al centro más cercano y teniendo en cuenta las restricciones */
    public void estado_inicial_cercania () {
        for (int i = 0; i < sensor.size(); ++i) {
            double min = Double.POSITIVE_INFINITY;
            int id_min = -1;

            for (int j = 0; j < centros.size(); ++j) {
                if (es_valido_centros(j + sensor.size())) {
                    double distancia = dist(i, j, true);
                    if (distancia < min) {
                        min = distancia;
                        id_min = j + sensor.size();
                    }
                }
            }
            if (id_min != -1) {
                actualizar_conexiones_centro(i, id_min);
                actualizar_capacidadRestante_centro(i, id_min);
            }

            else { // si no se puede conectar a un centro entonces se conecta al sensor o al centro más cercano
                boolean[][] visitado = new boolean[100][100];
                for (boolean[] fila : visitado) {
                    Arrays.fill(fila, false);
                }
                ArrayList<Integer> cercanos = bfs(i, visitado);
                boolean conexionEstablecida = false;


                for (int vecino : cercanos) { //vecino = cercanos.get(i)
                    if (es_centro(vecino) && es_valido_centros(vecino)) {
                        actualizar_conexiones_centro(i, vecino);
                        actualizar_capacidadRestante_centro(i, vecino);
                        conexionEstablecida = true;
                        break; // Salimos del bucle al encontrar una conexión válida
                    } else if (es_sensor(vecino) && es_valido_sensor(vecino)) {
                        actualizar_conexiones_sensor(i, vecino);
                        actualizar_capacidadRestante_sensor(i, vecino);
                        conexionEstablecida = true;
                        break; // Salimos al encontrar una conexión válida
                    }
                }

                if (!conexionEstablecida) {
                    System.out.println("Advertencia: No se encontró conexión válida para el sensor " + i);
                }
            }
        }
        System.out.println(sensor.get(0).getCapacidad());
    }

    /* ===================== HEURÍSTICA ===================== */
    public double getHeuristica() {
        double costeTotal = 0;
        double inforeal = 0;
        this.coste = 0;
        this.info = 0;
        for (int i = 0; i < conexiones.length; i++) {
            if (conexiones[i] != -1) { // Si el sensor i tiene conexión
                double distancia;
                if(es_centro(conexiones[i])) distancia = dist(i, conexiones[i]-sensor.size(),true );
                else distancia = dist(i, conexiones[i],false );


                double volumenCapturado = sensor.get(i).getCapacidad();
                System.out.println();
                System.out.println("sensord con id = " + i + " envia " + sensor.get(i).getCapacidad() + " a al id " + conexiones[i]);
                costeTotal += Math.pow(distancia, 2) * volumenCapturado;
            }
        }
        for(int i = sensor.size(); i < sensor.size()+ centros.size(); i++) {
            inforeal += capacidadRestante[i];
        }

        this.coste = costeTotal;
        this.info = centros.size()*150 - inforeal;
        System.out.println("info= " + sensor.get(0).getCapacidad() + " coste = " + this.coste);
        return a*coste - b*info;

    }

    @Override
    public String toString() {
        return "Sensores: " + sensor.size() +
                ", Centros: " + centros.size() +
                ", Conexiones: " + Arrays.toString(conexiones) +
                ", Coste: " + coste +
                ", Información transmitida: " + info;
    }
}
