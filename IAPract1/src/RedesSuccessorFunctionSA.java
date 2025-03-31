import IA.Red.CentrosDatos;
import IA.Red.Sensores;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import java.util.*;
/**
 * Class Name: RedesSuccessorFunctionSA
 *
 * Description:
 * Genera los sucesores para ejecutar Simulated Annealing
 *
 * @author Grup_IA
 * @version 1.0
 */
public class RedesSuccessorFunctionSA implements SuccessorFunction{
    /**
     * Metodo que genera los sucessores para simulated annealing
     *
     * @param a estado padre
     * @return lista de estados hijos
     */
    public List<Successor> getSuccessors(Object a) {
        ArrayList<Successor> retVal = new ArrayList();
        EstadoTest hijo = (EstadoTest) a;
        Random random = new Random();
        int nsensores = hijo.getSensores().size();
        int ncentros = hijo.getCentros().size();

        int factorRamificacionMoverConexion = nsensores*ncentros;
        int factorRamificacionSwap = nsensores*nsensores;
        int factorRamificacionTotal = factorRamificacionMoverConexion + factorRamificacionSwap;

        int numRand = random.nextInt(factorRamificacionTotal);
        EstadoTest newState = new EstadoTest(nsensores, ncentros);
        if (numRand < factorRamificacionMoverConexion) { //operador1
            int sensorRandom = random.nextInt(nsensores);
            int nodoRandom = random.nextInt(nsensores + ncentros);
            while (!newState.moverConexion(sensorRandom, nodoRandom)) {
                sensorRandom = random.nextInt(nsensores);
                nodoRandom = random.nextInt(nsensores + ncentros);
                newState = new EstadoTest(hijo.getSensores(), hijo.getCentros(), hijo.getReceptores(),
                        hijo.getCapacidadRestante(), hijo.getTransmisores(), hijo.getTablero(), hijo.getCoste(), hijo.getInfo());
            }
            String S;
            if (nodoRandom >= nsensores) {
                S = ("MOVIDA conexión: " + sensorRandom + " al centro: " + (nodoRandom - nsensores) + " | " + newState.toString() + "\n");
            }
            else {
                S = ("MOVIDA conexión: " + sensorRandom + " al sensor: " + nodoRandom + " | " + newState.toString() + "\n");
            }
            retVal.add(new Successor(S, newState));
        }

        else { //operador2
            int sensorRandom1 = random.nextInt(nsensores);
            int sensorRandom2 = random.nextInt(nsensores);
            while (!newState.swap(sensorRandom1, sensorRandom2)) {
                sensorRandom1 = random.nextInt(nsensores);
                sensorRandom2 = random.nextInt(nsensores);
                newState = new EstadoTest(hijo.getSensores(), hijo.getCentros(), hijo.getReceptores(),
                        hijo.getCapacidadRestante(), hijo.getTransmisores(), hijo.getTablero(), hijo.getCoste(), hijo.getInfo());            }
            String S;
            S = ("INTERCAMBIO " + " " + sensorRandom1 + " " + sensorRandom2 + " | " + newState.toString() + "\n");
            retVal.add(new Successor(S, newState));
        }
        return retVal;
    }
}

