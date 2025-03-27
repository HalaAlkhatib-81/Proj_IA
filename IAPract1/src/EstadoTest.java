import IA.Red.*;

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
    //Nos dice quién está conectado a cada elemento
    private ArrayList<ArrayList<Integer>> quienMeTransmite;
    //Capacidades de transmisión de los sensores iniciales.
    private static final double[] capacidadesIniciales = {1.0, 2.0, 5.0};

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

        this.tablero = new int[100][100];

        //Inicializamos el tablero a -1
        for (int i = 0; i < 100; ++i) {
            Arrays.fill(this.tablero[i], -1);
        }

        //Rellenamos el tablero con los elementos
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
    public void generarEstadoInicial(boolean greedy) {
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
        actualizarCapacidades();
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
        boolean[][] visitado = new boolean[100][100];
        int[][] direcciones = {
                {-1, 0},    //Arriba
                {0, 1},     //Derecha
                {1, 0},     //Abajo
                {0, -1}     //Izquierda
        };
        visitado[posX][posY] = true;

        Queue<int[]> proximasPosiciones = new LinkedList<>();
        proximasPosiciones.offer(new int[]{posX, posY});

        while (!proximasPosiciones.isEmpty()) {
            int[] posActual = proximasPosiciones.poll();
            posX = posActual[0];
            posY = posActual[1];

            int ID = tablero[posX][posY];
            if (ID != -1) {
                if (objetivo == 'C') {
                    if (es_centro(ID) && centroEsValido(ID)) {
                        return ID;
                    }
                } else if (objetivo == 'S') {
                    if (es_sensor(ID) && sensorEsValido(ID)) {
                        return ID;
                    }
                }
            }
            for (int[] d : direcciones) {
                int newX = posX + d[0];
                int newY = posY + d[1];
                if (pos_valida(newX, newY) && !visitado[newX][newY]) {
                    visitado[newX][newY] = true;
                    proximasPosiciones.offer(new int[]{newX, newY});
                }
            }
        }
        return -1;
    }

    /* ===================== MÉTODOS DE INICIALIZACIÓN ===================== */


    /* ===================== VERIFICACIONES Y UTILIDADES ===================== */
    private boolean es_sensor(int sensorId) {
        return sensorId >= 0 && sensorId < sensores.size();
    }

    private boolean es_centro(int centroId) {
        return centroId >= sensores.size() && centroId < sensores.size()+centros.size();
    }

    private boolean sensorEsValido(int sensorId) {
        // Verificar restricción conexiones simultáneas
        int numeroConexionesActuales = this.quienMeTransmite.get(sensorId).size();

        // Verificar restricción datos recibidos simultaneamente
        double capacidadMaximaReceptora = capacidadesIniciales[sensorId % 3] * 2;
        double informacionRecibida = calcularInformacionRecibida(sensorId);

        // Verificar si centroId está indirectamente conectado a un centro
        boolean conectadoConCentro = existeCaminoValido(sensorId);

        return (numeroConexionesActuales < 3 && (capacidadMaximaReceptora - informacionRecibida) > 0) && conectadoConCentro;
    }

    private boolean centroEsValido(int centroId) {
        // Verificar restricción conexiones simultáneas
        int numeroConexionesActuales = this.quienMeTransmite.get(centroId).size();

        // Verificar restricción datos recibidos simultaneamente
        double capacidadMaximaReceptora = 150;
        double informacionRecibida = calcularInformacionRecibida(centroId);

        return (numeroConexionesActuales < 25 && (capacidadMaximaReceptora - informacionRecibida) > 0);
    }

    private double calcularInformacionRecibida(int IDElemento) {
        double cantidadInformacionRecibida = 0;
        int nElementosConectados = this.quienMeTransmite.get(IDElemento).size();
        if (nElementosConectados > 0) {
            for (int i = 0; i < nElementosConectados; i++) {
                int elementoConectado = this.quienMeTransmite.get(IDElemento).get(i);
                double informacionTransmitida = capacidadesIniciales[elementoConectado % 3];
                cantidadInformacionRecibida += (calcularInformacionRecibida(elementoConectado) + informacionTransmitida);
            }
        }
        return cantidadInformacionRecibida;
    }

    private boolean existeCaminoValido(int ID) {
        boolean[] visitado = new boolean[this.sensores.size()];
        Arrays.fill(visitado, false);
        return conexionACentro(ID, visitado);
    }

    private boolean conexionACentro(int ID, boolean[] visitado) {
        if (es_centro(ID))
            return true;

        visitado[ID] = true;
        return conexionACentro(aQuienTransmito[ID], visitado);
    }

    private double dist(int id1, int id2, boolean esCentro) {
        double x1, y1, x2, y2;

        /* id1 siempre será sensor */

        x1 = sensores.get(id1).getCoordX();
        y1 = sensores.get(id1).getCoordY();

        if (esCentro){
            x2 = centros.get(id2).getCoordX();
            y2 = centros.get(id2).getCoordY();
        }
        else {
            x2 = sensores.get(id2).getCoordX();
            y2 = sensores.get(id2).getCoordY();
        }
        return Math.sqrt((Math.pow(x1-x2, 2)) + (Math.pow(y1-y2,2)));
    }

    private boolean pos_valida(int x, int y) {
        return x < 100 && x >= 0 && y < 100 && y >= 0;
    }

    public void actualizarCapacidades() {
        for (int i = 0; i < capacidadRestante.length; i++) {
            actualizarCapacidad(i);
        }
    }

    public void actualizarCapacidad(int ID) {
        double cantidadMaximaReceptora;
        if (es_centro(ID)) {
            cantidadMaximaReceptora = 150;
        }else {
            cantidadMaximaReceptora = capacidadesIniciales[ID % 3] * 2;
        }
        double informacionRecibida = calcularInformacionRecibida(ID);
        double nuevaCapacidad = Math.max(0, cantidadMaximaReceptora - informacionRecibida);
        System.out.println("Actualizada la capacidad restante de " + ID + ":");
        System.out.println("Nueva Capacidad: " + nuevaCapacidad);
        capacidadRestante[ID] = nuevaCapacidad;
    }

    private void conectar(int ID1, int ID2) {
        if (es_centro(ID1)) {
            System.out.println("No se ha podido llevar a cabo la operación: conectar (" + ID1 + " -> " + ID2 + ") debido a que " + ID1 + "es un centro");
        }
        this.aQuienTransmito[ID1] = ID2;
        //El elemento no está en el Array
        if (!this.quienMeTransmite.get(ID2).contains(ID1)){
            this.quienMeTransmite.get(ID2).add(ID1);
        }
    }

    private void desconectar(int ID1) {
            int ID2 = this.aQuienTransmito[ID1];
            if (ID2 != -1) {
                int indiceID1 = this.quienMeTransmite.get(ID2).indexOf(ID1);
                if (indiceID1 != -1) {
                    this.quienMeTransmite.get(ID2).remove(indiceID1);
                    this.aQuienTransmito[ID1] = -1;
                }
            }
            else {
                System.out.println("No se ha podido realizar la operacion: desconectar (" + ID1 + " -> " + ID2 + ") debido a que " + ID1 + "no está conectado a nada");
            }
    }

    public boolean swap(int ID1, int ID2) {
        if (es_centro(ID1) || es_centro(ID2)) {
            System.out.println("No se ha podido realizar la operacion: SWAP (" + ID1 + " -> " + ID2 + ") debido a que uno de los IDs pertenece a un centro");
            return false;
        }
        int conexionID1 = aQuienTransmito[ID1];
        int conexionID2 = aQuienTransmito[ID2];
        if (conexionID1 == -1 || conexionID2 == -1) {
            System.out.println("No se ha podido realizar la operacion: SWAP (" + ID1 + " -> " + ID2 + ") debido a que uno de los dos IDs no está conectado a nada");
            return false;
        }
        if (conexionID1 == conexionID2) {
            System.out.println("No se ha podido realizar la operacion: SWAP (" + ID1 + " -> " + ID2 + ") debido a que los dos sensores están conectados a lo mismo");
            return false;
        }

        //Desconexión provisional para comprobar si el cambio se puede llevar a cabo
        desconectar(ID1);
        desconectar(ID2);

        if (es_centro(conexionID1)) {
            if (!centroEsValido(conexionID1))
                return false;
        }
        else {
            if (!sensorEsValido(conexionID1))
                return false;
        }

        if (es_centro(conexionID2)) {
            if (!centroEsValido(conexionID2))
                return false;
        }
        else {
            if (!sensorEsValido(conexionID2))
                return false;
        }

        conectar(ID1, conexionID2);
        conectar(ID2, conexionID1);
        actualizarCapacidad(conexionID1);
        actualizarCapacidad(conexionID2);
        return true;
    }

    public boolean moverConexion(int ID, int nuevoDestino) {
        if (es_centro(ID)) {
            System.out.println("No se ha podido realizar la operacion: moverConexion (" + ID + " -> " + nuevoDestino + ") debido a que uno de los IDs pertenece a un centro");
            return false;
        }
        if (es_centro(nuevoDestino)) {
            if (!centroEsValido(nuevoDestino))
                return false;
        }
        else {
            if (!sensorEsValido(nuevoDestino)) {
                return false;
            }
        }
        int antiguoDestino = aQuienTransmito[ID];
        desconectar(ID);
        conectar(ID, nuevoDestino);
        actualizarCapacidad(antiguoDestino);
        actualizarCapacidad(nuevoDestino);
        return true;
    }

    public int getNumeroSensores() {
        return this.sensores.size();
    }

    public int getNumeroCentros() {
        return this.centros.size();
    }

    /* ===================== SOLUCIÓN VÁLIDA ===================== */

    /* ===================== OPERADORES DE BÚSQUEDA LOCAL ===================== */

    /* ===================== ESTADOS INICIALES ===================== */

    /* esta función genera el estado inicial aleatoriamente */
    public void estado_inicial_random () {
        Random rand = new Random();
        for (int i = 0; i < sensores.size(); i++) {
            int centroId = sensores.size() + rand.nextInt(centros.size());
            if (centroEsValido(centroId)) {
                conectar(i, centroId);
            } else {
                //si no hay un centro valido me conecto a un sensor random pero tengo que mirar que sea válido y que no sea el mismo
                //sensor porque no me puedo conectar a mi mismo
                int intentos = 0; //evitamos quedarnos en un bucle infinito
                int sensorId = rand.nextInt(sensores.size());
                while ((!sensorEsValido(sensorId) || sensorId == i) && intentos < sensores.size()) {
                    sensorId = rand.nextInt(sensores.size());
                    ++intentos;
                }
                if (intentos == sensores.size()) {
                    System.out.println("Advertencia: No se encontró conexión válida para el sensor " + i);
                    continue;
                }
                conectar(i, sensorId);
            }
        }
        actualizarCapacidades();
    }

    /* ===================== HEURÍSTICA ===================== */
    public double getHeuristica() {
        double costeTotal = 0;
        double inforeal = 0;
        this.coste = 0;
        this.info = 0;
        for (int i = 0; i < aQuienTransmito.length; i++) {
            if (aQuienTransmito[i] != -1) { // Si el sensor i tiene conexión
                double distancia;
                if(es_centro(aQuienTransmito[i])) distancia = dist(i, aQuienTransmito[i]-sensores.size(),true );
                else distancia = dist(i, aQuienTransmito[i],false );


                double volumenCapturado = sensores.get(i).getCapacidad();
                System.out.println();
                System.out.println("sensord con id = " + i + " envia " + sensores.get(i).getCapacidad() + " a al id " + aQuienTransmito[i]);
                costeTotal += Math.pow(distancia, 2) * volumenCapturado;
            }
        }
        for(int i = sensores.size(); i < sensores.size()+ centros.size(); i++) {
            inforeal += capacidadRestante[i];
        }

        this.coste = costeTotal;
        this.info = centros.size()*150 - inforeal;
        System.out.println("info= " + sensores.get(0).getCapacidad() + " coste = " + this.coste);
        return a*coste - b*info;
    }

    @Override
    public String toString() {
        return "Sensores: " + sensores.size() +
                ", Centros: " + centros.size() +
                ", Conexiones: " + Arrays.toString(aQuienTransmito) +
                ", Coste: " + coste +
                ", Información transmitida: " + info;
    }
}
