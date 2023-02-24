package org.jboss.as.quickstarts.kitchensink.controller;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Model;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jboss.as.quickstarts.kitchensink.model.MemberLoginModel;
import org.jboss.as.quickstarts.kitchensink.service.MemberLogin;



@Model
public class MemberLoginController {
    @Inject
    private FacesContext facesContext;
    
    @Inject
    private MemberLogin memberLogin;
    
    @Produces
    @Named
    private MemberLoginModel existingMember;
    
    @PostConstruct
    public void initExistingMember() {
    	existingMember = new MemberLoginModel();
    }
    
	public void login() throws Exception {
		FacesMessage m;
        try {
        	if (memberLogin.login(existingMember) == true){
        		m = new FacesMessage(FacesMessage.SEVERITY_INFO, "Logged in!", "Login successful");
        	    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        	    externalContext.redirect(externalContext.getRequestContextPath() + "/rest/members");
        	}
        	else {
        		m = new FacesMessage(FacesMessage.SEVERITY_INFO, "Incorrect username/password", "Login unsuccessful");
        	}
            
			facesContext.addMessage(null, m);
            initExistingMember();
        } catch (Exception e) {
            String errorMessage = getRootErrorMessage(e);
            m = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to login!", "Login unsuccessful");
            facesContext.addMessage(null, m);
        }
    }
	
	private String getRootErrorMessage(Exception e) {
        // Default to general error message that registration failed.
        String errorMessage = "Registration failed. See server log for more information";
        if (e == null) {
            // This shouldn't happen, but return the default messages
            return errorMessage;
        }

        // Start with the exception and recurse to find the root cause
        Throwable t = e;
        while (t != null) {
            // Get the message from the Throwable class instance
            errorMessage = t.getLocalizedMessage();
            t = t.getCause();
        }
        // This is the root cause message
        return errorMessage;
    }
}
