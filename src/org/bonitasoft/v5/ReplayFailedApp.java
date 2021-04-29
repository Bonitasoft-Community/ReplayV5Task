package org.bonitasoft.v5;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.security.auth.login.LoginContext;

import org.ow2.bonita.facade.CommandAPI;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.SimpleCallbackHandler;

public class ReplayFailedApp {

    static Logger logger = Logger.getLogger(ReplayFailedApp.class.getName());
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    private static String header = " -------------------------------------------------------------------------- ";

    /**
     * Decode the different args
     */
    private static class DecodeArg {

        String[] args;
        int indexArgs = 0;

        public DecodeArg(final String[] args) {
            this.args = args;
            this.indexArgs = 0;
        }

        public String nextArg() {
            if (indexArgs >= args.length)
                return null;
            indexArgs++;
            return args[indexArgs - 1];
        }

        public boolean isNextArgsAndOption() {
            if (indexArgs >= args.length)
                return false;
            if (args[indexArgs].startsWith("-"))
                return true;
            return false;

        }
    }

    /**
     * Decode the different args
     * admin bpm "D:/tomcat_h2/bonita" "D:/tomcat/jaas-standard.cfg"
     */

    public static void main(final String[] args) {


        EngineAPIUniq engineAPI = new EngineAPIUniq();

        DecodeArg decodeArgs = new DecodeArg(args);

        while (decodeArgs.isNextArgsAndOption()) {
            String option = decodeArgs.nextArg();
        }

        engineAPI.loginName = decodeArgs.nextArg();
        engineAPI.loginPassword = decodeArgs.nextArg();
        engineAPI.bonitaHome = decodeArgs.nextArg();
        engineAPI.jassFilePath = decodeArgs.nextArg();
        engineAPI.restServerAddress = decodeArgs.nextArg();
        if (engineAPI.restServerAddress == null)
            engineAPI.restServerAddress = "http://localhost:8080/bonita-server-rest";

        logger.info("loginName         = [" + engineAPI.loginName + "]");
        logger.info("loginPassword     = [" + engineAPI.loginPassword + "]");
        logger.info("BonitaHome        = [" + engineAPI.bonitaHome + "]");
        logger.info("jassFilePath      = [" + engineAPI.jassFilePath + "]");
        logger.info("RestServerAddress = [" + engineAPI.restServerAddress + "]");


        logger.info("java.version = [" + System.getProperty("java.version") + "]");
        logger.info("RetryFailedTask V1.0.0 (Apr 29 2021)");

        // check the environement
        try {
            File fileBonitaHome = new File(engineAPI.bonitaHome);
            if (!fileBonitaHome.isDirectory()) {
                logger.info("Bonita Home must be a directory");
                return;
            }
            File fileJaas = new File(engineAPI.jassFilePath);
            if (!fileJaas.isFile()) {
                logger.info("jassFilePath must be a file");
                return;
            }
        } catch (Exception e) {
            logger.severe("Error " + e.getMessage());
            return;
        }
        System.setProperty(BonitaConstants.HOME, engineAPI.bonitaHome);
        // Set the JASS file
        System.setProperty(BonitaConstants.JAAS_PROPERTY, engineAPI.jassFilePath);

        // System.setProperty(BonitaConstants.API_TYPE_PROPERTY, org.ow2.bonita.facade.Context.REST.toString());
        System.setProperty(BonitaConstants.API_TYPE_PROPERTY, org.ow2.bonita.facade.Context.REST.toString());
        System.setProperty(BonitaConstants.REST_SERVER_ADDRESS_PROPERTY, engineAPI.restServerAddress);

        // System.setProperty(BonitaConstants.PROVIDER_URL_PROPERTY, "http://localhost:8080/bonita/");

 
        engineAPI.manageConnection(false);
        // Retrieve the QueryRuntimeAPI
        // APIAccessor apiAccessor = AccessorUtil.getAPIAccessor();

     
        // RuntimeAPI runtimeAPI = apiAccessor.getRuntimeAPI();
        QueryAPIAccessor queryAPIAccessor = AccessorUtil.getQueryAPIAccessor();
        CommandAPI commandAPI = AccessorUtil.getCommandAPI();
        
        
        engineAPI.initialise();

        ReplayFailedTask replayFailedTask = new ReplayFailedTask();
        
        replayFailedTask.execute( queryAPIAccessor, commandAPI ,engineAPI);

        // first, estimate the complete time need

    }

     
   
}
