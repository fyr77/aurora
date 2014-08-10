package ru.game.aurora.world.generation.aliens.zorsan;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.player.research.BaseResearchWithFixedProgress;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.World;

/**
 * Can be dropped by a zorsan ship. Contains evil plans about attack on earth
 */
public class ZorsanWarData extends BaseGameObject {
    private static final long serialVersionUID = -149327875153345895L;

    private static final class ZorsanWarDataResearch extends BaseResearchWithFixedProgress {

        private static final long serialVersionUID = 9033482387575839066L;

        public ZorsanWarDataResearch() {
            super("zorsan_war_data", "probe_research", 50, 75);
            setReport(new ResearchReport(
                    "zorsan_dialog",
                    "zorsan_war_data.report"
            ));
        }

        @Override
        public void onCompleted(World world) {
            world.getGlobalVariables().put("zorsan.war_preparations", 0);
            world.getPlayer().getJournal().addQuestEntries("zorsan_relations", "war_data");
        }
    }

    public ZorsanWarData() {
        super(0, 0, (String) null);
    }

    @Override
    public void interact(World world) {
        world.getPlayer().getResearchState().addNewAvailableProject(new ZorsanWarDataResearch());
        ZorsanGenerator.removeWarDataDrop();
        world.addOverlayWindow(Dialog.loadFromFile("dialogs/zorsan/zorsan_war_data_collected.json"));
    }
}
