package pt.upa.transporter.ws;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DomainManager {
    private final String parity;
    private List<String> workCities;
    private List<JobView> jobs;

    public DomainManager(String transporterName) {
        int tNum = Integer.parseInt(transporterName.substring(transporterName.length() - 1));
        if (tNum % 2 == 0) { parity = "EVEN"; }
        else parity = "ODD";
    }

    private void init() {
        switch (parity) {
            case "EVEN":
                workCities = new ArrayList<>(Arrays.asList(
                        "Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda",
                        "Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança"));
            case "ODD":
                workCities = new ArrayList<>(Arrays.asList(
                        "Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda",
                        "Setúbal", "Évora", "Portalegre", "Beja", "Faro"));
        }
    }

    public String getParity() {
        return parity;
    }

    public boolean decideResponde() {
        //TODO RequestJob
        return false;
    }

    public void TransportSimulation() {
        // TODO
    }
}
