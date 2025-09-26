package de.hamburg.university.helper.drugstone.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugstOneConfigDTO {

    /**
     * The title which will be used in the header of the Drugst.One.
     */
    private String title = "Drugst.One";

    /**
     * Optional backend base URL (left empty by default).
     */
    private String backendUrl = "";

    /**
     * URL to an image (png, jpg) for a legend. If empty, a default legend will be created.
     */
    private String legendUrl = "";

    /**
     * Additional class to append to the legend.
     */
    private String legendClass = "legend";

    /**
     * Position of the legend: 'left' | 'right' | 'off'.
     */
    private String legendPos = "left";

    /**
     * Label of the button for the target identification.
     */
    private String taskTargetName = "Target identification";

    /**
     * Label of the button for the drug search.
     */
    private String taskDrugName = "Drug search";

    /**
     * Label of the button for the clustering.
     */
    private String clusteringName = "Clustering";

    /**
     * Label of the button for the pathway enrichment.
     */
    private String pathwayEnrichment = "Pathway enrichment";

    /**
     * Position of the sidebar with the algorithms, node details, etc.
     * Either 'left' | 'right' | false. Using Object to allow boolean|string.
     */
    private Object showSidebar = "left";

    /**
     * Only if legendUrl is not given. Controls visibility of node entries in legend.
     */
    private boolean showLegendNodes = true;

    /**
     * Only if legendUrl is not given. Controls visibility of edge entries in legend.
     */
    private boolean showLegendEdges = true;

    /**
     * Show or hide the overview panel.
     */
    private boolean showOverview = true;

    /**
     * Show or hide the query panel.
     */
    private boolean showQuery = true;

    /**
     * Show or hide the node detail panel.
     */
    private boolean showItemSelector = true;

    /**
     * Show or hide the panel with quick analysis buttons.
     */
    private boolean showSimpleAnalysis = false;

    /**
     * Show or hide the panel with advanced analysis buttons.
     */
    private boolean showAdvAnalysis = true;

    /**
     * Controls which advanced analysis buttons are visible.
     */
    private List<String> showAdvAnalysisContent = Arrays.asList("drug-search", "drug-target-search", "pathway-enrichment", "clustering", "enrichment-gprofiler", "enrichment-digest", "search-ndex");

    /**
     * Show or hide the panel with the selection table.
     */
    private boolean showSelection = true;

    /**
     * Show or hide the panel for adding and deleting nodes.
     */
    private boolean showEditNetwork = false;

    /**
     * Show or hide the panel for pruning based on properties.
     */
    private boolean showPruning = false;

    /**
     * Show or hide the logger below the network window.
     */
    private boolean showLogger = false;

    /**
     * Show or hide the panel with the task table.
     */
    private boolean showTasks = true;

    /**
     * Show or hide the panel with the view table.
     */
    private boolean showViews = true;

    /**
     * Show or hide the network menu. The network menu can be positioned:
     * 'left' | 'right' | false. Using Object to allow boolean|string.
     */
    private Object showNetworkMenu = "right";

    /**
     * Indicates whether the network menu starts open or closed.
     */
    private boolean expandNetworkMenu = true;

    /**
     * Show or hide the complete legend.
     */
    private boolean showLegend = true;

    /**
     * Show or hide the button to load expression data in the network menu.
     */
    private boolean showNetworkMenuButtonExpression = true;

    /**
     * Show or hide the screenshot button in the network menu.
     */
    private boolean showNetworkMenuButtonScreenshot = true;

    /**
     * Show or hide the GraphML export button.
     */
    private boolean showNetworkMenuButtonExportGraphml = true;

    /**
     * Show or hide the first-neighbor drugs button.
     */
    private boolean showNetworkMenuButtonAdjacentDrugs = true;

    /**
     * Activates adjacent drugs by default once proteins or genes are loaded.
     */
    private boolean activateNetworkMenuButtonAdjacentDrugs = false;

    /**
     * Show or hide the re-center button.
     */
    private boolean showNetworkMenuButtonCenter = true;

    /**
     * Show or hide the toggle for network physics.
     */
    private boolean showNetworkMenuButtonAnimation = true;

    /**
     * Show or hide the toggle for cellular component layout.
     */
    private boolean showNetworkMenuButtonLayout = true;

    /**
     * Show or hide the toggle for selection multi-drag.
     */
    private boolean showNetworkMenuButtonSelectionMultiDrag = true;

    /**
     * Show or hide the toggle for overlay of directed edges (OmniPath).
     */
    private boolean showNetworkMenuButtonOverlayDirectedEdges = true;

    /**
     * Show or hide the upload button (.csv, .sif, .gt, .graphml).
     */
    private boolean showNetworkMenuButtonUpload = false;

    /**
     * Show or hide the button to choose the ID space for labels.
     */
    private boolean showNetworkMenuButtonLabelIdspace = true;

    /**
     * Show or hide the first-neighbor disorders (proteins) button.
     */
    private boolean showNetworkMenuButtonAdjacentDisordersProteins = true;

    /**
     * Activates adjacent disorders by default once proteins or genes are loaded.
     */
    private boolean activateNetworkMenuButtonAdjacentDisorders = false;

    /**
     * Show or hide the first-neighbor disorders (drugs) button.
     */
    private boolean showNetworkMenuButtonAdjacentDisordersDrugs = true;

    /**
     * Activates adjacent disorders to drugs by default if drugs are loaded.
     */
    private boolean activateNetworkMenuButtonAdjacentDisorderDrugs = false;

    /**
     * Show or hide options to connect nodes in the sidebar.
     */
    private boolean showConnectGenes = true;

    /**
     * Label for the first-neighbor drugs button (empty = default).
     */
    private String networkMenuButtonAdjacentDrugsLabel = "";

    /**
     * Label for the first-neighbor disorders (protein) button (empty = default).
     */
    private String networkMenuButtonAdjacentDisordersProteinsLabel = "";

    /**
     * Label for the first-neighbor disorders (drug) button (empty = default).
     */
    private String networkMenuButtonAdjacentDisordersDrugsLabel = "";

    /**
     * Label for the physics toggle button.
     */
    private String networkMenuButtonAnimationLabel = "Animation";

    /**
     * Label for the layout toggle button.
     */
    private String networkMenuButtonLayoutLabel = "Layout";

    /**
     * Label for the multi-drag toggle button.
     */
    private String networkMenuButtonSelectionMultiDragLabel = "Multi-Drag";

    /**
     * Label for the overlay directions toggle button.
     */
    private String networkMenuButtonOverlayDirectedEdgesLabel = "Overlay Directions";

    /**
     * Label for the upload button.
     */
    private String networkMenuButtonUploadLabel = "Upload";

    /**
     * The identifier type of given node IDs:
     * 'symbol' | 'uniprot' | 'ensg' | 'ensembl' | 'entrez' | 'ncbigene'.
     */
    private String identifier = "symbol";

    /**
     * The identifier type used for labels:
     * 'symbol' | 'uniprot' | 'ensg' | 'ensembl' | 'entrez' | 'ncbigene'.
     */
    private String label = "symbol";

    /**
     * Allow self-referencing edges ('loops'). If false, loops are removed.
     */
    private boolean selfReferences = false;

    /**
     * Options for user-input edges: considered in analysis and/or selectable.
     */
    private CustomEdges customEdges = new CustomEdges(true, true);

    /**
     * Drug DB used to fetch drug–protein interactions.
     */
    private String interactionDrugProtein = "NeDRex";

    /**
     * DB used to fetch protein–protein interactions.
     */
    private String interactionProteinProtein = "NeDRex";

    /**
     * DB used to fetch drug–disorder interactions.
     */
    private String indicationDrugDisorder = "NeDRex";

    /**
     * DB used to fetch protein–disorder interactions.
     */
    private String associatedProteinDisorder = "NeDRex";

    /**
     * Automatically fetch interactions for all network nodes from the selected
     * protein interaction database.
     * <p>
     * NOTE: Target JSON shows this default as true.
     */
    private boolean autofillEdges = true;

    /**
     * Sets initial state of the network interaction physics.
     */
    private boolean physicsOn = false;

    /**
     * Whether to run initial layouting before visualizing.
     * Always forced off for big networks by the component.
     * <p>
     * Also accepts legacy JSON key "physicsInital".
     */
    @JsonAlias("physicsInital")
    private boolean physicsInitial = true;

    /**
     * Turn shadows of network nodes on/off.
     */
    private boolean nodeShadow = true;

    /**
     * Turn shadows of network edges on/off.
     */
    private boolean edgeShadow = true;

    /**
     * Indicates whether licensed datasets should be used (requires user acceptance).
     */
    private boolean licensedDatasets = false;

    /**
     * Only consider reviewed proteins.
     */
    private boolean reviewed = false;

    /**
     * Only show approved adjacent drugs.
     */
    private boolean approvedDrugs = false;

    /**
     * Auto-calc node properties (degree, SPD, clustering coefficient, etc.).
     */
    private boolean calculateProperties = false;

    /**
     * Enable/disable cellular component layout initially.
     */
    private boolean layoutOn = false;

    /**
     * Enable/disable selection multi-drag initially.
     */
    private boolean selectionMultiDrag = true;

    /**
     * Start in fullscreen mode.
     */
    private boolean fullscreen = false;

    /**
     * Overlay directed edges (e.g., OmniPath) initially.
     */
    private boolean overlayDirectedEdges = false;

    /**
     * Groups ('nodeGroups' and 'edgeGroups') that define styles.
     * Use a raw map to allow “see 'Raw JSON'” structures.
     */
    private Map<String, Object> groups = new HashMap<>();

    /**
     * List of algorithms available to the user per category.
     * Example defaults match your target JSON.
     */
    private Map<String, List<String>> algorithms = defaultAlgorithms();


    public CustomEdges getCustomEdges() {
        return new CustomEdges(true, true);
    }

    private static Map<String, List<String>> defaultAlgorithms() {
        Map<String, List<String>> m = new HashMap<>();
        m.put("drug", Arrays.asList("trustrank", "closeness", "degree", "proximity"));
        m.put("drug-target", Arrays.asList("trustrank", "multisteiner", "keypathwayminer", "degree", "closeness", "betweenness", "first-neighbor"));
        m.put("gene", Arrays.asList("pathway-enrichment"));
        m.put("clustering", Arrays.asList("louvain-clustering", "leiden-clustering"));
        return m;
        // If you also expose g:Profiler/Digest under “gene”, keep them in showAdvAnalysisContent.
    }

    @Data
    public static class CustomEdges {
        /**
         * Whether custom edges should be considered by analysis algorithms.
         * Field name differs from JSON key; mapped with @JsonProperty.
         */
        @JsonProperty("default")
        private boolean defaultValue;

        /**
         * Whether custom edges should be selectable in the network.
         */
        private boolean selectable;

        public CustomEdges() {
        }

        public CustomEdges(boolean defaultValue, boolean selectable) {
            this.defaultValue = defaultValue;
            this.selectable = selectable;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DrugstOneConfigDTO that = (DrugstOneConfigDTO) o;
        return isShowLegendNodes() == that.isShowLegendNodes() &&
                isShowLegendEdges() == that.isShowLegendEdges() &&
                isShowOverview() == that.isShowOverview() &&
                isShowQuery() == that.isShowQuery() &&
                isShowItemSelector() == that.isShowItemSelector() &&
                isShowSimpleAnalysis() == that.isShowSimpleAnalysis() &&
                isShowAdvAnalysis() == that.isShowAdvAnalysis() &&
                isShowSelection() == that.isShowSelection() &&
                isShowEditNetwork() == that.isShowEditNetwork() && isShowPruning() == that.isShowPruning() && isShowLogger() == that.isShowLogger() && isShowTasks() == that.isShowTasks() && isShowViews() == that.isShowViews() && isExpandNetworkMenu() == that.isExpandNetworkMenu() &&
                isShowLegend() == that.isShowLegend() && isShowNetworkMenuButtonExpression() == that.isShowNetworkMenuButtonExpression() && isShowNetworkMenuButtonScreenshot() == that.isShowNetworkMenuButtonScreenshot() && isShowNetworkMenuButtonExportGraphml() == that.isShowNetworkMenuButtonExportGraphml() && isShowNetworkMenuButtonAdjacentDrugs() == that.isShowNetworkMenuButtonAdjacentDrugs() && isActivateNetworkMenuButtonAdjacentDrugs() == that.isActivateNetworkMenuButtonAdjacentDrugs() && isShowNetworkMenuButtonCenter() == that.isShowNetworkMenuButtonCenter() && isShowNetworkMenuButtonAnimation() == that.isShowNetworkMenuButtonAnimation() && isShowNetworkMenuButtonLayout() == that.isShowNetworkMenuButtonLayout() && isShowNetworkMenuButtonSelectionMultiDrag() == that.isShowNetworkMenuButtonSelectionMultiDrag() && isShowNetworkMenuButtonOverlayDirectedEdges() == that.isShowNetworkMenuButtonOverlayDirectedEdges() && isShowNetworkMenuButtonUpload() == that.isShowNetworkMenuButtonUpload() && isShowNetworkMenuButtonLabelIdspace() == that.isShowNetworkMenuButtonLabelIdspace() && isShowNetworkMenuButtonAdjacentDisordersProteins() == that.isShowNetworkMenuButtonAdjacentDisordersProteins() && isActivateNetworkMenuButtonAdjacentDisorders() == that.isActivateNetworkMenuButtonAdjacentDisorders() && isShowNetworkMenuButtonAdjacentDisordersDrugs() == that.isShowNetworkMenuButtonAdjacentDisordersDrugs() && isActivateNetworkMenuButtonAdjacentDisorderDrugs() == that.isActivateNetworkMenuButtonAdjacentDisorderDrugs() && isShowConnectGenes() == that.isShowConnectGenes() && isSelfReferences() == that.isSelfReferences() && isAutofillEdges() == that.isAutofillEdges() && isPhysicsOn() == that.isPhysicsOn() && isPhysicsInitial() == that.isPhysicsInitial() && isNodeShadow() == that.isNodeShadow() && isEdgeShadow() == that.isEdgeShadow() && isLicensedDatasets() == that.isLicensedDatasets() && isReviewed() == that.isReviewed() && isApprovedDrugs() == that.isApprovedDrugs() && isCalculateProperties() == that.isCalculateProperties() && isLayoutOn() == that.isLayoutOn() && isSelectionMultiDrag() == that.isSelectionMultiDrag() && isFullscreen() == that.isFullscreen() && isOverlayDirectedEdges() == that.isOverlayDirectedEdges() && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getBackendUrl(), that.getBackendUrl()) && Objects.equals(getLegendUrl(), that.getLegendUrl()) && Objects.equals(getLegendClass(), that.getLegendClass()) && Objects.equals(getLegendPos(), that.getLegendPos()) && Objects.equals(getTaskTargetName(), that.getTaskTargetName()) && Objects.equals(getTaskDrugName(), that.getTaskDrugName()) && Objects.equals(getClusteringName(), that.getClusteringName()) && Objects.equals(getPathwayEnrichment(), that.getPathwayEnrichment()) && Objects.equals(getShowSidebar(), that.getShowSidebar()) && Objects.equals(getShowAdvAnalysisContent(), that.getShowAdvAnalysisContent()) && Objects.equals(getShowNetworkMenu(), that.getShowNetworkMenu()) && Objects.equals(getNetworkMenuButtonAdjacentDrugsLabel(), that.getNetworkMenuButtonAdjacentDrugsLabel()) && Objects.equals(getNetworkMenuButtonAdjacentDisordersProteinsLabel(), that.getNetworkMenuButtonAdjacentDisordersProteinsLabel()) && Objects.equals(getNetworkMenuButtonAdjacentDisordersDrugsLabel(), that.getNetworkMenuButtonAdjacentDisordersDrugsLabel()) && Objects.equals(getNetworkMenuButtonAnimationLabel(), that.getNetworkMenuButtonAnimationLabel()) && Objects.equals(getNetworkMenuButtonLayoutLabel(), that.getNetworkMenuButtonLayoutLabel()) && Objects.equals(getNetworkMenuButtonSelectionMultiDragLabel(), that.getNetworkMenuButtonSelectionMultiDragLabel()) && Objects.equals(getNetworkMenuButtonOverlayDirectedEdgesLabel(), that.getNetworkMenuButtonOverlayDirectedEdgesLabel()) && Objects.equals(getNetworkMenuButtonUploadLabel(), that.getNetworkMenuButtonUploadLabel()) && Objects.equals(getIdentifier(), that.getIdentifier()) && Objects.equals(getLabel(), that.getLabel()) && Objects.equals(getInteractionDrugProtein(), that.getInteractionDrugProtein()) && Objects.equals(getInteractionProteinProtein(), that.getInteractionProteinProtein()) && Objects.equals(getIndicationDrugDisorder(), that.getIndicationDrugDisorder()) && Objects.equals(getAssociatedProteinDisorder(), that.getAssociatedProteinDisorder()) && Objects.equals(getGroups(), that.getGroups()) && Objects.equals(getAlgorithms(), that.getAlgorithms());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getBackendUrl(), getLegendUrl(), getLegendClass(), getLegendPos(), getTaskTargetName(), getTaskDrugName(), getClusteringName(), getPathwayEnrichment(), getShowSidebar(), isShowLegendNodes(), isShowLegendEdges(), isShowOverview(), isShowQuery(), isShowItemSelector(), isShowSimpleAnalysis(), isShowAdvAnalysis(), getShowAdvAnalysisContent(), isShowSelection(), isShowEditNetwork(), isShowPruning(), isShowLogger(), isShowTasks(), isShowViews(), getShowNetworkMenu(), isExpandNetworkMenu(), isShowLegend(), isShowNetworkMenuButtonExpression(), isShowNetworkMenuButtonScreenshot(), isShowNetworkMenuButtonExportGraphml(), isShowNetworkMenuButtonAdjacentDrugs(), isActivateNetworkMenuButtonAdjacentDrugs(), isShowNetworkMenuButtonCenter(), isShowNetworkMenuButtonAnimation(), isShowNetworkMenuButtonLayout(), isShowNetworkMenuButtonSelectionMultiDrag(), isShowNetworkMenuButtonOverlayDirectedEdges(), isShowNetworkMenuButtonUpload(), isShowNetworkMenuButtonLabelIdspace(), isShowNetworkMenuButtonAdjacentDisordersProteins(), isActivateNetworkMenuButtonAdjacentDisorders(), isShowNetworkMenuButtonAdjacentDisordersDrugs(), isActivateNetworkMenuButtonAdjacentDisorderDrugs(), isShowConnectGenes(), getNetworkMenuButtonAdjacentDrugsLabel(), getNetworkMenuButtonAdjacentDisordersProteinsLabel(), getNetworkMenuButtonAdjacentDisordersDrugsLabel(), getNetworkMenuButtonAnimationLabel(), getNetworkMenuButtonLayoutLabel(), getNetworkMenuButtonSelectionMultiDragLabel(), getNetworkMenuButtonOverlayDirectedEdgesLabel(), getNetworkMenuButtonUploadLabel(), getIdentifier(), getLabel(), isSelfReferences(), getInteractionDrugProtein(), getInteractionProteinProtein(), getIndicationDrugDisorder(), getAssociatedProteinDisorder(), isAutofillEdges(), isPhysicsOn(), isPhysicsInitial(), isNodeShadow(), isEdgeShadow(), isLicensedDatasets(), isReviewed(), isApprovedDrugs(), isCalculateProperties(), isLayoutOn(), isSelectionMultiDrag(), isFullscreen(), isOverlayDirectedEdges(), getGroups(), getAlgorithms());
    }

    @Override
    public String toString() {
        return "DrugstOneConfigDTO{" +
                "title='" + title + '\'' +
                ", backendUrl='" + backendUrl + '\'' +
                ", legendUrl='" + legendUrl + '\'' +
                ", legendClass='" + legendClass + '\'' +
                ", legendPos='" + legendPos + '\'' +
                ", taskTargetName='" + taskTargetName + '\'' +
                ", taskDrugName='" + taskDrugName + '\'' +
                ", clusteringName='" + clusteringName + '\'' +
                ", pathwayEnrichment='" + pathwayEnrichment + '\'' +
                ", showSidebar=" + showSidebar +
                ", showLegendNodes=" + showLegendNodes +
                ", showLegendEdges=" + showLegendEdges +
                ", showOverview=" + showOverview +
                ", showQuery=" + showQuery +
                ", showItemSelector=" + showItemSelector +
                ", showSimpleAnalysis=" + showSimpleAnalysis +
                ", showAdvAnalysis=" + showAdvAnalysis +
                ", showAdvAnalysisContent=" + showAdvAnalysisContent +
                ", showSelection=" + showSelection +
                ", showEditNetwork=" + showEditNetwork +
                ", showPruning=" + showPruning +
                ", showLogger=" + showLogger +
                ", showTasks=" + showTasks +
                ", showViews=" + showViews +
                ", showNetworkMenu=" + showNetworkMenu +
                ", expandNetworkMenu=" + expandNetworkMenu +
                ", showLegend=" + showLegend +
                ", showNetworkMenuButtonExpression=" + showNetworkMenuButtonExpression +
                ", showNetworkMenuButtonScreenshot=" + showNetworkMenuButtonScreenshot +
                ", showNetworkMenuButtonExportGraphml=" + showNetworkMenuButtonExportGraphml +
                ", showNetworkMenuButtonAdjacentDrugs=" + showNetworkMenuButtonAdjacentDrugs +
                ", activateNetworkMenuButtonAdjacentDrugs=" + activateNetworkMenuButtonAdjacentDrugs +
                ", showNetworkMenuButtonCenter=" + showNetworkMenuButtonCenter +
                ", showNetworkMenuButtonAnimation=" + showNetworkMenuButtonAnimation +
                ", showNetworkMenuButtonLayout=" + showNetworkMenuButtonLayout +
                ", showNetworkMenuButtonSelectionMultiDrag=" + showNetworkMenuButtonSelectionMultiDrag +
                ", showNetworkMenuButtonOverlayDirectedEdges=" + showNetworkMenuButtonOverlayDirectedEdges +
                ", showNetworkMenuButtonUpload=" + showNetworkMenuButtonUpload +
                ", showNetworkMenuButtonLabelIdspace=" + showNetworkMenuButtonLabelIdspace +
                ", showNetworkMenuButtonAdjacentDisordersProteins=" + showNetworkMenuButtonAdjacentDisordersProteins +
                ", activateNetworkMenuButtonAdjacentDisorders=" + activateNetworkMenuButtonAdjacentDisorders +
                ", showNetworkMenuButtonAdjacentDisordersDrugs=" + showNetworkMenuButtonAdjacentDisordersDrugs +
                ", activateNetworkMenuButtonAdjacentDisorderDrugs=" + activateNetworkMenuButtonAdjacentDisorderDrugs +
                ", showConnectGenes=" + showConnectGenes +
                ", networkMenuButtonAdjacentDrugsLabel='" + networkMenuButtonAdjacentDrugsLabel + '\'' +
                ", networkMenuButtonAdjacentDisordersProteinsLabel='" + networkMenuButtonAdjacentDisordersProteinsLabel + '\'' +
                ", networkMenuButtonAdjacentDisordersDrugsLabel='" + networkMenuButtonAdjacentDisordersDrugsLabel + '\'' +
                ", networkMenuButtonAnimationLabel='" + networkMenuButtonAnimationLabel + '\'' +
                ", networkMenuButtonLayoutLabel='" + networkMenuButtonLayoutLabel + '\'' +
                ", networkMenuButtonSelectionMultiDragLabel='" + networkMenuButtonSelectionMultiDragLabel + '\'' +
                ", networkMenuButtonOverlayDirectedEdgesLabel='" + networkMenuButtonOverlayDirectedEdgesLabel + '\'' +
                ", networkMenuButtonUploadLabel='" + networkMenuButtonUploadLabel + '\'' +
                ", identifier='" + identifier + '\'' +
                ", label='" + label + '\'' +
                ", selfReferences=" + selfReferences +
                ", customEdges=" + customEdges +
                ", interactionDrugProtein='" + interactionDrugProtein + '\'' +
                ", interactionProteinProtein='" + interactionProteinProtein + '\'' +
                ", indicationDrugDisorder='" + indicationDrugDisorder + '\'' +
                ", associatedProteinDisorder='" + associatedProteinDisorder + '\'' +
                ", autofillEdges=" + autofillEdges +
                ", physicsOn=" + physicsOn +
                ", physicsInitial=" + physicsInitial +
                ", nodeShadow=" + nodeShadow +
                ", edgeShadow=" + edgeShadow +
                ", licensedDatasets=" + licensedDatasets +
                ", reviewed=" + reviewed +
                ", approvedDrugs=" + approvedDrugs +
                ", calculateProperties=" + calculateProperties +
                ", layoutOn=" + layoutOn +
                ", selectionMultiDrag=" + selectionMultiDrag +
                ", fullscreen=" + fullscreen +
                ", overlayDirectedEdges=" + overlayDirectedEdges +
                ", groups=" + groups +
                ", algorithms=" + algorithms +
                '}';
    }
}
