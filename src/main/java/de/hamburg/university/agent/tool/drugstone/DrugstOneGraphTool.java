package de.hamburg.university.agent.tool.drugstone;


import de.hamburg.university.agent.bot.DrugstOneBot;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.helper.drugstone.DrugstOneManager;
import de.hamburg.university.helper.drugstone.dto.DrugstOneConfigDTO;
import de.hamburg.university.helper.drugstone.dto.DrugstOneGroupsConfigDTO;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DrugstOneGraphTool {

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    @Inject
    DrugstOneBot bot;

    @Inject
    DrugstOneManager drugstOneManager;

    @Tool(" Uses the ConfigBot to apply user instructions to the CURRENT Drugst.One config. " +
            "Only existing fields are modified; no new fields are invented. Unspecified " +
            "properties remain unchanged. The update is minimal and conservative.")
    public String updateConfig(@P(value = "query") String query, @ToolMemoryId String sessionId) {
        ToolDTO toolDTO = new ToolDTO(Tools.DRUGST_ONE.name());
        toolDTO.setInput(query);
        toolDTO.addContent("Updating Drugst.One config");
        chatWebsocketSender.sendTool(toolDTO, sessionId);
        DrugstOneConfigDTO current = drugstOneManager.getConfig(sessionId);
        DrugstOneConfigDTO updated = bot.updateConfig(current, query);
        drugstOneManager.setConfig(sessionId, updated);
        drugstOneManager.stopAndSend(toolDTO, sessionId);
        return "Updated Drugst.One config";
    }

    @Tool("""
            Uses the GroupsUpdaterBot to edit ONLY existing node/edge group configs.
            Groups are name-addressable: you can update style or visual settings of a
            known group, but not add/remove/rename groups. All other fields are preserved.
            """)
    public String updateGroups(@P(value = "query") String query, @ToolMemoryId String sessionId) {
        ToolDTO toolDTO = new ToolDTO(Tools.DRUGST_ONE.name());
        toolDTO.setInput(query);
        toolDTO.addContent("Updating Drugst.One Groups");
        chatWebsocketSender.sendTool(toolDTO, sessionId);
        DrugstOneGroupsConfigDTO current = drugstOneManager.getGroups(sessionId);
        DrugstOneGroupsConfigDTO updated = bot.updateGroups(current, query);
        drugstOneManager.setGroups(sessionId, updated);
        drugstOneManager.stopAndSend(toolDTO, sessionId);
        return "Updated Drugst.One groups";
    }

    @Tool("""
            Uses the GroupsCreatorBot to create new node/edge group configs.
            Groups are name-addressable: you can add new groups with a unique name.
            Existing groups are preserved and unchanged.
            """)
    public String createGroups(@P(value = "DRUGST_ONE") String query, @ToolMemoryId String sessionId) {
        ToolDTO toolDTO = new ToolDTO(Tools.DIGEST.name());
        toolDTO.setInput(query);
        toolDTO.addContent("Creating Drugst.One groups");
        chatWebsocketSender.sendTool(toolDTO, sessionId);
        DrugstOneGroupsConfigDTO current = drugstOneManager.getGroups(sessionId);
        DrugstOneGroupsConfigDTO updated = bot.updateGroups(current, query);
        drugstOneManager.setGroups(sessionId, updated);
        drugstOneManager.stopAndSend(toolDTO, sessionId);
        return "Updated Drugst.One groups";
    }
}
