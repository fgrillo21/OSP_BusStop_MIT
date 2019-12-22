package busstop.customtrip.model;

public class FeaturesCount {

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
