package com.perfectomobile.perfectomobilejenkins;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.util.ListBoxModel;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import com.perfectomobile.perfectomobilejenkins.connection.rest.RestServices;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import javax.servlet.ServletException;

import java.io.IOException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link PerfectoMobileBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class PerfectoMobileBuilder extends Builder {

    private final String name;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public PerfectoMobileBuilder(String name) {
        this.name = name;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // This is where you 'build' the project.

        // This also shows how you can consult the global configuration of the builder
    	ClientResponse perfectoResponse = null;
    			
    	try {
        	perfectoResponse = RestServices.getRepoScripts(getDescriptor().getUrl(), 
					getDescriptor().getAccessId(), 
					 Secret.toString(getDescriptor().getSecretKey()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        System.out.println(perfectoResponse.getStatus());
        System.out.println(perfectoResponse.getResponseDate());
        
        String textEntity = perfectoResponse.getEntity(String.class);
        listener.getLogger().println(textEntity);
        
        if( perfectoResponse.getStatus() == 200 ) {
            return true;
        }else{
            return false;
            	
        }      
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link PerfectoMobileBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/com/perfectomobile/perfectomobilejenkins/PerfectoMobileBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String logicalName;
        private String url;
        private String username;
        private Secret password;

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

		/**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }
        
        /**
         * Get clouds names
         * @return
         */
        public ListBoxModel doFillGoalTypeItems() {
            ListBoxModel items = new ListBoxModel();
            //for (BuildGoal goal : getBuildGoals()) {
                //items.add(goal.getDisplayName(), goal.getId());
            items.add(getName());
           //}
            return items;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Perfecto Mobile Build Step";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            logicalName=formData.getString("name");
            url = formData.getString("url");
            username = formData.getString("accessId");
            password = Secret.fromString(formData.getString("secretKey"));
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUrl)
            save();
            return super.configure(req,formData);
        }

        /**
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the control by the naming convention.
         */
        public String getName() {
            return logicalName;
        }
        
        public String getUrl() {
            return url;
        }
        public String getAccessId() {
            return username;
        }
        public Secret getSecretKey() {
            return password;
        }
        
        private static WebResource getService(final String url, final String user,
                final Secret password) {
        // setup REST-Client
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        client.addFilter( new HTTPBasicAuthFilter(user, Secret.toString( password ) ) ); 
        WebResource service = client.resource( url );
        return service;
}
        
        public FormValidation doTestConnection(
        		@QueryParameter("url") final String url,
        		@QueryParameter("accessId") final String accessId,
                @QueryParameter("secretKey") final String secretKey) throws IOException, ServletException {
            
        	// setup REST-Client
        	WebResource service = getService(url, accessId, Secret.fromString( secretKey ) );
            ClientResponse perfectoStatus = service.path("services").path("handsets").
        			queryParam("operation", "list").
        			queryParam("availableTo", accessId).
        			queryParam("user", accessId).
        			queryParam("password", secretKey).
        			queryParam("inUse", "false").
        			get(ClientResponse.class);
            if( perfectoStatus.getStatus() == 200 ) {
                    return FormValidation.ok("Success. Connection with perfecto mobile verified.");
            }
            return FormValidation.error("Failed. Please check the configuration. HTTP Status: " + perfectoStatus);
        }
    }
}

