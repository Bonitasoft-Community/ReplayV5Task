package org.bonitasoft.v5;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.security.auth.login.LoginContext;

import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.paging.ProcessInstanceCriterion;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.search.SearchQueryBuilder;
import org.ow2.bonita.search.index.ProcessInstanceIndex;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.SimpleCallbackHandler;

public class EngineAPIUniq {
  QueryAPIAccessor queryAPIAccessor;
  QueryRuntimeAPI queryRuntime;
  QueryDefinitionAPI queryDefinition;
  static Logger logger = Logger.getLogger(EngineAPIUniq.class.getName());

  public EngineAPIUniq()
  {
   
  }
  
  public void initialise()
  {
    queryAPIAccessor = AccessorUtil.getQueryAPIAccessor();
  queryRuntime = queryAPIAccessor.getQueryRuntimeAPI();
  queryDefinition = queryAPIAccessor.getQueryDefinitionAPI();
  }
  
  
  /* ******************************************************************************** */
  /*
   * manage connection
   * in V5, connectin has to be open and close regulary
   */
  /* ******************************************************************************** */

  private int countOperation = -1;
  LoginContext loginContext;
  String loginName;
  String loginPassword;
  String bonitaHome;
  String jassFilePath;
  String restServerAddress;

  public int stepReconnect = 8000;
  public int stepSleep = 20;

  
  
  /**
   * if errorServer is true, the call is after an error on the server. Wait longer then
   * 
   * @param errorServer
   */
  public void manageConnection(boolean errorServer) {
      countOperation++;
      if (countOperation % stepReconnect == 0) {

          try {
              logger.info(".... reconnect to the server to calm it");
              if (loginContext != null) {
                  loginContext.logout();
                  loginContext = null;
              }
              // Log in on the engine
              // loginContext = new LoginContext("BonitaAuth", new SimpleCallbackHandler(loginName, loginPassword));
              // loginContext.login();
              // loginContext.logout();
              // loginContext=null;
              loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(loginName, loginPassword));
              loginContext.login();

              // let's the server have a break
              if (countOperation > 0)
                  Thread.sleep(1000 * (stepSleep + (errorServer ? 120 : 0)));

          } catch (Exception e) {
              logger.severe("Connection " + e.getMessage());
              return;
          }

      }
  }
  
  
  public int getPageSize() {
    return 5000;
  }
  
  public void preload( List<LightProcessInstance> listProcessInstances)
  {
    
  }
  List<LightProcessInstance> getLightParentProcessInstances( Set<ProcessDefinitionUUID> processUUIDs, int fromIndex, int pageSize)
  {
    return   queryRuntime.getLightParentProcessInstances(processUUIDs, fromIndex, pageSize, ProcessInstanceCriterion.DEFAULT);

  }
  
  /**
   * search the active case
   * @param processUUIDs
   * @param fromIndex
   * @param pageSize
   * @return
   */
  List<LightProcessInstance> getLightActiveParentProcessInstances( Set<LightProcessDefinition> processUUIDs, int fromIndex, int pageSize)
  {
      SearchQueryBuilder searchQueryBuilder = new SearchQueryBuilder(new ProcessInstanceIndex());
      
      // We need 
      searchQueryBuilder.leftParenthesis();
      int i=0;
      for (LightProcessDefinition processUUID: processUUIDs)
      {
        if (i>0)
          searchQueryBuilder.or();
        i++;
        List<String> listCriteria = new ArrayList<String>();
        listCriteria.add( ProcessInstanceIndex.PROCESS_DEFINITION_UUID+"='"+processUUID.toString()+"'");
        searchQueryBuilder.criteria(listCriteria); 
      }
      searchQueryBuilder.rightParenthesis();
      List<String> listCriteria = new ArrayList<String>();
      listCriteria.add( ProcessInstanceIndex.ENDED_DATE+" !='null'");
      searchQueryBuilder.criteria(listCriteria); 

      // sear ProcessInstance
      List<LightProcessInstance> answer= queryRuntime.search( searchQueryBuilder, fromIndex, pageSize);
      
      return answer;
  }
  
  
  Set<ProcessInstanceUUID> getChildrenInstanceUUIDsOfProcessInstance(ProcessInstanceUUID processInstancreUUID) throws InstanceNotFoundException
  {
    return queryRuntime.getChildrenInstanceUUIDsOfProcessInstance(processInstancreUUID);
  }
  
  LightProcessInstance getLightProcessInstance( ProcessInstanceUUID processInstanceUID) throws InstanceNotFoundException
  {
    return queryRuntime.getLightProcessInstance(processInstanceUID);
  }
  
  List<LightActivityInstance> getLightActivityInstancesFromRoot( ProcessInstanceUUID processInstanceUID)
  {
    return queryRuntime.getLightActivityInstancesFromRoot(processInstanceUID);
  }
  
  List<LightTaskInstance> getLightTaskInstancesFromRoot( ProcessInstanceUUID processInstanceUID)
  {
  return  queryRuntime.getLightTaskInstancesFromRoot(processInstanceUID);
  }
  
  
  Map<String, Object> getActivityInstanceVariables( ActivityInstanceUUID activityInstanceUUID) throws ActivityNotFoundException
  {    
    return queryRuntime.getActivityInstanceVariables(activityInstanceUUID);
  }
  
  Map<String, Object> getProcessInstanceVariables(ProcessInstanceUUID processInstanceUID) throws InstanceNotFoundException
  {
    return queryRuntime.getProcessInstanceVariables( processInstanceUID );
  }
}
