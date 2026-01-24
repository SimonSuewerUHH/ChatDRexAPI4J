package de.hamburg.university.agent.memory;

import de.hamburg.university.helper.drugstone.dto.DrugstOneDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryStateHolder {
    private final ConcurrentHashMap<Object, List<String>> threads = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Object, List<PlanStateResult>> states = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Object, DrugstOneDTO> sharedDrugstOne = new ConcurrentHashMap<>();


    public void removeClient(String clientId) {
        if (!threads.containsKey(clientId)) {
            return;
        }
        List<String> threadIds = threads.get(clientId);
        for (String threadId : threadIds) {
            states.remove(threadId);
            sharedDrugstOne.remove(threadId);
        }
        threads.remove(clientId);
    }

    public List<PlanStateResult> getStates(String clientId, String threadId) {
        insureThreadExists(clientId, threadId);
        if (!states.containsKey(threadId)) {
            states.put(threadId, new ArrayList<>());
        }
        return states.get(threadId);
    }

    public DrugstOneDTO getDrugstOne(String sessionId) {
        if (!sharedDrugstOne.containsKey(sessionId)) {
            sharedDrugstOne.put(sessionId, new DrugstOneDTO());
        }
        return sharedDrugstOne.get(sessionId);
    }

    public void addState(String clientId, String threadId, PlanStateResult state) {
        insureThreadExists(clientId, threadId);
        if (!states.containsKey(threadId)) {
            states.put(threadId, new ArrayList<>());
        }
        states.get(threadId).add(state);
    }

    private void insureThreadExists(String clientId, String threadId) {
        if (!threads.containsKey(clientId)) {
            threads.put(clientId, new ArrayList<>());
        }
        List<String> threadIds = threads.get(clientId);
        if (!threadIds.contains(threadId)) {
            threadIds.add(threadId);
        }
    }
}
