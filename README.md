# 4.1 Exercise 1: Change Check Condition
In the project kitchensink, the minimum phone number check was modified to allow 8 digits. 

This was done by changing the `@Size` parameter in the `model/Member.java` file.

```
@Size(min = 10, max = 12)
@Size(min = 8, max = 12)
```

# 4.2 Exercise 2: Add Username and Password Fields
In project kitchensink, add username and password fields with appropriate check.

1. Firstly, new fields properties were written in the file `model/Member.java` with the following checks:
- `username` - must not be empty or null
- `username` - should not exist in the database
- `password` - at least 8 characters, with at least one number and one special character.

```
@NotNull
@NotEmpty
private String username;

@NotNull
@Size(min=8)
@Pattern(regexp = "^(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\\\|\\[{\\]};:'\",<.>/?]).+$", message = "Must have at least 1 number, and one special character.")
private String password;

public String getUsername() {
    return username;
}

public void setUsername(String username) {
    this.username = username;
}

public String getPassword() {
    return password;
}

public void setPassword(String password) {
    this.password = password;
}
```

2. The `index.xhtml` file was also modified to include the `username` and `password` fields.
```
<h:outputLabel for="username" value="Username:" />
<h:inputText id="username"
             value="#{newMember.username}" />
<h:message for="username" errorClass="invalid" />

<h:outputLabel for="password" value="Password:" />
<h:inputText id="password"
             value="#{newMember.password}"  type="password" />
<h:message for="password" errorClass="invalid" />
```

3. A funcition was added in data/MemberRepository.java as the username registered should be unique and not exist in the database.
```
public MemberRegisterModel findByUsername(String username) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<MemberRegisterModel> criteria = cb.createQuery(MemberRegisterModel.class);
    Root<MemberRegisterModel> member = criteria.from(MemberRegisterModel.class);
    // Swap criteria statements if you would like to try out type-safe criteria queries, a new
    // feature in JPA 2.0
    // criteria.select(member).where(cb.equal(member.get(Member_.email), email));
    criteria.select(member).where(cb.equal(member.get("username"), username));
    return em.createQuery(criteria).getSingleResult();
}
```
4. The username restriction was added into the `model/Member.java`
```
@Table(uniqueConstraints = {
	    @UniqueConstraint(columnNames = "email"),
	    @UniqueConstraint(columnNames = "username")
})
```

5. The `import.sql` file was modified to include the new fields.
```
insert into Member (id, name, email, phone_number, username, password) values (0, 'John Smith', 'john.smith@mailinator.com', '2125551212', 'jsmith', '01234567.') 
```

# 4.3 Exercise 3: Secure the Webservice
In project kitchensink, implement a security check that only valid registered username
and password can successfully access the webservice to read the JSON response.

1. The original `model/Member.java` was renamed to `model/MemberRegisterationModel.java`
2. A new model called `MemberLoginModel.java` was created. This contains the `username` and `password` only
```
@SuppressWarnings("serial")
@Entity
@XmlRootElement
public class MemberLoginModel implements Serializable {
	@Id
    @GeneratedValue
    private Long id;

    @NotNull
    @NotEmpty
    private String username;

    @NotNull
    @NotEmpty
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

3. A new controller called `controller/MemberLoginController.java` was created. This will process the login information, similar to how `controller/MemberRegisterController.java` processes the registeration information. 

If the login was successful, the page will redirect the user to the JSON response.
```
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
```

4. A new service called `MemberLogin.java` was created.
```
@Stateless
public class MemberLogin {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private MemberRepository repository;

    public boolean login(MemberLoginModel loginInfo) {
    	String username = loginInfo.getUsername();
    	String password = loginInfo.getPassword();
        log.info("Logging in " + username);
        MemberRegisterModel member = repository.findByUsername(username);
        if (member != null && member.getPassword().equals(password)) {
            return true;
        } else {
            return false;
        }
    }

}
```

5. A login field was added into the `index.xhtml` file.
```
<h:form id="login">
<h2>Member Login</h2>
	<h:panelGrid columns="2" columnClasses="titleCell">
	    <h:outputLabel for="login_username" value="Username:" />
	    <h:inputText id="login_username"  value="#{existingMember.username}"/>

	    <h:outputLabel for="login_password" value="Password:" />
	    <h:inputText id="login_password"  value="#{existingMember.password}" type="password" />
	</h:panelGrid>
	<p>
	    <h:panelGrid columns="2">
	        <h:commandButton id="login" value="Login" action="#{memberLoginController.login}" styleClass="register" />
	        <h:messages styleClass="messages" errorClass="invalid" infoClass="valid" warnClass="warning" globalOnly="true" />
	    </h:panelGrid>
	</p>
</h:form>
```
