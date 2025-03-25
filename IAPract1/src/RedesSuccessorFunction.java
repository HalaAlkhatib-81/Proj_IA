import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import IA.Red.*;

import java.util.*;

public class RedesSuccessorFunction implements SuccessorFunction{
    public List<Successor> getSuccessors(Object a) {
        ArrayList<Successor> retVal = new ArrayList();
        Estado estadoActual = (Estado) a;

        CentrosDatos centros = estadoActual.centros;
        System.out.println();
        for (int i = 0; i < estadoActual.sensor.size(); i++) {
            for (int j =  0; j < estadoActual.sensor.size(); ++j) {
                Sensores nuevaListaSensores = new Sensores(0, 1234);
                for (Sensor s : estadoActual.sensor) {
                    nuevaListaSensores.add(new Sensor((int)s.getCapacidad(), s.getCoordX(), s.getCoordY()));
                }
                Estado newState = new Estado(estadoActual,nuevaListaSensores,estadoActual.centros);
                if (newState.swap(i, j)) {
                    System.out.println("SWAP " + i + " " + j + "");
                    String S = ("INTERCAMBIO " + " " + i + " " + j + " " + newState.toString() + "\n");
                    retVal.add(new Successor(S, newState));
                }
            }
        }

        for (int i = 0; i < estadoActual.sensor.size(); i++) {
            for (int j = 0; j < estadoActual.sensor.size() + centros.size(); ++j) { // Puede ser otro sensor o un centro
                if (i != j) { // No puede moverse a sí mismo
                    Sensores nuevaListaSensores = new Sensores(0, 1234);
                    for (Sensor s : estadoActual.sensor) {
                        nuevaListaSensores.add(new Sensor((int)s.getCapacidad(), s.getCoordX(), s.getCoordY()));
                    }
                    Estado newState = new Estado(estadoActual,nuevaListaSensores,estadoActual.centros);
                    if (newState.moverConexion(i, j)) {
                        String S;
                        System.out.println("MOVER" + i + " " + j);
                        if (j >= estadoActual.sensor.size()) {
                            S = "MOVIDA conexión: " + i + " al centro: " + (j - estadoActual.sensor.size()) + " " + newState.toString() + "\n";
                        }
                        else S = "MOVIDA conexión: " + i + " al sensor: " + j + " " + newState.toString() + "\n";
                        retVal.add(new Successor(S, newState));
                    }
                }
            }
        }
        System.out.println("NOUS " + retVal.size());
        return retVal;
    }
}
