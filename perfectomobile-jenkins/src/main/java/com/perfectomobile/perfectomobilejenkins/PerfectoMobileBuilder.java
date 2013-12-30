package com.perfectomobile.perfectomobilejenkins;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.util.ListBoxModel;
import hudson.model.AbstractBuild;
import hudson.model.AutoCompletionCandidates;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import com.perfectomobile.perfectomobilejenkins.connection.rest.RestServices;
import com.perfectomobile.perfectomobilejenkins.xml.XmlParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import javax.servlet.ServletException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
 * @author Guy Michaelis
 */
public class PerfectoMobileBuilder extends Builder {
	
	private final String name;
    private final String perfectoCloud;
    private final String autoScript;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public PerfectoMobileBuilder(String name, String perfectoCloud, String autoScript) {
        this.name = name;
        this.perfectoCloud = perfectoCloud;
        this.autoScript = autoScript;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return name;
    }
    
    public String getPerfectoCloud() {
        return perfectoCloud;
    }
    
    public String getAutoScript() {
        return autoScript;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

    	ClientResponse perfectoResponse = null;
    			
    	try {
        	perfectoResponse = RestServices.getInstance().getRepoScriptsItems(getDescriptor().getUrl(), 
					getDescriptor().getAccessId(), 
					 Secret.toString(getDescriptor().getSecretKey()),
					 autoScript);
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
         * Get clouds names. Might be more than one cloud in the future.
         * @return Cloud Items
         */
        public ListBoxModel doFillPerfectoCloudItems() {
            ListBoxModel items = new ListBoxModel();
            
            items.add(getLogicalName());
            return items;
        }
        
        public ListBoxModel doFillDeviceIdItems() {
            ListBoxModel items = new ListBoxModel();
            
            ClientResponse perfectoResponse = null;
            try {
				perfectoResponse = RestServices.getInstance().getHandsets(getUrl(), getAccessId(), 
						 Secret.toString(getSecretKey()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            File resultFile = perfectoResponse.getEntity(File.class);
            List<String> devices = XmlParser.getInstance().getXmlElements(resultFile, XmlParser.DEVICEID_ELEMENT_NAME);
            List<String> manufacturer = XmlParser.getInstance().getXmlElements(resultFile, XmlParser.MANUFACTURER_ELEMENT_NAME);
            List<String> location = XmlParser.getInstance().getXmlElements(resultFile, XmlParser.LOCATION_ELEMENT_NAME);
            List<String> model = XmlParser.getInstance().getXmlElements(resultFile, XmlParser.MODEL_ELEMENT_NAME);
            
            for (int i=0; i<devices.size();i++) {
            	StringBuffer itemDetails=new StringBuffer();
            	itemDetails.append(manufacturer.get(i)).append("-").append(model.get(i)).append("-").append(location.get(i)).append("-").append(devices.get(i));
            	items.add(itemDetails.toString());
            }

            return items;
        }
        
        
        public AutoCompletionCandidates doAutoCompleteAutoScript(@QueryParameter String value) {
            AutoCompletionCandidates c = new AutoCompletionCandidates();
            
          //Holds all Perfecto scripts
           String [] scripts = PMScripts.getInstance().getAllScripts(url, username, Secret.toString(password));
            
            for (String script : scripts)
                if (script.toLowerCase().startsWith(value.toLowerCase()))
                    c.add(script);
            return c;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.getName
         */
        public String getDisplayName() {
            return "Perfecto Mobile Build Step";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            logicalName=formData.getString("logicalName");
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
        public String getLogicalName() {
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
        
        /**
         * Check if an element exists in the response.
         * @param perfectoResponse
         * @param element
         * @return
         */
        private boolean isElementExists(ClientResponse perfectoResponse, String element){
   		 
	   		 boolean isExists=true;
	   		 
	   		 File resultFile = perfectoResponse.getEntity(File.class);
	   		 
	   		 String item = XmlParser.getInstance().getXmlFirstElement(resultFile, element);
	   		 
	   		 if (item==null || item.isEmpty())
	   			 isExists=false;
	   		 
	   		 return isExists;
   		 
   	 	}
        
        public FormValidation doTestConnection(
        		@QueryParameter("url") final String url,
        		@QueryParameter("accessId") final String accessId,
                @QueryParameter("secretKey") final String secretKey) throws IOException, ServletException {
            
        	// setup REST-Client
        	ClientResponse perfectoResponse = null;
        	
        	try {
            	perfectoResponse = RestServices.getInstance().getHandsets(url, accessId, secretKey);
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (ServletException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
        	if( perfectoResponse.getStatus() == 200) {
                    return FormValidation.ok("Success. Connection with perfecto mobile verified.");
            }
            return FormValidation.error("Failed. Please check the configuration. HTTP Status: " + perfectoResponse);
        }
        
    }
}

