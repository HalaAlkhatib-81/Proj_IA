import IA.Red.*;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Class Name: EstadoTest
 *
 * Description:
 Es la clase que da estructura al planteamiento inicial del problema.
 En esta clase se implementan todos los elementos, métodos y operadores
 necesarios y esenciales para generar los estados en los que se basarán los experimentos futuros.
 *
 * @author Grup_IA
 * @version 1.0
 */

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
     *
     * @param nSensores  numero de sensores
     * @param nCentros numero de centros
     */
    public EstadoTest(int nSensores, int nCentros) {
        //System.out.println("Constructora Básica");
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
     * @param s sensores existentes
     * @param c centros existentes
     * @param receptores para toda posición i, el nodo al cual envia un sensor de id i
     * @param cRestante capacidad restante de sensores y centros
     * @param transmisores para toda posición i, los sensores conectados al nodo i
     * @param tablero representación del tablero
     * @param coste coste total del estado
     * @param info información transmitida del estado
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

    /* ===================== GETTERS ===================== */

    /**
     * Metodo getter sensores
     * @return sensores existentes
     */
    public Sensores getSensores(){
        return sensores;
    }

    /**
     * Metodo getter centros
     * @return centros existentes
     */
    public CentrosDatos getCentros(){
        return centros;
    }

    /**
     * Metodo getter receptores
     * @return nodos a los cuales transmite cada sensor
     */
    public int[] getReceptores(){
        return aQuienTransmito;
    }

    /**
     * Metodo getter capacidad restante
     * @return capacidad restante de todos los nodos
     */
    public double[] getCapacidadRestante(){
        return capacidadRestante;
    }

    /**
     * Metodo getter tablero
     * @return tablero
     */
    public int[][] getTablero(){
        return tablero;
    }

    /**
     * Metodo getter transmissores
     * @return sensores que transmiten a todos los nodos
     */
    public ArrayList<ArrayList<Integer>> getTransmisores(){
        return quienMeTransmite;
    }

    /**
     * Metodo getter coste
     * @return coste total del estado
     */
    public double getCoste(){
        return coste;
    }

    /**
     * Metodo getter info
     * @return info total transmitida
     */
    public double getInfo(){
        return info;
    }

    /**
     * Clona estado
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

    /* ===================== GENERACIÓN DE ESTADOS INICIALES ===================== */


    /**
     * Genera un estado inicial a partir de la estrategia seleccionada por el usuario:
     * if (Greedy) Se genera un estado en donde los sensores se intentan conectar
     * al centro más cercano y a un sensor en caso que no hayan centros disponibles.
     * En caso que no haya ni centros ni sensores disponibles, se queda sin transmitir a nadie.
     *
     * else Se genera un estado a partir de eventos aleatorios.
     *
     * @param greedy determina si queremos generar el estado inicial aleatoriamente o por cercanias
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
     *
     */
    private void generarEstadoCercania() {
        /*
        Estrategia: Conectar cada sensor de capacidad 5 al centro más cercano. En caso que los
        centros aún no estén completos, rellenar con los sensores más cercanos de coste 2 y 1.
        Para los demás sensores, buscaremos conectarlos a los sensores disponibles que estén
        posicionados lo más cercano posible a un centro.
         */

        int nSensores = this.sensores.size();
        int nCentros = this.centros.size();

        //Array que contiene para cada sensor cual es su centro más cercano y la distancia entre ellos.
        ArrayList<PriorityQueue<Pair<Integer, Double> > > distanciasACentros = new ArrayList<>();

        for (int i = 0; i < nSensores; ++i) {
            PriorityQueue<Pair<Integer, Double> > distancias = new PriorityQueue<>(
                    (p1, p2) -> {
                        // Primero comparamos por la distancia (segundo elemento de cada par)
                        int distComp = Double.compare(p1.getSecond(), p2.getSecond());

                        // Si las distancias son iguales, comparamos por el identificador (primer elemento de cada par)
                        if (distComp == 0) {
                            return Integer.compare(p1.getFirst(), p2.getFirst());
                        }
                        return distComp;
                    }
            );
            calcularDistanciasACentros(i, distancias);
            distanciasACentros.add(distancias);
        }

        //Array que nos dirá en cada momento, que sensores se han conectado.
        boolean[] sensoresConectados = new boolean[nSensores];
        Arrays.fill(sensoresConectados, false);

        //Conectamos sensores de capacidad 5 a sus centros. Sabemos que los sensores de capacidad 5 son aquellos cuyo ID%3 = 2.
        for (int i = 2; i < nSensores; i += 3) {
            PriorityQueue<Pair<Integer, Double> > distancias = distanciasACentros.get(i);
            boolean found = false;
            while (!found) {
                if (!distancias.isEmpty()) {
                    Pair<Integer, Double> sensorMasCercano = distancias.poll();
                    int centroID = sensorMasCercano.getFirst();
                    if (centroID == -1)
                        continue;
                    if (centroEsValido(centroID)) {
                        conectar(i, centroID);
                        actualizarCapacidad(centroID);
                        sensoresConectados[i] = true;
                        found = true;
                    }
                    sensorMasCercano = distanciasACentros.get(i).poll();
                }
                else {
                    int x = this.sensores.get(i).getCoordX();
                    int y = this.sensores.get(i).getCoordY();
                    //Buscamos el sensor o centro válido más cercano para conectarnos.
                    int centroID = buscarEnTablero(x, y, 'T');
                    if (centroID != -1) {
                        conectar(i, centroID);
                        actualizarCapacidad(centroID);
                        sensoresConectados[i] = true;
                    }
                    found = true;
                }
            }
        }

        //Ahora conectaremos sensores de coste 1 y 2 según la estrategia explicada anteriormente.
        for (int i = 0; i < nSensores; ++i) {
            if (i % 3 == 2)
                continue;
            PriorityQueue<Pair<Integer, Double> > distancias = distanciasACentros.get(i);
            Pair<Integer, Double> sensorMasCercano = distancias.poll();
            int centroID = sensorMasCercano.getFirst();
            double distancia = sensorMasCercano.getSecond();
            if (centroID == -1)
                continue;
            if (centroEsValido(centroID)) {
                conectar(i, centroID);
                actualizarCapacidad(centroID);
                sensoresConectados[i] = true;
            }
            //El centro más cercano no está disponible. Intentamos conectarnos al sensor válido más cercano a un centro.
            else {
                int id = buscarEnTablero(this.sensores.get(i).getCoordX(), this.sensores.get(i).getCoordY(), 'T');
                conectar(i, id);
                actualizarCapacidad(id);
            }
        }
    }


    /**
     * Se genera un estado usando la estrategia aleatoria.
     * se conectan todos los sensores de forma aleatoria, pero asegurandonos de seguir las
     * restricciones.
     */

    private void generarEstadoAleatorio() {
        Random rand = new Random();

        for (int i = 0; i < sensores.size(); i++) {
            int centroId = sensores.size() + rand.nextInt(centros.size());
            if (centroEsValido(centroId)) {
                conectar(i, centroId);
                actualizarCapacidad(centroId);
            } else {
                //si no hay un centro valido me conecto a un sensor random pero tengo que mirar que sea válido y que no sea el mismo
                //sensor porque no me puedo conectar a mi mismo
                int intentos = 0; //evitamos quedarnos en un bucle infinito
                int sensorId = rand.nextInt(sensores.size());
                while ((!centroEsValido(sensorId) || sensorId == i) && intentos < sensores.size()) {
                    sensorId = rand.nextInt(sensores.size());
                    ++intentos;
                }
                if (intentos == sensores.size()) {
                    System.out.println("Advertencia: No se encontró conexión válida para el sensor " + i);
                    continue;
                }
                conectar(i, sensorId);
                actualizarCapacidad(sensorId);
            }

        }
    }



    /**
     *Función que calcula la distancia de un sensor con id ID de cada uno de los centros,
     * clasificandolos según su cercania. Esta función nos sirve para determinar que centro
     * es el más cercano a cada sensor.
     *
     * @param ID id del sensor
     * @param distancias cola prioritaria donde colocamos el id del centro y la distancia entre él y ID. Se ordena segun el valor de la distancia.
     */
    private void calcularDistanciasACentros(int ID, PriorityQueue<Pair<Integer, Double> > distancias) {
        for (int i = 0; i < this.centros.size(); ++i) {
            double distancia = dist(ID, i, true);
            distancias.add(new Pair<Integer, Double>(i + this.sensores.size(), distancia));
        }
    }

    private int SensorDistanciaMinima(ArrayList<Pair<Integer, Double> > distancias, boolean[] sensoresComprobados) {
        double minDist = Double.POSITIVE_INFINITY;
        int minID = -1;
        for (int i = 0; i < sensores.size(); ++i) {
            if (distancias.get(i).second < minDist && !sensoresComprobados[i]) {
                minDist = distancias.get(i).second;
                minID = i;
            }
        }
        return minID;
    }

    /**
     * @param posX es la posición X del sensor transmisor en el tablero
     * @param posY es la posición Y del sensor transmisor en el tablero
     * @param objetivo Si es 'C', se buscará el centro VÁLIDO más cercano.
     *                 En caso que sea 'S' se busca el sensor VÁLIDO más cercano.
     *                 En caso que sea 'T' se busca el sensor o centro VÁLIDO más cercano
     * @return el ID de el objetivo encontrado. -1 en caso de no haber encontrado a ninguno.
     * Esta función la usamos en el generador por cercania para encontrar centros o sensores cercanos
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
                if (objetivo == 'C' || objetivo == 'T') {
                    if (es_centro(ID) && centroEsValido(ID)) {
                        return ID;
                    }
                }
                if (objetivo == 'S' || objetivo == 'T') {
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

    /**
     * funcion usada para comprobar si un nodo con una id determinada es un sensor.
     * Si el id es mayor o igual a 0 y menos al tamaño de la lista de sensores,
     * es un id de sensor.
     *
     * @param sensorId id del sensor potencial
     * @return booleano que indica si el nodo con id sensorId es un sensor
     */
    private boolean es_sensor(int sensorId) {
        return sensorId >= 0 && sensorId < sensores.size();
    }

    /**
     * funcion usada para comprobar si un nodo con una id determinada es un centro
     * Si el id es mayor o igual al tamaño de la lista de sensores (ya que los ids de los centros
     * comienzan desde el valor sensores.size()) y menor a la suma de tamaño de las listas
     * de sensores y centros, es un id de centro.
     *
     * @param centroId id del centro potencial
     * @return booleano que indica si el nodo con id centroId es un centro
     */
    private boolean es_centro(int centroId) {
        return centroId >= sensores.size() && centroId < sensores.size()+centros.size();
    }

    /**
     * Función usada para comprobar si un nodo con un id determinada es un sensor válido.
     * Primero, se mira el número de conexiones actuales a sensorId (tiene que ser menor a 3).
     * Además se mira la información recibida total del sensor y comprueba que la capacidad de
     * este sensor es suficiente (siendo la capacidad el doble de 1, 2, o 5 Mb/s).
     * Además, comprueba si está conectado a un centro (mirando si existe un camino válido para que eso ocurra).
     *
     * @param sensorId id del sensor del cual queremos comprobar la validez
     * @return booleano que indica si el nodo con id sensorId es un sensor válido
     */
    private boolean sensorEsValido(int sensorId) {
        // Verificar restricción conexiones simultáneas
        int numeroConexionesActuales = this.quienMeTransmite.get(sensorId).size();

        double informacionRecibida = calcularInformacionRecibida(sensorId);

        // Verificar si centroId está indirectamente conectado a un centro
        boolean conectadoConCentro = existeCaminoValido(sensorId);

        return (numeroConexionesActuales < 3 && (capacidadesIniciales[sensorId % 3] * 2 - informacionRecibida) > 0) && conectadoConCentro;
    }

    /**
     * Función usada para comprobar si un nodo con un id determinado es un centro válido.
     * Primero, se mira el número de conexiones actuales a sensorId (tiene que ser menor a 25).
     * Además, que no recibe más de 150 Mb/s
     *
     * @param centroId id del centro del cual queremos comprobar la validez
     * @return booleano que indica si el nodo con id centroId es un centro válido
     */
    private boolean centroEsValido(int centroId) {
        // Verificar restricción conexiones simultáneas
        int numeroConexionesActuales = this.quienMeTransmite.get(centroId).size();

        // Verificar restricción datos recibidos simultaneamente
        double informacionRecibida = calcularInformacionRecibida(centroId);

        return (numeroConexionesActuales < 25 && (150 - informacionRecibida) > 0);
    }

    /**
     * Función usada calcular la información que recibe cierto elemento. Se recorren todos los elementos que
     * transmiten al nodo en cuestion y se suman a una variable. A continuación, la información recibida será
     * el minimo entre esa suma y la capacidad maxima del elemento, ya que no se debe superar
     *
     * @param IDElemento id del centro/sensor del cual queremos calcular la información que recibe
     * @return la cantidad de info recibida.
     */

    private double calcularInformacionRecibida(int IDElemento) {
        double cantidadInformacionRecibida = 0;
        int nElementosConectados = this.quienMeTransmite.get(IDElemento).size();
        for (int i = 0; i < nElementosConectados; i++) {
            int elementoConectado = this.quienMeTransmite.get(IDElemento).get(i);
            double informacionTransmitida = capacidadesIniciales[elementoConectado % 3];
            cantidadInformacionRecibida += (calcularInformacionRecibida(elementoConectado) + informacionTransmitida);
        }
        return Math.min(maximaCapacidad(IDElemento), cantidadInformacionRecibida);
    }

    /**
     * Función usada para calcular la maxima capacidad de un elemento. Puede ser 2, 4, 6.
     * asignamos 1, 2 y 5 succesivamente a los IDs de los sensores según el modulo 3 de su id.
     * En caso de los centros siempre será 150.
     *
     * @param ID id del centro/sensor del cual queremos saber la maxima capacidad
     * @return double con la capacidad maxima
     */
    private double maximaCapacidad(int ID) {
        if (es_centro(ID))
            return (double) 150;
        else
            return (capacidadesIniciales[ID%3] * 2);
    }


    /**
     * Función usada para comprobar si existe un camino valido para un sensor. Miramos si
     * se puede conectar a un centro.
     *
     * @param ID id del sensor del cual queremos saber si tenemos un camino valido
     * @return booleano que indica si hay un camino valido
     */
    private boolean existeCaminoValido(int ID) {
        boolean[] visitado = new boolean[this.sensores.size()];
        Arrays.fill(visitado, false);
        return conexionACentro(ID, visitado);
    }


    /**
     * Función usada para comprobar si se puede conectar un sensor a un centro (directa o
     * indirectamente)
     *
     * @param ID id del sensor del cual queremos saber si podemos conectarlo a un centro
     * @param visitado lista de booleanos donde guardamos si nuestro sensor se puede conectar a un centro en la posicion correspondiente.
     * @return booleano que indica si se puede conectar a un centro
     */
    private boolean conexionACentro(int ID, boolean[] visitado) {
        if(es_centro(ID)) return true;
        if (es_centro(aQuienTransmito[ID])) return true;
        if(aQuienTransmito[ID] == -1) return false;
        if(visitado[aQuienTransmito[ID]] == true) return false;
        visitado[ID] = true;
        return conexionACentro(aQuienTransmito[ID], visitado);
    }


    /**
     * Función usada para calcular la distancia entre 2 elementos
     *
     * @param id1 id del primer elemento
     * @param id2 id del segundo elemento
     * @param esCentro booleano que indica si id2 es un centro
     * @return distancia entre id1 y id2
     */
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

    /**
     * Función usada para comprobar si cierta posición es valida, sin superar el tablero de 100x100
     *
     * @param x coordenada x de cierto elemento
     * @param y coordenada y de cierto elemento
     * @return booleano que indica si la posicion (x,y) es valida
     */
    private boolean pos_valida(int x, int y) {
        return x < 100 && x >= 0 && y < 100 && y >= 0;
    }

    /**
     * Función usada para actualizar todas las capacidades haciendo un recorrido por ellas
     *
     */
    public void actualizarCapacidades() {
        for (int i = 0; i < capacidadRestante.length; i++) {
            actualizarCapacidad(i);
        }
    }


    /**
     * Función usada para modificar su capacidad. La usamos al conectar un sensor a otro sensor o un centro
     * la capacidad sera el maximo entre 0 y la cantidad maxima receptora menos la información recibida,
     * que calculamos usando la función correspondiente
     *
     * @param ID id del elemento a actualizar (puede ser un sensor o un centro)
     */
    public void actualizarCapacidad(int ID) {
        double cantidadMaximaReceptora;
        if (es_centro(ID)) {
            cantidadMaximaReceptora = 150;
        } else {
            cantidadMaximaReceptora = capacidadesIniciales[ID % 3] * 2;
        }
        double informacionRecibida = calcularInformacionRecibida(ID);
        double nuevaCapacidad = Math.max(0, cantidadMaximaReceptora - informacionRecibida);
        //System.out.println("Actualizada la capacidad restante de " + ID + ":");
        //System.out.println("Nueva Capacidad: " + nuevaCapacidad);
        capacidadRestante[ID] = nuevaCapacidad;
    }

    /**
     * Función usada para conectar un sensor a otro sensor o un centro, comprobando siempre las restricciones
     * usando los metodos pertinentes
     *
     * @param ID1 id del sensor a conectar (si ID1 es un centro, no llevamos a cabo la operación)
     * @param ID2 id del elemento al cual queremos conectar ID1
     */
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

    /**
     * Función usada para desconectar un sensor, comprobando siempre las restricciones
     * usando los metodos pertinentes. hacemos los cambios pertinentes en nuestros atributos
     *
     * @param ID1 id del sensor a desconectar.
     */

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

    /**
     * Función usada para desconectar un sensor de cierto elemento para conectarlo a otro, comprobando
     * las restricciones pertinentes (por ejemplo, si alguno de los ids es de un centro, no se realiza la operación)
     * @param ID1 sensor sobre el cual queremos realizar la operación
     * @param ID2 sensor sobre el cual queremos realizar la operación.
     * @return booleano que nos indica si la operación se ha podido realizar con exito
     */
    public boolean swap(int ID1, int ID2) {
        if (es_centro(ID1) || es_centro(ID2)) {
            //System.out.println("No se ha podido realizar la operacion: SWAP (" + ID1 + " -> " + ID2 + ") debido a que uno de los IDs pertenece a un centro");
            System.out.println();
            return false;
        }
        int conexionID1 = aQuienTransmito[ID1];
        int conexionID2 = aQuienTransmito[ID2];
        if (conexionID1 == -1 || conexionID2 == -1) {
            //System.out.println("No se ha podido realizar la operacion: SWAP (" + ID1 + " -> " + ID2 + ") debido a que uno de los dos IDs no está conectado a nada");
            //System.out.println();
            return false;
        }
        if (conexionID1 == conexionID2) {
            //System.out.println("No se ha podido realizar la operacion: SWAP (" + ID1 + " -> " + ID2 + ") debido a que los dos sensores están conectados a lo mismo");
            //System.out.println();
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
    /**
     * Función usada para desconectar un sensor de cierto elemento para conectarlo a otro, comprobando
     * las restricciones pertinentes
     * @param ID sensor sobre el cual queremos realizar la operación (si ID es un centro, no podemos hacer la operación)
     * @param nuevoDestino elemento al cual queremos conectar ID
     * @return booleano que nos indica si la operación se ha podido realizar con exito
     */

    public boolean moverConexion(int ID, int nuevoDestino) {
        int antiguoDestino = aQuienTransmito[ID];
        if (es_centro(ID)) {
            //System.out.println("No se ha podido realizar la operacion: moverConexion (" + ID + " -> " + nuevoDestino + ") debido a que uno de los IDs pertenece a un centro");
            //System.out.println();
            return false;
        }
        desconectar(ID);
        if (es_centro(nuevoDestino)) {
            if (!centroEsValido(nuevoDestino)) {
                conectar(ID, antiguoDestino);
                return false;
            }
        }
        else {
            if (!sensorEsValido(nuevoDestino)) {
                conectar(ID, antiguoDestino);
                return false;
            }
        }
        conectar(ID, nuevoDestino);
        actualizarCapacidad(antiguoDestino);
        actualizarCapacidad(nuevoDestino);
        return true;
    }

    /**
     * funcion getter del numero de sensores
     * @return total de sensores
     */

    public int getNumeroSensores() {
        return this.sensores.size();
    }

    /**
     * funcion getter del numero de centros
     * @return total de centros
     */
    public int getNumeroCentros() {
        return this.centros.size();
    }

    /* ===================== SOLUCIÓN VÁLIDA ===================== */

    /* ===================== OPERADORES DE BÚSQUEDA LOCAL ===================== */

    /* ===================== ESTADOS INICIALES ===================== */


    /**
     * Función usada para generar un estado inicial random
     *
     */
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

    /**
     *Función usada para calcular la heuristica. Recorremos la lista de receptores
     * comprobando que todos los sensores estén conectados a algún elemento.
     * Si un sensor i está conectado a un centro, calculamos su distancia con esCentro = true,
     * si no, esCentro será false. A continuación, cogemos la capacidad del sensor i y la sumamos
     * al coste total junto a la potencia de 2 de la distancia. A continuación, calculamos la
     * información real sumando la totalidad de capacidades restantes. Esta totalidad la restamos de
     * la capacidad acumulada de todos los centros (numero de centros multiplicado por 150).
     * Finalmente, la heuristica, será el costeTotal, multiplicandole un coeficiente a
     * y restandole la información transmitida multiplicada por un coeficiente b
     * @return valor de la heuristica.
     */
    public double getHeuristica() {
        double costeTotal = 0;
        double inforeal = 0;
        this.coste = 0;
        this.info = 0;
        for (int i = 0; i < aQuienTransmito.length; i++) {
            if (aQuienTransmito[i] != -1) { // Si el sensor i tiene conexión
                double distancia;
                if (es_centro(aQuienTransmito[i])) {
                    distancia = dist(i, aQuienTransmito[i]-sensores.size(),true );
                }
                else {
                    distancia = dist(i, aQuienTransmito[i],false);
                }
                double volumenCapturado = sensores.get(i).getCapacidad();
                //System.out.println();
                //System.out.println("sensord con id = " + i + " envia " + sensores.get(i).getCapacidad() + " a al id " + aQuienTransmito[i]);
                costeTotal += Math.pow(distancia, 2) * volumenCapturado;
            }
        }
        for(int i = sensores.size(); i < sensores.size()+ centros.size(); i++) {
            inforeal += capacidadRestante[i];
        }

        this.coste = costeTotal;
        this.info = centros.size()*150 - inforeal;
        //System.out.println("info= " + sensores.get(0).getCapacidad() + " coste = " + this.coste);
        return a*coste - b*info;
    }


    /**
     * Imprimir todas las conexiones actuales
     */
    public void imprimirConexiones() {
        System.out.println("Conexiones:");
        for (int i = 0; i < this.sensores.size(); ++i) {
            String s = new String("El sensor " + i + " está conectado al ");
            int receptor = aQuienTransmito[i];
            if (es_centro(receptor)) {
                s += "centro ";
                s += receptor - this.sensores.size();
            }
            else {
                s += "sensor ";
                s += receptor;
            }
            double capacidadInicialSensor = capacidadesIniciales[i % 3];
            double informacionTransmitida = capacidadInicialSensor + calcularInformacionRecibida(i);
            s += (" y le transmite " + informacionTransmitida + " MB/s");
            System.out.println(s);
        }
    }

    /**
     * convertimos nuestra información en string
     * @return String con la información correspondiente
     */
    @Override
    public String toString() {
        return "Sensores: " + sensores.size() +
                ", Centros: " + centros.size() +
                ", Conexiones: " + Arrays.toString(aQuienTransmito) +
                ", Coste: " + coste +
                ", Información transmitida: " + info;
    }
}