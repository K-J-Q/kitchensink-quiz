# 4.1 Exercise 1: Change Check Condition
In project kitchensink, change the phone number check from min 10 digits to 8 digits.

This was achieved using the *@size* parameter

```
@Size(min = 10, max = 12)
@Size(min = 8, max = 12)
```

# 4.2 Exercise 2: Add Username and Password Fields
In project kitchensink, add username and password fields with appropriate check.
# 4.3 Exercise 3: Secure the Webservice
In project kitchensink, implement a security check that only valid registered username
and password can successfully access the webservice to read the JSON response.
