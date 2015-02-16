import org.wso2.carbon.identity.sso.agent.exception.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentConfigs;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SampleContextEventListener implements ServletContextListener {

    private static Logger LOGGER = Logger.getLogger("InfoLogging");

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Properties properties = new Properties();
        try {
            if(servletContextEvent.getServletContext().getContextPath().contains("travelocity.com")) {
                properties.load(servletContextEvent.getServletContext().getClassLoader().getResourceAsStream("travelocity.properties"));
            } else if(servletContextEvent.getServletContext().getContextPath().contains("avis.com")) {
                properties.load(servletContextEvent.getServletContext().getClassLoader().getResourceAsStream("avis.properties"));
            }
            SSOAgentConfigs.initConfig(properties);
        } catch (IOException e){
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (SSOAgentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        SSOAgentConfigs.setKeyStoreStream(servletContextEvent.getServletContext().getClassLoader().getResourceAsStream("wso2carbon.jks"));
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
