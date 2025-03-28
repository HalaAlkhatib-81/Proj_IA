import java.util.*;
import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

import IA.Red.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Num de sensores: ");
        int nsensores = scanner.nextInt();

        System.out.print("Introduce numero de centros de datos: ");
        int ncentros = scanner.nextInt();

        System.out.print("¿Qué algoritmo quieres usar? [1= Hill Climbing / 0= Simulated Annealing]: ");
        boolean hC = (scanner.nextInt() == 1);

        int steps = 10000, stiter = 1000, k = 25;
        double lambda = 0.01;
        if (!hC) {
            System.out.println("Introduce los parámetros de Simulated Annealing:");
            System.out.print("Num Steps [10000 por defecto]: ");
            steps = scanner.nextInt();
            System.out.print("Stiter [1000 por defecto]: ");
            stiter = scanner.nextInt();
            System.out.print("K [25 por defecto]: ");
            k = scanner.nextInt();
            System.out.print("Lambda [0.01 por defecto]: ");
            lambda = scanner.nextDouble();
        }

        int semillaSensores;
        int semillaCentros;
        System.out.print("¿Semillas random? [1 = Si, 0 = No]: ");
        if(scanner.nextInt() == 1){
            Random r = new Random();
            semillaSensores = r.nextInt();
            System.out.println("La semilla random para los sensores es: " + semillaSensores);
            semillaCentros = r.nextInt();
            System.out.println("La semilla random para los Centros es: " + semillaCentros);
        }
        else{
            System.out.print("Introduce la semilla para los sensores: ");
            semillaSensores = scanner.nextInt();
            System.out.print("Introduce la semilla para los centros: ");
            semillaCentros = scanner.nextInt();
        }
        System.out.print("¿Qué estrategia para la solución inicial? [1 para avariciosa / 0 para aleatoria]: ");
        boolean greedy = (scanner.nextInt() == 1);

        System.out.print("Que funcion heuristica quieres usar?[1 = solo coste/ 0 = coste + info]: ");
        boolean infoPerdidaIncluida = (scanner.nextInt() == 1);

        double a = 0.1, b = 0.2;
        if(!infoPerdidaIncluida){
            System.out.println("A continuación introduce el valor de A y el valor de B");
            System.out.print("Valor a por defecto -> 0.1, Valor b por defecto = 0.2");
            a = scanner.nextDouble();
            b = scanner.nextDouble();
        }
        else{
            a = 0.6;
            b = 0.4;
        }

        long startTime = System.nanoTime();

        semillaSensores = 1234;
        semillaCentros = 4321;

        Sensores sensors = new Sensores(nsensores, semillaSensores);
        CentrosDatos centros = new CentrosDatos(ncentros, semillaCentros);

        Estado.a = a;
        Estado.b = b;

        /**
         * Código de Alex: Borrar a partir de aquí:
         */
        //Actualizar las variables static en base al input del usuario
        EstadoTest.a = a;
        EstadoTest.b = b;
        EstadoTest.semillaSensores = semillaSensores;
        EstadoTest.semillaCentros = semillaCentros;

        /**
        Generar estado inicial: si Greedy == True, se genera usando
        la estrategia de cercanía. En caso contrario, se decide el
        próximo paso aleatoriamente.
         */

        /*
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("GENERANDO ESTADO INICIAL:");
        System.out.println("-------------------------------------------------------------------------");
         */

        EstadoTest estadoInicialTest = new EstadoTest(nsensores, ncentros);
        estadoInicialTest.generarEstadoInicial(greedy);

        /*
        System.out.println("ESTADO INICIAL GENERADO:");
        System.out.println();
        estadoInicialTest.imprimirConexiones();
         */

        /**
         * Borrar hasta aquí
         */
        Estado estadoInicial = new Estado(greedy, sensors, centros);


        if (hC) {
            ejecutarHillClimbing(estadoInicialTest);
        } else {
            ejecutarSimulatedAnnealing(estadoInicial, steps, stiter, k, lambda);
        }

        long endTime = System.nanoTime();
        System.out.println("Duración del algoritmo: " + (endTime - startTime) / 1000000 + " ms");
    }

    private static void ejecutarHillClimbing(EstadoTest estado) {
        System.out.println("\nEjecutando Hill Climbing...");
        try {
            RedesSuccessorFunction successorFunction = new RedesSuccessorFunction();
            RedesGoalTest goalTest = new RedesGoalTest();
            RedesHeuristicFunction heuristicFunction = new RedesHeuristicFunction();
            Problem p = new Problem(estado, successorFunction, goalTest, heuristicFunction);
            Search search = new HillClimbingSearch();
            SearchAgent agent = new SearchAgent(p, search);

            imprimirResultados(agent);
            //imprimirIntrumentacion
            EstadoTest solucion = (EstadoTest) search.getGoalState();
            System.out.println("\nSolución Final: " + solucion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ejecutarSimulatedAnnealing(Estado es, int steps, int stiter, int k, double lambda) {
        System.out.println("\nEjecutando Simulated Annealing...");
        try {
            RedesSuccessorFunctionSA successorFunction = new RedesSuccessorFunctionSA();
            RedesGoalTest goalTest = new RedesGoalTest();
            RedesHeuristicFunction heuristicFunction = new RedesHeuristicFunction();
            Problem p = new Problem(es, successorFunction, goalTest, heuristicFunction);
            Search search = new SimulatedAnnealingSearch(steps, stiter, k, lambda);
            SearchAgent agent = new SearchAgent(p, search);

            imprimirResultados(agent);
            Estado solucion = (Estado) search.getGoalState();
            System.out.println("\nSolución Final: " + solucion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void imprimirResultados(SearchAgent agent) {
        System.out.println();
        for (Object action : agent.getActions()) {
            System.out.println(action);
        }
        Properties properties = agent.getInstrumentation();
        for (Object key : properties.keySet()) {
            System.out.println(key + " : " + properties.getProperty((String) key));
        }
    }

}