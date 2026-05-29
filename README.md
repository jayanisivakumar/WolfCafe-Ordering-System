# WolfCafe 

[![Frontend Tests](https://github.com/ncstate-csc-coursework/csc326-2026-spring-tp-204-2/actions/workflows/frontend-tests.yml/badge.svg)](https://github.com/ncstate-csc-coursework/csc326-2026-spring-tp-204-2/actions/workflows/frontend-tests.yml)


## Install Lombok
Lombok is a library that lets us use annotations to automatically generate getters, setters, and constructors.  For Lombok to work in Eclipse (and other IDEs like IntelliJ or VS Code), you need to set up Lombok with the IDE in addition to including in the pom.xml file.

Follow the [instructions for setting up Lombok in Eclipse](https://projectlombok.org/setup/eclipse) (you may need to specify the location of your Eclipse installation).  Make sure you download the latest version of Lombok from [Maven Repository](https://mvnrepository.com/artifact/org.projectlombok/lombok) as a jar file. If you have a `development` directory for your Eclipse installations, it's helpful to add the Lombok jar there.

## Configuration

Update `application.properties` in `src/main/resources/` and `src/test/resources/`.

  * Do not commit secrets. Use environment variables or local config files.
  * Provide `spring.datasource.password` via an environment variable (e.g. `DB_PASSWORD`).
  * Provide `app.jwt-secret` and `app.admin-user-password` via environment variables.
  
### Set `app.jwt-secret`

We will create a secret key that will be used for JWT authentication.  Think of a secret key phrase.  You'll want to encrypt it using SHA256 encryption.  You can use a tool like:  https://emn178.github.io/online-tools/sha256.html to generate the encrypted text.  Copy that into your `application.properties` file.

## Setup
The rest of the setup for WolfCafe is the same as for [CoffeeMaker](https://courses.csc.ncsu.edu/csc326/onboarding/setup/).


## User Roles

User roles are defined and initialized in `config.Roles`.  The `ADMIN` role is a constant.  All other roles are listed in the `UserRoles` enumeration. You can add new roles by adding the role name to the enumeration.

### Initializing the Roles/Admin user in the DB

`config.SetupDataLoader` initializes the DB with roles and creates a default user with the `ADMIN` role.  This class is automatically run when the application starts.  

The admin user has the user name of "admin" and an email address of "admin@admin.edu".  You will specify the password for the admin user in the `application.properties` file.  The password is read in from the `application.properties` and then encrypted using the password encoder.  

## Testing User Authentication in Postman

The following provides examples of how to work with user authentication in Postman.

### Create a New User

Endpoint: `POST http://localhost:8080/api/auth/register`

Body:

```
{
  "name": "Sarah Heckman",
  "username": "sheckman",
  "email": "sheckman@ncsu.edu",
  "password": "<redacted>"
}
```

Response: 201 Created

```
User registered successfully.
```

### Login with User

Endpoint: `POST http://localhost:8080/api/auth/login`

Body: 

```
{
  "usernameOrEmail": "sheckman",
  "password": "<redacted>"
}
```

Response: 200 OK

```
{
  "accessToken": "<redacted token>",
  "tokenType": "Bearer",
  "role": "ROLE_CUSTOMER"
}
```

Note the accessToken will vary with each login.  You'll want to save this for testing endpoints that require authentication.

### Get Items

Endpoint: `GET http://localhost:8080/api/items`

Roles: STAFF, CUSTOMER

Authorization:
  * Bearer
  * Token - copy from the response of an authenticated User
  
Response: 200 OK

```
JSON list of items
```
