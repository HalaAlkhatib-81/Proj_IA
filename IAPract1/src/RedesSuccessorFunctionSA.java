import IA.Red.CentrosDatos;
import IA.Red.Sensores;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import java.util.*;

public class RedesSuccessorFunctionSA implements SuccessorFunction{
    public List<Successor> getSuccessors(Object a) {
        ArrayList<Successor> retVal = new ArrayList();
        EstadoTest hijo = (EstadoTest) a;
        Random random = new Random();
        int nsensores = hijo.getSensores().size();
        int ncentros = hijo.getCentros().size();

        int operacionElegida = random.nextInt(2);
        int element = random.nextInt(nsensores);
        EstadoTest newState = hijo.clone();
        //SWAP
        if (operacionElegida == 0) {
            ArrayList<Integer> posiblesSensores = new ArrayList();
            for (int i = 0; i < nsensores; i++) {
                posiblesSensores.add(i);
            }
            boolean found = false;
            while (!found && posiblesSensores.size() > 0) {
                int nextElementIt = random.nextInt(posiblesSensores.size());
                int nextElement = posiblesSensores.get(nextElementIt);
                posiblesSensores.remove(nextElementIt);
                if (newState.swap(element, nextElement)) {
                    String S = ("INTERCAMBIO " + " " + element + " " + nextElement + " | " + newState.toString() + "\n");
                    retVal.add(new Successor(S, newState));
                    found = true;
                }
            }
        }
        //MOVE
        else {
            ArrayList<Integer> posiblesSensores = new ArrayList();
            for (int i = 0; i < nsensores + ncentros; i++) {
                posiblesSensores.add(i);
            }
            boolean found = false;
            while (!found && posiblesSensores.size() > 0) {
                int nextElementIt = random.nextInt(posiblesSensores.size());
                int nextElement = posiblesSensores.get(nextElementIt);
                posiblesSensores.remove(nextElementIt);
                if (newState.moverConexion(element, nextElement)) {
                    String S = ("INTERCAMBIO " + " " + element + " " + nextElement + " | " + newState.toString() + "\n");
                    retVal.add(new Successor(S, newState));
                    found = true;
                }
            }
        }
        return retVal;
    }
}

