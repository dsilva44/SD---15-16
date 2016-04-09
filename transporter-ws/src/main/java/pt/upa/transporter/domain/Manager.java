package pt.upa.transporter.domain;

import pt.upa.transporter.ws.JobView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Manager {
    private final String parity;
    private List<String> workCities;
    private List<JobView> jobs;

    public Manager(String transporterName) {
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
