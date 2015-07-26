package se.ivankrizsan.mule.jerseylogging.starter;

import org.mule.api.MuleContext;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextFactory;

/**
 * Starts an embedded instance of Mule running the Jersey Logging Example application.
 *
 * @author Ivan Krizsan
 */
public class JerseyLoggingExampleMuleStarter {
    /* Constant(s): */
    public final static String MULE_CONFIG_FILE = "mule-config.xml";

    public static void main(final String[] inArgs) throws Exception {
        final SpringXmlConfigurationBuilder theConfigBuilder = new SpringXmlConfigurationBuilder(MULE_CONFIG_FILE);
        final MuleContext theMuleContext = new DefaultMuleContextFactory().createMuleContext(theConfigBuilder);
        theMuleContext.start();
    }
}
