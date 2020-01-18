package busstop.customtrip.model;

import java.io.Serializable;

public class FeaturesCount implements  Serializable {

    private static final long serialVersionUID = 7316776533289077690L;
    int nodes;
    int ways;
    int relations;

    public int waysCount() {
        return ways;
    }

    public int relationsCount() {
        return relations;
    }

    public int nodesCount() {
        return nodes;
    }

    public int aggregatedCount() { return nodes + ways + relations; }

    public FeaturesCount(int n, int w, int r) {
        nodes     = n;
        ways      = w;
        relations = r;
    }

    @Override
    public String toString() {
        return "Count{" +
                "nodes=" + nodes + " ways=" + ways + " relations=" + relations +
                '}';
    }
}
