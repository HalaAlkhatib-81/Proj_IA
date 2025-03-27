import aima.search.framework.HeuristicFunction;

public class RedesHeuristicFunction implements HeuristicFunction{
    public double getHeuristicValue(Object o) {
        EstadoTest estado = (EstadoTest) o;
        return estado.getHeuristica();
    }
}
