package de.hamburg.university.helper.drugstone;

import de.hamburg.university.helper.drugstone.dto.DrugstOneGroupsConfigColorDTO;
import de.hamburg.university.helper.drugstone.dto.DrugstOneGroupsConfigEdgeDTO;
import de.hamburg.university.helper.drugstone.dto.DrugstOneGroupsConfigFontDTO;
import de.hamburg.university.helper.drugstone.dto.DrugstOneGroupsConfigNodeDTO;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public final class DrugstOneNodeGroupDefaults {

    public static Map<String, DrugstOneGroupsConfigNodeDTO> defaultNodeGroups() {
        Map<String, DrugstOneGroupsConfigNodeDTO> map = new HashMap<>();

        map.put("default", new DrugstOneGroupsConfigNodeDTO(
                "Default Node Group",
                3,
                new DrugstOneGroupsConfigColorDTO(
                        "#FFFF00",
                        "#FFFF00",
                        new DrugstOneGroupsConfigColorDTO.Highlight("#FF0000", "#FF0000")
                ),
                "triangle",
                "default type",
                false,
                new DrugstOneGroupsConfigFontDTO(
                        "#000000",
                        14,
                        "arial",
                        0,
                        "#ffffff",
                        "center",
                        false,
                        false,
                        false,
                        false
                ),
                2
        ));

        map.put("foundnode", new DrugstOneGroupsConfigNodeDTO(
                "Found Nodes",
                null,
                new DrugstOneGroupsConfigColorDTO(
                        "#F12590",
                        "#F12590",
                        new DrugstOneGroupsConfigColorDTO.Highlight("#F12590", "#F12590")
                ),
                "circle",
                "default node type",
                null,
                null,
                null
        ));

        map.put("seednode", new DrugstOneGroupsConfigNodeDTO(
                "Seeds",
                3,
                new DrugstOneGroupsConfigColorDTO(
                        "#978117",
                        "#E4C326",
                        new DrugstOneGroupsConfigColorDTO.Highlight("#978117", "#E4C326")
                ),
                "star",
                "default seed node type",
                null,
                null,
                null
        ));

        map.put("diamondnode", new DrugstOneGroupsConfigNodeDTO(
                "Diamond Nodes",
                3,
                new DrugstOneGroupsConfigColorDTO(
                        "#13367A",
                        "#3F78C1",
                        new DrugstOneGroupsConfigColorDTO.Highlight("#13367A", "#3F78C1")
                ),
                "diamond",
                "default diamond node type",
                null,
                null,
                null
        ));

        // --- Drugs ---
        map.put("founddrug", new DrugstOneGroupsConfigNodeDTO(
                "Drugs",
                3,
                new DrugstOneGroupsConfigColorDTO(
                        "#9c195d",
                        "#F12590",
                        new DrugstOneGroupsConfigColorDTO.Highlight("#9c195d", "#F12590")
                ),
                "diamond",
                "default drug node type",
                null,
                null,
                null
        ));

        map.put("gene", new DrugstOneGroupsConfigNodeDTO(
                "Genes",
                3,
                new DrugstOneGroupsConfigColorDTO(
                        "#356920",
                        "#7EC16A",
                        new DrugstOneGroupsConfigColorDTO.Highlight("#356920", "#7EC16A")
                ),
                "circle",
                "default gene node type",
                null,
                null,
                null
        ));

        map.put("disorder", new DrugstOneGroupsConfigNodeDTO(
                "Disorders",
                3,
                new DrugstOneGroupsConfigColorDTO(
                        "#d18b19",
                        "#f1a223",
                        new DrugstOneGroupsConfigColorDTO.Highlight("#d18b19", "#f1a223")
                ),
                "triangle",
                "default gene node type",
                null,
                null,
                null
        ));

        return map;
    }

    public static DrugstOneGroupsConfigEdgeDTO defaultEdge = new DrugstOneGroupsConfigEdgeDTO("Default Edge Group", "black", false);


    public static DrugstOneGroupsConfigNodeDTO getByName(String name) {
        if (name == null) return defaultNodeGroup(null);
        return defaultNodeGroups().getOrDefault(name.toLowerCase(), defaultNodeGroup(name));
    }

    public static DrugstOneGroupsConfigNodeDTO defaultNodeGroup(String name) {
        if (name == null) {
            name = "default";
        }
        DrugstOneGroupsConfigNodeDTO d = defaultNodeGroups().get("default");
        d.setGroupName(name);
        return d;
    }


}