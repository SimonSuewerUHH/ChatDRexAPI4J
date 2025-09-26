package de.hamburg.university.agent.memory;

import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.helper.drugstone.dto.DrugstOneDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryStateHolder {
    private final ConcurrentHashMap<Object, List<PlanStateResult>> states = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Object, DrugstOneDTO> sharedDrugstOne = new ConcurrentHashMap<>();

    public void addClient(String clientId, ChatRequestDTO request) {
        states.put(clientId, new ArrayList<>());
        sharedDrugstOne.put(clientId, new DrugstOneDTO());
    }

    public void removeClient(String clientId) {
        states.remove(clientId);
        sharedDrugstOne.remove(clientId);
    }

    public List<PlanStateResult> getStates(String clientId) {
        if (!states.containsKey(clientId)) {
            states.put(clientId, new ArrayList<>());
        }
        return states.get(clientId);
    }

    public DrugstOneDTO getDrugstOne(String clientId) {
        if (!sharedDrugstOne.containsKey(clientId)) {
            sharedDrugstOne.put(clientId, new DrugstOneDTO());
        }
        return sharedDrugstOne.get(clientId);
    }

    public void addState(String clientId, PlanStateResult state) {
        if (!states.containsKey(clientId)) {
            states.put(clientId, new ArrayList<>());
        }
        states.get(clientId).add(state);
    }
}
