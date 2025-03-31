import aima.search.framework.HeuristicFunction;
/**
 * Class Name: RedesHeuristicFunction
 *
 * Description:
 * recupera el valor heuristico del estado
 * @author Grup_IA
 * @version 1.0
 */
public class RedesHeuristicFunction implements HeuristicFunction{

    /**
     * Metodo que recupera el valor de la heuristica del estado
     *
     * @param o estado padre
     * @return valor heuristico
     */
    public double getHeuristicValue(Object o) {
        EstadoTest estado = (EstadoTest) o;
        return estado.getHeuristica();
    }
}
