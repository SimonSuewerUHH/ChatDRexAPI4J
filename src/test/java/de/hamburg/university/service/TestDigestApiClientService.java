package de.hamburg.university.service;

import com.fasterxml.jackson.core.type.TypeReference;
import de.hamburg.university.helper.JsonLoader;
import de.hamburg.university.service.digest.DigestApiClientService;
import de.hamburg.university.service.digest.DigestFormatterService;
import de.hamburg.university.service.digest.DigestResultResponseDTO;
import de.hamburg.university.service.digest.DigestToolResultDTO;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
@Priority(1)
@Alternative
public class TestDigestApiClientService extends DigestApiClientService {

    @Inject
    DigestFormatterService digestFormatterService;

    public Uni<DigestToolResultDTO> callSubnetwork(List<String> target) {
        return Uni.createFrom().item(testCloseness("subnet", target));
    }

    public Uni<DigestToolResultDTO> callSet(List<String> target) {
        return Uni.createFrom().item(testCloseness("set", target));
    }


    private DigestToolResultDTO testCloseness(String folderName, List<String> target) {
        File folder = new File(Thread.currentThread().getContextClassLoader().getResource("tools/digest/" + folderName).getPath());
        File[] arrayOfFiles = folder.listFiles();
        if (arrayOfFiles == null) {
            return null;
        }
        return Arrays.stream(arrayOfFiles)
                .map(File::getName)
                .filter(s -> s.endsWith(".json") && !s.equals("questions.json"))
                .map(name -> JsonLoader.loadJson(getPath(folderName, name), new TypeReference<DigestResultResponseDTO>() {
                }))
                .filter(mock -> equalTarget(mock.getParameters().getTarget(), target))
                .findFirst()
                .map(resultMocked -> digestFormatterService.formatDigestOutputStructured(resultMocked.getResult(), resultMocked.getTask()))
                .orElse(null);
    }

    private String getPath(String folderName, String name){
        return "tools/digest/" + folderName + "/" + name;
    }

    public boolean equalTarget(List<String> mockedTargets, List<String> targets) {
        if (mockedTargets == null) {
            return false;
        }
        if (mockedTargets.size() != targets.size()) {
            return false;
        }
        for (String target : targets) {
            if (!mockedTargets.contains(target)) {
                return false;
            }
        }
        return true;
    }
}