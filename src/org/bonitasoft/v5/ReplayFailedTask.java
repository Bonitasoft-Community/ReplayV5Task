package org.bonitasoft.v5;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.ow2.bonita.facade.CommandAPI;
// https://www.mvndoc.com/c/org.ow2.bonita/bonita-client/
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.ClientReplayCommand;

public class ReplayFailedTask {

    static Logger logger = Logger.getLogger(ReplayFailedTask.class.getName());
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    public void execute(QueryAPIAccessor queryAPIAccessor, CommandAPI commandAPI, EngineAPIUniq engineAPI) {

        QueryRuntimeAPI queryRuntime = queryAPIAccessor.getQueryRuntimeAPI();
        QueryDefinitionAPI queryDefinition = queryAPIAccessor.getQueryDefinitionAPI();

        Set<LightProcessDefinition> setLigthProcess = queryDefinition.getLightProcesses();

        int countProcess = 0;

        for (LightProcessDefinition processDefinition : setLigthProcess) {
            countProcess++;

            logger.info("Check process " + countProcess + "/" + setLigthProcess.size() + " [" + processDefinition.getUUID() + "]");
            try {

                Set<LightProcessDefinition> setProcess = new HashSet();
                setProcess.add(processDefinition);
                List<LightProcessInstance> listProcessInstances = engineAPI.getLightActiveParentProcessInstances(setProcess, 0, 10000);
                logger.info("  found " + listProcessInstances.size() + " cases, check them");

                int count = 0;
                for (LightProcessInstance processInstance : listProcessInstances) {
                    count++;
                    if (count % 500 == 0)
                        logger.info("Advancement: " + count + "/" + listProcessInstances.size());
                    // get all tasks in the processInstance
                    // Set<TaskInstance>  setTasks= queryRuntime.getTasks(processInstance.getProcessInstanceUUID());
                    Set<ActivityInstance> setOfActivities = queryRuntime.getActivityInstances(processInstance.getUUID());

                    // in V5, we have to reconnect time to time
                    engineAPI.manageConnection(false);

                    for (ActivityInstance activityInstance : setOfActivities) {
                        if (activityInstance.getState().equals(ActivityState.FAILED)) {
                            logger.info("Found Task Failed : [" + activityInstance.getUUID() + "]");

                            try {
                                ClientReplayCommand replayCommand = new ClientReplayCommand(activityInstance.getUUID());
                                commandAPI.execute(replayCommand);
                                logger.info("Task Re-executed : [" + activityInstance.getUUID() + "]");
                            } catch (Exception e) {
                                StringWriter sw = new StringWriter();
                                e.printStackTrace(new PrintWriter(sw));
                                String exceptionDetails = sw.toString();

                                logger.severe("Failed during execution of task: [" + activityInstance.getUUID() + "] : " + e.getMessage() + " at " + exceptionDetails);
                            }

                        }
                    }
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionDetails = sw.toString();

                logger.severe("Failed during Search task: " + e.getMessage() + " at " + exceptionDetails);

            }
        }
    }

}
