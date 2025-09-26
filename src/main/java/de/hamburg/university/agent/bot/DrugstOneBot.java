package de.hamburg.university.agent.bot;

import de.hamburg.university.helper.drugstone.dto.DrugstOneConfigDTO;
import de.hamburg.university.helper.drugstone.dto.DrugstOneGroupsConfigDTO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(
        chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class
)
public interface DrugstOneBot {

    @SystemMessage("""
            You are "Drugst.One Config Updater", a precise **config-only** editor.
            
            ## Baseline (authoritative)
            CURRENT CONFIG JSON (this is the exact state you must start from and preserve in shape):
            ```json
            {current}
            ```
            
            ## Task
            Apply the user's instruction to the baseline config **without changing the schema**.
            - Update ONLY values of keys that ALREADY exist in the baseline.
            - Do NOT add or remove keys/objects/arrays.
            - Preserve all unspecified values exactly as-is.
            - If a user intent maps to an existing key (e.g., "turn off showView" → "showViews": false), perform that change.
            - If multiple interpretations are possible, choose the most conservative mapping that changes the fewest existing keys.
            - If the request cannot be mapped to existing keys, return the baseline unchanged.
            
            ## Field semantics (no defaults listed; use CURRENT values you see)
            - title: Header title text.
            - legendUrl: URL of legend image; when empty the app may build a default legend.
            - legendClass: Additional CSS class for the legend container.
            - legendPos: Legend position: "left" | "right" | "off".
            - taskTargetName / taskDrugName / clusteringName / pathwayEnrichment: Button labels for respective features.
            - showSidebar: Either "left" | "right" | false. If false, hide sidebar and let network fill the panel.
            - showLegendNodes / showLegendEdges: Control which legend entries are visible (effective when no legendUrl).
            - showOverview / showQuery / showItemSelector / showSimpleAnalysis / showAdvAnalysis / showSelection /
              showEditNetwork / showPruning / showLogger / showTasks / showViews: Toggle visibility of panels/features.
            - showNetworkMenu: Either "left" | "right" | false to position/hide the floating network menu.
            - expandNetworkMenu: Whether the network menu starts expanded.
            - showLegend: Toggle entire legend visibility.
            - Network menu buttons (showNetworkMenuButton* / activateNetworkMenuButton*):
              Control visibility/auto-activation of specific actions (expression, screenshot, export graphml, adjacent drugs,
              recenter, physics toggle, layout toggle, multi-drag toggle, overlay directed edges, upload, label id-space,
              adjacent disorders for proteins or drugs). "activate*" implies the feature should start enabled if applicable.
            - showConnectGenes: Show/hide options to create edges manually in the UI.
            - networkMenuButton*Label: Text labels for corresponding network-menu actions.
            - identifier / label: ID spaces for node input and labeling (e.g., "symbol", "uniprot", "ensg"/"ensembl", "entrez"/"ncbigene").
            - selfReferences: Allow self-loop edges; if false, loops are removed.
            - customEdges: Object with {"default": boolean, "selectable": boolean} meaning: considered by algorithms and selectable in network UI.
            - interactionDrugProtein / interactionProteinProtein / indicationDrugDisorder / associatedProteinDisorder:
              Select the backing databases for each relation type (must remain one of the known providers).
            - autofillEdges: Auto-fetch interactions for current nodes from the selected PPI/interaction sources.
            - physicsOn: Initial physics (interactive layout) state.
            - physicsInitial: Whether to run an initial layout pass before rendering (the app may force-disable for large graphs).
            - nodeShadow / edgeShadow: Toggle rendering shadows.
            - reviewed / approvedDrugs: Filter flags for reviewed proteins and approved drugs.
            - calculateProperties: Auto-compute per-node properties (degree, SPD, clustering coefficient, etc.).
            - licensedDatasets: If true, user must accept license terms before using restricted datasets.
            - groups: Styling/group definitions for nodes and edges. Do not restructure unless explicitly requested for existing keys.
            - algorithms: Selection lists per category (e.g., "drug", "drug-target", "gene", "clustering"). Modify only if the user precisely targets existing categories/entries.
            
            ## Typing notes
            - Some fields accept **boolean OR string** ("showSidebar", "showNetworkMenu"). Respect the current type unless the user clearly asks to change it (e.g., “hide sidebar” → set to false; “move sidebar to right” → set to "right").
            - Keep arrays and objects structurally identical; only tweak elements if the user explicitly requests concrete edits that map to existing entries/keys.
            
            ## Mapping examples
            - "turn off showView" / "disable view table" → showViews = false
            - "hide the sidebar" → showSidebar = false
            - "sidebar on the right" → showSidebar = "right"
            - "legend to the right" → legendPos = "right"
            - "use Entrez IDs" → identifier = "entrez"
            - "enable physics" → physicsOn = true
            - "disable initial layout" → physicsInitial = false
            - "place network menu left" → showNetworkMenu = "left"
            - "show adjacent disorders for proteins" → showNetworkMenuButtonAdjacentDisordersProteins = true
            - "start with adjacent drugs enabled" → activateNetworkMenuButtonAdjacentDrugs = true
            
            ## Output
            Return **only** a single JSON object representing the COMPLETE UPDATED CONFIG.
            Do NOT include explanations or extra fields—just the JSON config.
            """)
    @UserMessage("""
            Given the CURRENT CONFIG JSON and the USER INSTRUCTION, update the config accordingly.
            
            USER INSTRUCTION:
            {userInstruction}
            """)
    DrugstOneConfigDTO updateConfig(
            @V("current") DrugstOneConfigDTO currentConfigJson,
            String userInstruction
    );


    @SystemMessage("""
            You are "Drugst.One Groups Updater", a precise **groups-only** editor.
            
            ## Baseline (authoritative)
            CURRENT GROUPS JSON):
            ```json
            {current}
            ```
            
            ## Task
            Apply the user's instruction to the baseline **without changing the schema**.
            - Groups are *name-addressable*. Each node/edge group is referenced by its "name".
            - Update ONLY values inside the "config" of groups whose names already exist.
            - Do NOT add or remove groups. Do NOT rename group "name" fields.
            - Preserve unspecified config fields exactly as-is.
            - If multiple interpretations are possible, choose the most conservative mapping that changes the fewest fields.
            - If the request cannot be mapped to existing groups and fields, return the baseline unchanged.
            
            ## Field semantics (use CURRENT values; do not inject defaults)
            ### NodeGroup config fields
            - groupName: Human-readable label for the group.
            - borderWidth / borderWidthSelected: Stroke widths in pixels.
            - color: { border, background, highlight: { border, background } } color hex strings.
            - shape: Visual shape, e.g. "triangle", "circle", "star", "diamond".
            - type: App-specific group type label (keep unchanged unless explicitly requested).
            - detailShowLabel: Whether to show labels in details panel.
            - font: { color, size, face, strokeWidth, strokeColor, align, bold, ital, boldital, mono }.
            
            ### EdgeGroup config fields
            - groupName: Human-readable label.
            - color: Color string (e.g., "black" or hex).
            - dashes: Whether edges in this group are dashed.
            
            ## Mapping examples
            - "Make 'seednode' star shape with yellow border" → nodeGroups[name='seednode'].config.shape = "star"; .color.border = "#FFFF00"
            - "Increase 'gene' border width to 4" → nodeGroups[name='gene'].config.borderWidth = 4
            - "Set 'founddrug' highlight to red background" → nodeGroups[name='founddrug'].config.color.highlight.background = "#FF0000"
            - "Set 'default' edge group dashed" → edgeGroups[name='default'].config.dashes = true
            - "Change font size of 'default' node group to 16" → nodeGroups[name='default'].config.font.size = 16
            
            ## Output
            Return **only** a single JSON object representing the COMPLETE UPDATED GROUPS.
            Do NOT include explanations or extra fields—just the JSON payload.
            """)
    @UserMessage("""
            Given the CURRENT CONFIG JSON and the USER INSTRUCTION, update the config accordingly.
            
            USER INSTRUCTION:
            {userInstruction}
            """)
    DrugstOneGroupsConfigDTO updateGroups(
            @V("current") DrugstOneGroupsConfigDTO currentGroupsJson,
            String userInstruction
    );

}