# Spring Security in Spring Boot: Understanding the Complete Request Lifecycle

## Table of Contents

1. [Overview](#1-overview)
2. [Understanding the Servlet Filter Architecture](#2-understanding-the-servlet-filter-architecture)
3. [The Complete HTTP Request Lifecycle](#3-the-complete-http-request-lifecycle)
   - [Stage 1: HTTP Request Enters the Application](#stage-1-http-request-enters-the-application)
   - [Stage 2: DelegatingFilterProxy](#stage-2-delegatingfilterproxy)
   - [Stage 3: FilterChainProxy](#stage-3-filterchainproxy)
   - [Stage 4: SecurityFilterChain](#stage-4-securityfilterchain)
   - [Stage 5: Authentication](#stage-5-authentication)
   - [Stage 6: Authorization](#stage-6-authorization)
   - [Stage 7: Exception Handling](#stage-7-exception-handling)
   - [Stage 8: DispatcherServlet and Controller](#stage-8-dispatcherservlet-and-controller)
4. [Complete Request Flow Diagram](#4-complete-request-flow-diagram)
5. [Core Spring Security Components](#5-core-spring-security-components)
6. [Understanding Filter Execution Order](#6-understanding-filter-execution-order)
7. [Key Takeaways](#7-key-takeaways)
8. [Suggested Next Steps](#8-suggested-next-steps)

---

# 1. Overview

Spring Security is a powerful authentication and authorization framework used to secure Java applications, particularly applications built with Spring Boot.

It protects application resources by determining:

- **Authentication:** Who are you?
- **Authorization:** What are you allowed to do?

For example, imagine we are developing an e-commerce application.

We have the following endpoints:

| Endpoint | Access requirement |
|---|---|
| `/api/auth/register` | Public |
| `/api/auth/login` | Public |
| `/api/products` | Authenticated users |
| `/api/orders` | Authenticated users |
| `/api/admin/users` | Administrators only |

Spring Security ensures that users cannot access resources they are not authorized to use.

## Why does Spring Security use Servlet Filters?

One important architectural decision in Spring Security is that security logic is implemented primarily through **Servlet Filters rather than Controllers**.

A Servlet Filter is a component that intercepts HTTP requests and responses before they reach the application servlet.

This approach has several advantages:

1. **Centralized security:** Security rules are configured in one place rather than repeated across multiple Controllers.
2. **Separation of concerns:** Controllers focus on business logic, while security components handle authentication and authorization.
3. **Early request interception:** Unauthorized requests can be rejected before application business logic executes.
4. **Reusability:** The same security rules can protect multiple endpoints.
5. **Consistency:** Every matching request passes through the configured security filters.

For example, without centralized security, developers might write authentication checks in every Controller:

```java
@RestController
public class ProductController {

    @GetMapping("/products")
    public String getProducts(HttpServletRequest request) {

        // Manually check whether the user is authenticated.
        // Manually check the user's permissions.

        return "Product list";
    }
}
```

This creates duplicated code and makes security difficult to maintain.

With Spring Security, we configure the security rules once:

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
);
```

Spring Security handles the checks before the request reaches the Controller.

## Why is DispatcherServlet security-agnostic?

`DispatcherServlet` is the central component of Spring MVC.

Its primary responsibility is to:

- Receive requests that have passed through the Servlet Filter chain.
- Identify the appropriate Controller.
- Find the matching handler method.
- Invoke the Controller.
- Process the returned result.
- Produce the HTTP response.

It does not need to understand how authentication works, how passwords are verified, or how roles are assigned.

That responsibility belongs to Spring Security.

This separation allows Spring MVC and Spring Security to work independently.

**Important:** `DispatcherServlet` does not automatically authenticate or authorize users. By the time an HTTP request reaches it, Spring Security's configured web authorization filters have already evaluated the request.

---

# 2. Understanding the Servlet Filter Architecture

Before understanding the complete request lifecycle, we need to understand how Servlet Filters work.

A Servlet Filter intercepts an HTTP request before it reaches a Servlet.

A filter can:

- Inspect HTTP headers.
- Read request parameters.
- Check authentication information.
- Modify requests and responses.
- Reject a request.
- Pass the request to the next filter.

A simplified Servlet Filter looks like this:

```java
@Component
public class ExampleFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        System.out.println("Request entering filter");

        // Continue to the next filter.
        chain.doFilter(request, response);

        System.out.println("Response leaving filter");
    }
}
```

The important method is:

```java
chain.doFilter(request, response);
```

It tells the Servlet container to continue processing the request through the remaining filters.

If a filter does not call this method, the request does not proceed further through that chain.

Spring Security builds on this mechanism by providing multiple specialized filters.

Examples include:

- `UsernamePasswordAuthenticationFilter`
- `BasicAuthenticationFilter`
- `SecurityContextHolderFilter`
- `AnonymousAuthenticationFilter`
- `ExceptionTranslationFilter`
- `AuthorizationFilter`
- `CsrfFilter`

Each filter has a specific responsibility.

---

# 3. The Complete HTTP Request Lifecycle

Let's follow an HTTP request from the client until it reaches a Controller.

Consider this request:

```http
GET /api/products HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOi...
```

The client wants to retrieve products from our application.

The request passes through several components before Spring MVC processes it.

## Stage 1: HTTP Request Enters the Application

The request originates from a client such as:

- A web browser.
- A mobile application.
- Postman.
- Another backend service.

The request first enters the embedded Servlet container.

In Spring Boot, this is commonly Tomcat, although Jetty and Undertow are also supported.

The Servlet container is responsible for managing HTTP requests, responses, Servlets, and Servlet Filters.

At this point, the request has not reached the Controller.

Instead, it enters the Servlet Filter chain.

## Stage 2: DelegatingFilterProxy

`DelegatingFilterProxy` is the bridge between the Servlet container and Spring Security.

The Servlet container understands Servlet Filters, but it does not directly manage Spring application beans.

Spring Security uses `DelegatingFilterProxy` to connect these two environments.

Its responsibility is to delegate the incoming request to a Spring-managed Filter bean, normally named `springSecurityFilterChain`.

The process looks like this:

1. The Servlet container receives the HTTP request.
2. It invokes `DelegatingFilterProxy`.
3. `DelegatingFilterProxy` locates the Spring-managed security Filter bean.
4. It delegates the request to that bean.

Conceptually:

```java
public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
) throws IOException, ServletException {

    Filter delegate = getFilterBean("springSecurityFilterChain");

    delegate.doFilter(request, response, chain);
}
```

The example above illustrates the idea, not the complete implementation.

**Remember:** `DelegatingFilterProxy` does not perform authentication itself. Its job is to connect the Servlet container to Spring Security.

## Stage 3: FilterChainProxy

The delegated Spring Security Filter bean is an instance of `FilterChainProxy`.

`FilterChainProxy` is the central entry point for Spring Security's Servlet-based security infrastructure.

Its responsibilities include:

- Managing multiple `SecurityFilterChain` instances.
- Finding the appropriate security chain for an incoming request.
- Invoking the filters belonging to the selected chain.
- Applying Spring Security's HTTP firewall.
- Cleaning up the security context when request processing finishes.

One important concept is that an application can have multiple security filter chains.

For example:

| Security chain | URL pattern | Purpose |
|---|---|---|
| API chain | `/api/**` | REST API security |
| Admin chain | `/admin/**` | Administrator security |
| Web chain | Other requests | Browser-based security |

`FilterChainProxy` evaluates the configured chains in order and selects the **first matching `SecurityFilterChain`**.

The remaining matching chains are not executed for that request.

This makes it possible to apply different authentication mechanisms and security policies to different parts of an application.

## Stage 4: SecurityFilterChain

A `SecurityFilterChain` defines which security filters should run for a particular HTTP request.

It is configured using `HttpSecurity`.

For example:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
```

Here, Spring Security constructs the required security filters based on the configuration.

### Understanding securityMatcher versus requestMatchers

These two methods have different purposes.

| Method | Responsibility |
|---|---|
| `securityMatcher()` | Determines which requests belong to a particular SecurityFilterChain. |
| `requestMatchers()` | Defines authorization rules for requests inside that chain. |

For example:

```java
@Bean
@Order(1)
public SecurityFilterChain apiSecurity(HttpSecurity http)
        throws Exception {

    http
        .securityMatcher("/api/**")
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        .httpBasic(Customizer.withDefaults());

    return http.build();
}
```

This chain applies only to `/api/**`.

Requests outside that pattern are evaluated against other configured chains.

If no chain matches, the request does not receive protection from a Spring Security chain. Therefore, applications should configure a suitable fallback chain when they need to secure all remaining requests.

---

## Stage 5: Authentication

Authentication answers one fundamental question:

**Who are you?**

Spring Security needs to establish the identity of the person or system making the request.

Depending on the authentication mechanism, credentials can come from:

- Username and password.
- HTTP Basic authentication.
- A JWT access token.
- OAuth2 access tokens.
- Session cookies.
- Other supported authentication mechanisms.

### 5.1 Extracting credentials

Suppose a client sends:

```http
POST /api/auth/login HTTP/1.1
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "mypassword"
}
```

For a traditional username/password login, an authentication filter or application login endpoint extracts the credentials and creates an unauthenticated `Authentication` object.

For example:

```java
Authentication authenticationRequest =
    UsernamePasswordAuthenticationToken.unauthenticated(
        "user@example.com",
        "mypassword"
    );
```

For HTTP Basic authentication, `BasicAuthenticationFilter` extracts the credentials from the `Authorization` header.

For JWT authentication, a suitable authentication filter extracts the bearer token and delegates validation to the configured authentication components.

**Important:** Not every authentication mechanism uses the same filter. The filter responsible for extracting credentials depends on the configured authentication mechanism.

### 5.2 AuthenticationManager

After credentials are extracted, the authentication mechanism passes an authentication request to the `AuthenticationManager`.

`AuthenticationManager` is an interface that defines the authentication operation.

Its main method is:

```java
Authentication authenticate(Authentication authentication)
        throws AuthenticationException;
```

It receives an unauthenticated `Authentication` object and attempts to authenticate it.

The most common implementation is `ProviderManager`.

### 5.3 ProviderManager

`ProviderManager` delegates authentication to one or more `AuthenticationProvider` implementations.

Think of `ProviderManager` as a coordinator.

It does not necessarily know how to validate every type of credential itself. Instead, it delegates the work to providers that understand particular authentication mechanisms.

For example:

| AuthenticationProvider | Responsibility |
|---|---|
| `DaoAuthenticationProvider` | Username/password authentication using `UserDetailsService`. |
| `JwtAuthenticationProvider` | JWT authentication. |
| `LdapAuthenticationProvider` | LDAP authentication. |

The process is:

1. `AuthenticationManager` receives an authentication request.
2. `ProviderManager` searches its configured providers.
3. A compatible provider processes the request.
4. The provider verifies the supplied credentials.
5. If successful, it returns an authenticated `Authentication` object.
6. If authentication fails, an `AuthenticationException` is thrown.

A provider can also indicate that it does not support the supplied authentication type, allowing another provider to try.

### 5.4 UserDetailsService

For username/password authentication, `DaoAuthenticationProvider` commonly uses `UserDetailsService`.

`UserDetailsService` is responsible for retrieving user information from a data source.

For example, it can retrieve a user from PostgreSQL.

```java
@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found")
            );

        return User.builder()
            .username(user.getEmail())
            .password(user.getPassword())
            .authorities(
                user.getRole().getAuthorities()
            )
            .build();
    }
}
```

The example assumes that the application's `User` entity and `getRole().getAuthorities()` method have been implemented.

The returned `UserDetails` object contains information Spring Security needs to authenticate the user.

Typically, it includes:

- Username.
- Encoded password.
- Granted authorities.
- Account status.

`UserDetailsService` retrieves the user. It does not independently authenticate the password.

### 5.5 PasswordEncoder

The retrieved password is compared with the password provided by the user using a `PasswordEncoder`.

Passwords should never be stored as plain text in a database.

Instead, Spring Security supports password hashing through implementations such as `BCryptPasswordEncoder`.

Example:

```java
@Configuration
public class ApplicationConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

During registration:

```java
String encodedPassword =
    passwordEncoder.encode("mypassword");
```

During authentication, the provider uses:

```java
passwordEncoder.matches(
    rawPassword,
    encodedPassword
);
```

If the password matches, authentication can succeed.

If it does not match, authentication fails.

Note that BCrypt is a one-way password hashing algorithm, not reversible encryption.

### 5.6 Storing the authenticated user

After successful authentication, Spring Security needs to make the authenticated user's identity available to the rest of the request-processing infrastructure.

This is done through:

- `SecurityContext`
- `SecurityContextHolder`

Conceptually:

```java
Authentication authenticated =
    authenticationManager.authenticate(authenticationRequest);

SecurityContext context =
    SecurityContextHolder.createEmptyContext();

context.setAuthentication(authenticated);

SecurityContextHolder.setContext(context);
```

This is illustrative of the context-setting operation. Normal Spring Security authentication filters handle this process for their respective authentication mechanisms.

The authenticated `Authentication` object now contains information about the current user.

**Important:** Spring Security 6 uses `SecurityContextHolderFilter` to load the context from a `SecurityContextRepository`. Saving a newly created context for subsequent requests is a separate operation when explicit-save behavior is in use. Authentication mechanisms integrated with Spring Security handle the appropriate context lifecycle.

---

## Stage 6: Authorization

Authentication has established the user's identity.

Now Spring Security asks:

**What are you allowed to do?**

This is authorization.

For example:

- Can the user access `/api/products`?
- Can the user access `/api/admin/users`?
- Can the user delete a product?
- Does the user have the `ADMIN` role?

### 6.1 AuthorizationFilter

In Spring Security 6, `AuthorizationFilter` is responsible for applying HTTP authorization rules configured through `authorizeHttpRequests`.

It uses an `AuthorizationManager` to evaluate whether a request should be allowed.

For example:

```java
http.authorizeHttpRequests(auth -> auth

    .requestMatchers("/api/auth/**").permitAll()

    .requestMatchers("/api/admin/**")
        .hasRole("ADMIN")

    .requestMatchers("/api/products/**")
        .hasAnyRole("USER", "ADMIN")

    .anyRequest().authenticated()
);
```

Spring Security evaluates the applicable authorization rule.

If the user satisfies the rule, the request continues through the remaining filter chain.

If the user does not satisfy the rule, access is denied.

### 6.2 GrantedAuthority

Spring Security uses `GrantedAuthority` to represent permissions assigned to an authenticated user.

For example:

```java
List<GrantedAuthority> authorities = List.of(
    new SimpleGrantedAuthority("ROLE_ADMIN"),
    new SimpleGrantedAuthority("PRODUCT_READ"),
    new SimpleGrantedAuthority("PRODUCT_DELETE")
);
```

These authorities are attached to the authenticated `Authentication` object.

### 6.3 Roles versus authorities

A role is a special convention for representing an authority.

Consider:

```java
.hasRole("ADMIN")
```

Spring Security interprets this as:

```java
.hasAuthority("ROLE_ADMIN")
```

The `ROLE_` prefix is automatically added by `hasRole`.

Compare:

| Configuration | Authority checked |
|---|---|
| `hasRole("ADMIN")` | `ROLE_ADMIN` |
| `hasAuthority("ROLE_ADMIN")` | `ROLE_ADMIN` |
| `hasAuthority("PRODUCT_READ")` | `PRODUCT_READ` |
| `hasAnyRole("ADMIN", "USER")` | `ROLE_ADMIN` or `ROLE_USER` |

Use roles when grouping broad user categories.

Use authorities when defining more specific permissions.

For example, an administrator may have:

```text
ROLE_ADMIN
PRODUCT_CREATE
PRODUCT_READ
PRODUCT_UPDATE
PRODUCT_DELETE
USER_MANAGE
```

This gives you flexibility to implement fine-grained access control.

---

## Stage 7: Exception Handling

Sometimes a request cannot proceed because authentication or authorization fails.

Spring Security uses `ExceptionTranslationFilter` to translate relevant security exceptions into HTTP responses.

It primarily handles:

- `AuthenticationException`
- `AccessDeniedException`

It is important to understand that this filter does not perform authentication or authorization itself. It handles exceptions raised by downstream security processing.

### 7.1 AuthenticationException

An `AuthenticationException` indicates that authentication failed.

Examples:

- Invalid username.
- Incorrect password.
- Invalid authentication token.
- Expired credentials.

The response depends on the authentication mechanism.

For example:

```http
HTTP/1.1 401 Unauthorized
```

For browser-based form login, Spring Security may redirect the user to the login page instead.

### 7.2 AccessDeniedException

An `AccessDeniedException` indicates that the user does not have sufficient permission to access a resource.

For example:

A user with `ROLE_USER` attempts to access:

```text
/api/admin/users
```

But the endpoint requires:

```java
.hasRole("ADMIN")
```

The user is authenticated, but does not have the required authority.

Spring Security typically responds with:

```http
HTTP/1.1 403 Forbidden
```

There is an important distinction:

- An unauthenticated user trying to access a protected resource is normally sent to the authentication entry point.
- An authenticated user who lacks the required permission is normally handled by the access-denied handler.

### 7.3 AuthenticationEntryPoint

`AuthenticationEntryPoint` determines how Spring Security requests authentication from an unauthenticated client.

Depending on the application, it can:

- Return HTTP 401.
- Redirect the user to a login page.
- Send a `WWW-Authenticate` challenge.

For a REST API, a common implementation returns HTTP 401.

### 7.4 AccessDeniedHandler

`AccessDeniedHandler` handles requests where an authenticated user lacks the required permissions.

For a REST API, the usual response is HTTP 403.

For example:

```http
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
    "error": "Access denied"
}
```

The response body can be customized through a custom `AccessDeniedHandler`.

### 7.5 RequestCache

`RequestCache` is particularly useful for browser-based applications.

Imagine a user attempts to access:

```text
/dashboard
```

But the user has not logged in.

Spring Security can:

1. Save the original request using `RequestCache`.
2. Redirect the user to the login page.
3. Allow the user to authenticate.
4. Redirect the user back to the original requested page.

This provides a better browsing experience.

For stateless REST APIs, request caching is often unnecessary, and applications commonly disable it.

Example:

```java
http.requestCache(cache ->
    cache.disable()
);
```

### Exception handling summary

| Situation | Typical result |
|---|---|
| Unauthenticated user requests a protected REST endpoint | 401 Unauthorized |
| Unauthenticated browser user requests a protected page | Redirect to login |
| Authenticated user lacks permission | 403 Forbidden |
| Invalid credentials during login | Authentication failure, commonly 401 for REST APIs |

---

## Stage 8: DispatcherServlet and Controller

If the request successfully passes through the security filters, it continues to the Servlet container's remaining filter chain.

Eventually, it reaches Spring MVC's `DispatcherServlet`.

At this point, Spring Security has already performed the configured request-level authentication and authorization checks.

### 8.1 DispatcherServlet

`DispatcherServlet` is responsible for routing the request to the appropriate Controller.

For example:

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public String getProducts() {
        return "Here is the product list";
    }
}
```

When a request arrives at:

```text
GET /api/products
```

Spring MVC identifies the matching method and invokes it.

The Controller does not need to manually authenticate the user.

### 8.2 Accessing the authenticated user using SecurityContextHolder

A Controller can retrieve the current authenticated user using `SecurityContextHolder`.

```java
@RestController
public class UserController {

    @GetMapping("/api/me")
    public String getCurrentUser() {

        Authentication authentication =
            SecurityContextHolder
                .getContext()
                .getAuthentication();

        return authentication.getName();
    }
}
```

The `getName()` method returns the authenticated principal's name.

For username/password authentication, this is commonly the username or email address.

### 8.3 Accessing the user using @AuthenticationPrincipal

Spring Security also provides `@AuthenticationPrincipal`, which is often cleaner when working with Spring MVC.

```java
@RestController
public class UserController {

    @GetMapping("/api/me")
    public UserDetails getCurrentUser(
            @AuthenticationPrincipal UserDetails user
    ) {

        return user;
    }
}
```

This annotation injects the authenticated principal into the Controller method.

It avoids manually accessing `SecurityContextHolder`.

For custom user principal classes, you can use your own type instead of `UserDetails`.

**Security recommendation:** Avoid returning sensitive information from the principal, such as password hashes. Prefer returning a dedicated response DTO containing only the information the client needs.

### 8.4 The Controller returns a response

After processing the request, the Controller returns its result.

Spring MVC processes the result and produces the HTTP response.

For example:

```java
@GetMapping("/api/products")
public ResponseEntity<String> getProducts() {

    return ResponseEntity.ok("Products retrieved successfully");
}
```

The response then travels back through the Servlet filter chain in reverse unwinding order.

Spring Security also completes its request lifecycle, including clearing thread-associated security context state where appropriate.

---

# 4. Complete Request Flow Diagram

The following diagram summarizes the complete lifecycle of a typical HTTP request.

```text
                  CLIENT
                    |
                    |
              HTTP REQUEST
                    |
                    v
          SERVLET CONTAINER
             (Tomcat)
                    |
                    v
          DelegatingFilterProxy
                    |
                    v
             FilterChainProxy
                    |
                    v
            SecurityFilterChain
                    |
                    |
          +---------+----------+
          |                    |
          v                    v
    SecurityContext       Security Filters
    Holder Filter         (CSRF, CORS, etc.)
          |                    |
          +---------+----------+
                    |
                    v
              AUTHENTICATION
                    |
                    v
          Extract Credentials
                    |
                    v
          AuthenticationManager
                    |
                    v
             ProviderManager
                    |
                    v
          AuthenticationProvider
                    |
                    v
       UserDetailsService (if used)
                    |
                    v
             PasswordEncoder
                    |
                    v
          Authentication SUCCESS
                    |
                    v
          SecurityContextHolder
                    |
                    v
              AUTHORIZATION
                    |
                    v
             AuthorizationFilter
                    |
                    v
          Check GrantedAuthority
                    |
              +-----+-----+
              |           |
              v           v
           ALLOWED      DENIED
              |           |
              |           v
              |    ExceptionTranslationFilter
              |           |
              |      +----+-----+
              |      |          |
              |      v          v
              | Authentication AccessDenied
              |  EntryPoint       Handler
              |      |              |
              |      v              v
              |    401/           403
              |   REDIRECT
              |
              v
        DispatcherServlet
              |
              v
         HandlerMapping
              |
              v
          Controller
              |
              v
       Business Logic / Service
              |
              v
         HTTP RESPONSE
              |
              v
            CLIENT
```

**Note:** This is a conceptual diagram, not a literal list of every filter. Actual filters depend on the application's configuration and authentication mechanism. `ExceptionTranslationFilter` wraps downstream processing, including authorization, and can translate exceptions raised there. Authentication filters may also handle their own authentication failures.

---

# 5. Core Spring Security Components

Every Spring Boot developer working with Spring Security should understand the following components.

## 5.1 SecurityContextHolder

`SecurityContextHolder` is the central location through which Spring Security exposes the current security context.

It allows application code to access the currently authenticated user.

```java
Authentication authentication =
    SecurityContextHolder.getContext().getAuthentication();
```

### ThreadLocal behavior

By default, Spring Security uses `ThreadLocal` to store the `SecurityContext`.

This means the security context is associated with the current execution thread.

Conceptually:

```text
Thread 1
   |
   +---- SecurityContext
   |          |
   |          +---- Authentication (User A)
   |
Thread 2
   |
   +---- SecurityContext
              |
              +---- Authentication (User B)
```

Each thread has its own context, preventing one request's authentication from automatically becoming another request's authentication.

Spring Security manages the context lifecycle and clears it after request processing to avoid leaking authentication information between requests.

Important considerations:

- Do not assume a new asynchronous thread automatically inherits the security context.
- Use Spring Security's supported context-propagation mechanisms when working with asynchronous tasks.
- The default strategy is `MODE_THREADLOCAL`.

## 5.2 SecurityContext

`SecurityContext` holds the `Authentication` object for the current security context.

Example:

```java
SecurityContext context =
    SecurityContextHolder.getContext();

Authentication authentication =
    context.getAuthentication();
```

The relationship is:

```text
SecurityContextHolder
        |
        v
  SecurityContext
        |
        v
  Authentication
        |
        +---- Principal
        |
        +---- Credentials
        |
        +---- Authorities
```

## 5.3 Authentication

`Authentication` is an interface representing either:

1. Credentials submitted by a user who is attempting to authenticate.
2. The identity of a user who has successfully authenticated.

It contains three important properties:

| Property | Meaning | Example |
|---|---|---|
| Principal | Who the user is | `user@example.com` or `UserDetails` |
| Credentials | Proof of identity | Password or token |
| Authorities | What the user can access | `ROLE_ADMIN` |

Example:

```java
Authentication authentication =
    SecurityContextHolder
        .getContext()
        .getAuthentication();

Object principal = authentication.getPrincipal();

Object credentials = authentication.getCredentials();

Collection<? extends GrantedAuthority> authorities =
    authentication.getAuthorities();
```

After successful authentication, credentials are commonly erased to prevent sensitive information from being retained unnecessarily.

## 5.4 AuthenticationManager

`AuthenticationManager` defines the authentication process.

It receives an authentication request and attempts to authenticate it.

```java
public interface AuthenticationManager {

    Authentication authenticate(
        Authentication authentication
    ) throws AuthenticationException;
}
```

It is the main interface that authentication filters use to authenticate credentials.

## 5.5 ProviderManager

`ProviderManager` is the most common implementation of `AuthenticationManager`.

It delegates authentication to one or more `AuthenticationProvider` implementations.

Example concept:

```text
AuthenticationManager
        |
        v
  ProviderManager
        |
        +---- DaoAuthenticationProvider
        |
        +---- JwtAuthenticationProvider
        |
        +---- Other Providers
```

Each provider handles an authentication type it supports.

## 5.6 AuthenticationProvider

An `AuthenticationProvider` contains the actual logic for authenticating a particular type of credentials.

For example, `DaoAuthenticationProvider` supports username/password authentication using `UserDetailsService` and `PasswordEncoder`.

A custom provider can be implemented when an application requires a specialized authentication mechanism.

## 5.7 SecurityFilterChain

`SecurityFilterChain` defines which security filters apply to a request.

In Spring Security 6.x, the recommended configuration style is to declare a `SecurityFilterChain` bean rather than extending the old `WebSecurityConfigurerAdapter`.

Example:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .securityMatcher("/api/**")

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
```

This is an illustrative configuration for an application that separates API and browser security.

Important details:

- `@Order(1)` gives the API chain higher priority.
- `securityMatcher("/api/**")` limits the first chain to API requests.
- The second chain handles requests not matched by the first chain.
- `STATELESS` means Spring Security should not use an HTTP session to persist the security context for subsequent requests.
- Disabling CSRF is appropriate only when the API's authentication model and browser threat model justify it. Do not blindly disable CSRF in cookie- or session-authenticated browser applications.
- HTTP Basic is shown for simplicity. Production APIs commonly use an appropriately configured bearer-token authentication mechanism.

## 5.8 UserDetailsService

`UserDetailsService` retrieves user information from the application's user store.

It is commonly used by `DaoAuthenticationProvider`.

```java
public interface UserDetailsService {

    UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException;
}
```

A custom implementation typically retrieves a user from a database.

It is important to remember that retrieving a user and authenticating a user are two different responsibilities.

## 5.9 PasswordEncoder

`PasswordEncoder` handles secure password hashing and verification.

Example:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

To encode:

```java
String password = passwordEncoder.encode("secret123");
```

To verify:

```java
boolean matches = passwordEncoder.matches(
    "secret123",
    storedEncodedPassword
);
```

Never store raw passwords in a database.

## 5.10 GrantedAuthority

`GrantedAuthority` represents a permission granted to an authenticated user.

Example:

```java
new SimpleGrantedAuthority("ROLE_ADMIN");
```

It is used by Spring Security during authorization.

Remember:

```java
hasRole("ADMIN")
```

is equivalent to:

```java
hasAuthority("ROLE_ADMIN")
```

Roles are a convenient convention, while authorities can represent both roles and more specific permissions.

---

# 6. Understanding Filter Execution Order

Spring Security executes filters in a carefully defined order.

The order is important because authentication must generally happen before authorization.

A simplified view of a commonly configured Spring Security 6.x filter chain is:

| Order | Filter | Responsibility |
|---|---|---|
| 1 | `DisableEncodeUrlFilter` | Prevents session identifiers from being encoded into URLs. |
| 2 | `WebAsyncManagerIntegrationFilter` | Integrates the security context with Spring MVC asynchronous processing. |
| 3 | `SecurityContextHolderFilter` | Loads the security context for the request. |
| 4 | `HeaderWriterFilter` | Writes security-related HTTP headers. |
| 5 | `CorsFilter` (when configured) | Handles CORS processing. |
| 6 | `CsrfFilter` | Protects against Cross-Site Request Forgery. |
| 7 | `LogoutFilter` | Processes logout requests. |
| 8 | Authentication filters | Authenticate the request using the configured mechanism. |
| 9 | `AnonymousAuthenticationFilter` | Provides anonymous authentication when no authenticated user exists. |
| 10 | `ExceptionTranslationFilter` | Translates downstream authentication and access-denied exceptions. |
| 11 | `AuthorizationFilter` | Applies request authorization rules. |

This is a simplified illustration. The actual list and ordering depend on the configured features, authentication mechanisms, and Spring Security version.

For example, `UsernamePasswordAuthenticationFilter` is typically used with form login, while `BasicAuthenticationFilter` is used with HTTP Basic authentication. They do not necessarily both appear in every application.

### How can you see the actual filter order?

Spring Security logs the configured filter chains at DEBUG level.

Add the following to `application.properties`:

```properties
logging.level.org.springframework.security=DEBUG
```

When the application starts, look for output similar to:

```text
Will secure any request with [
    DisableEncodeUrlFilter,
    WebAsyncManagerIntegrationFilter,
    SecurityContextHolderFilter,
    HeaderWriterFilter,
    CsrfFilter,
    LogoutFilter,
    UsernamePasswordAuthenticationFilter,
    DefaultLoginPageGeneratingFilter,
    BasicAuthenticationFilter,
    AnonymousAuthenticationFilter,
    ExceptionTranslationFilter,
    AuthorizationFilter
]
```

The output depends on your application's configuration.

This is one of the most useful ways to understand what Spring Security is actually doing behind the scenes.

---

# 7. Key Takeaways

- Spring Security protects Spring Boot applications using Servlet Filters.
- Security logic is separated from Controller business logic.
- `DelegatingFilterProxy` connects the Servlet container to Spring-managed security infrastructure.
- `FilterChainProxy` selects the appropriate `SecurityFilterChain`.
- `SecurityFilterChain` defines the security filters applied to matching requests.
- Authentication answers: **Who are you?**
- Authorization answers: **What are you allowed to do?**
- `AuthenticationManager` delegates authentication, commonly through `ProviderManager`.
- `UserDetailsService` retrieves user information, while `PasswordEncoder` verifies passwords.
- `SecurityContextHolder` makes the current authentication available throughout request processing.
- `AuthorizationFilter` applies HTTP authorization rules.
- `ExceptionTranslationFilter` handles downstream authentication and authorization exceptions.
- `DispatcherServlet` receives requests that have passed the applicable security filters.
- Controllers can access the current user using `SecurityContextHolder` or `@AuthenticationPrincipal`.

The most important concept is that **the Controller should not be responsible for implementing the application's entire security mechanism. Spring Security handles that responsibility before the request reaches the Controller.**

---

# 8. Suggested Next Steps

Understanding the architecture is the first step. The next step is to implement and experiment with the components yourself.

## 8.1 Read the official documentation

The following resources provide more detailed explanations and practical examples.

1. [Spring Security Official Documentation](https://docs.spring.io/spring-security/reference/)
2. [Spring Security Servlet Architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
3. [Servlet Authentication Architecture](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html)
4. [Authorization for HTTP Requests](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)
5. [Password Storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)
6. [Spring Boot Official Documentation](https://docs.spring.io/spring-boot/reference/)

## 8.2 Enable Spring Security debug logging

Start by enabling:

```properties
logging.level.org.springframework.security=DEBUG
```

Make requests using Postman and observe:

- Which security filter chain is selected.
- Which filters execute.
- Whether authentication succeeds.
- Whether authorization succeeds.
- Why a request is rejected.

## 8.3 Build a custom authentication filter

Create a custom filter using `OncePerRequestFilter`.

For example, build a JWT authentication filter that:

1. Reads the `Authorization` header.
2. Extracts the JWT bearer token.
3. Validates the token.
4. Retrieves the user identity and authorities.
5. Creates an authenticated `Authentication` object.
6. Places it in the `SecurityContextHolder`.
7. Continues the filter chain.

This will help you understand how JWT authentication integrates with Spring Security.

## 8.4 Implement a custom AuthenticationProvider

Create a custom `AuthenticationProvider` to understand how `ProviderManager` delegates authentication.

Experiment with:

- Implementing `supports()`.
- Implementing `authenticate()`.
- Validating credentials.
- Returning an authenticated `Authentication` object.
- Handling authentication failures.

## 8.5 Build a complete authentication project

A good practical project would be a Spring Boot REST API with PostgreSQL.

Implement the following features:

- User registration.
- Password hashing using BCrypt.
- User login.
- JWT generation.
- JWT validation.
- Custom JWT authentication filter.
- Role-based authorization.
- Protected endpoints.
- Custom 401 and 403 responses.

Suggested endpoint structure:

| Endpoint | Purpose | Access |
|---|---|---|
| `POST /api/auth/register` | Register a user | Public |
| `POST /api/auth/login` | Authenticate a user | Public |
| `GET /api/users/me` | Get current user | Authenticated |
| `GET /api/products` | List products | Authenticated |
| `POST /api/products` | Create a product | ADMIN |
| `DELETE /api/products/{id}` | Delete a product | ADMIN |

---

## Final Thought

When learning Spring Security, do not begin by memorizing every annotation or configuration method.

Instead, understand the request lifecycle:

**HTTP Request → Servlet Filters → Authentication → Authorization → DispatcherServlet → Controller.**

Once you understand how a request moves through these components, the individual Spring Security classes, interfaces, and configuration methods become much easier to understand.
