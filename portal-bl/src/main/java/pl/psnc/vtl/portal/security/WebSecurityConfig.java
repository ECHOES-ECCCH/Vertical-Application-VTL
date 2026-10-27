package pl.psnc.vtl.portal.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import pl.psnc.vtl.portal.conf.OauthConfig;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.security.adfs.ADFSAuthenticationFilter;
import pl.psnc.vtl.portal.security.adfs.ADFSService;
import pl.psnc.vtl.portal.security.csrf.CSRFTokenService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pl.psnc.vtl.portal.model.users.SecurityGroup.*;

@Configuration
@EnableWebSecurity
@Slf4j
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String OAUTH_AUTHORIZATION_ENDPOINT = "/api/oauth2/authorization";

    @Value("${vtl.auth.cors.max-age}")
    private Long corsMaxAge;

    @Value("${vtl.auth.remember-me.secret-key}")
    private String REMEMBER_ME_TOKEN_KEY;

    @Value("${vtl.auth.cors.domains}")
    private String CORSComaSeparatedDomains;

    @Value("${vtl.auth.cors.domain-patterns-public}")
    private String publicCorsComaSeparatedDomainPatterns;

    @Value("${vtl.auth.csrf.secret-key}")
    private String CSRF_KEY;
    private final OauthConfig oc;

    private static final String REMEMBER_ME = "remember-me";

    private final JwtTokenProvider jwtTokenProvider;
    private final RememberMeTokenProvider rememberMeTokenProvider;

    private final PasswordEncoder passwordEncoder;

    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final DecryptionService decryptionService;

    private final UserRepository userRepository;

    private final ADFSService adfsService;
    private final SecurityConfiguration securityConfiguration;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //Oauth2
        if (oc != null && oc.getRegistration() != null && !oc.getRegistration().isEmpty()) {
            //TODO: change default oauth login path from /login to /api/outh2
            http.authorizeRequests().and().authenticationProvider(this.authProvider()).oauth2Login()
                    .redirectionEndpoint().baseUri("/api/oauth2/callback/*")
                    .and()
                    .authorizationEndpoint().baseUri(OAUTH_AUTHORIZATION_ENDPOINT)
                    .authorizationRequestRepository(authorizationRequestRepository())
                    .and().tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient())
                    .and().successHandler(oAuth2AuthenticationSuccessHandler).failureHandler(
                    oAuth2AuthenticationFailureHandler());

        }

        //logout
        http.logout().logoutUrl("/api/auth/sign-out").permitAll()
                .logoutSuccessHandler(this::onLogoutSuccess)
                .invalidateHttpSession(true).clearAuthentication(true)
                .deleteCookies("JSESSIONID").deleteCookies(REMEMBER_ME);

        http.cors().and().csrf().disable().exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy());
        //URL config
        http
                .authorizeRequests()
                .antMatchers("/api/auth/token").authenticated();
        //Superadmin access
        http
                .authorizeRequests()
                .antMatchers("/api/admin/**").hasAuthority(SUPER_ADMIN.getAuthority())
                .antMatchers("/api/user-transcriptions/**").hasAuthority(SUPER_ADMIN.getAuthority())
                .antMatchers(HttpMethod.POST, "/api/events").hasAuthority(SUPER_ADMIN.getAuthority());

        http
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/api/oauth2/**").permitAll()
                .antMatchers("/api/info/**").permitAll()
                .antMatchers("/api/iiif/**").permitAll()
                .antMatchers("/api/opensearch/**").permitAll()
                .antMatchers("/api/oai-pmh/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/healthcheck/buildinfo").authenticated()
                .antMatchers(HttpMethod.GET, "/api/healthcheck/start-state").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/users/reset-password/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/transcriptions/*/permissions").hasAnyAuthority(
                        SUPER_ADMIN.getAuthority(), EXPORT_REVIEWER.getAuthority(), USER.getAuthority())
                .antMatchers(HttpMethod.GET, "/api/transcriptions/*/statistics").hasAnyAuthority(
                        SUPER_ADMIN.getAuthority(), EXPORT_REVIEWER.getAuthority(), USER.getAuthority())
                .antMatchers(HttpMethod.GET, "/api/transcriptions/*/verse-statistics").authenticated()
                .antMatchers(HttpMethod.POST, "/api/transcriptions/*/eBooks/*").permitAll()
                .antMatchers(HttpMethod.POST, "/api/transcriptions/**").hasAnyAuthority(
                        SUPER_ADMIN.getAuthority(), EXPORT_REVIEWER.getAuthority(), USER.getAuthority())
                .antMatchers(HttpMethod.PUT, "/api/transcriptions/**").hasAnyAuthority(
                        SUPER_ADMIN.getAuthority(), EXPORT_REVIEWER.getAuthority(), USER.getAuthority())
                .antMatchers(HttpMethod.DELETE, "/api/transcriptions/**").hasAnyAuthority(
                        SUPER_ADMIN.getAuthority(), EXPORT_REVIEWER.getAuthority(), USER.getAuthority())
                .antMatchers(HttpMethod.GET, "/api/transcriptions/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/my-transcriptions/**").hasAnyAuthority(
                        SUPER_ADMIN.getAuthority(), EXPORT_REVIEWER.getAuthority(), USER.getAuthority())
                .antMatchers(HttpMethod.GET, "/api/my-last/transcription/**").hasAnyAuthority(
                        SUPER_ADMIN.getAuthority(), EXPORT_REVIEWER.getAuthority(), USER.getAuthority())
                .antMatchers(HttpMethod.GET, "/api/pages/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/verses/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/indexes/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/domains").hasAnyAuthority(
                        SUPER_ADMIN.getAuthority(), EXPORT_REVIEWER.getAuthority(), USER.getAuthority())
                .antMatchers(HttpMethod.GET, "/api/projects/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/events/**").authenticated()
                .antMatchers("/api/indexes/**").hasAnyAuthority(
                        SUPER_ADMIN.getAuthority(), EXPORT_REVIEWER.getAuthority(), USER.getAuthority())
                .antMatchers("/api/static-files/**").permitAll()
                .antMatchers("/api/languages").permitAll()
                .antMatchers("/api/ocr/languages").permitAll()
                .antMatchers("/api/last-transcriptions/**").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/api/oauth2/success").authenticated()
                .antMatchers("/api/communication/**").authenticated()
                .antMatchers("/api/pageLayers/**").hasAnyAuthority(
                        SUPER_ADMIN.getAuthority(), EXPORT_REVIEWER.getAuthority(), USER.getAuthority())
                .antMatchers("/api/indexes/**").hasAnyAuthority(
                        SUPER_ADMIN.getAuthority(), EXPORT_REVIEWER.getAuthority(), USER.getAuthority());
        http
                .rememberMe()
                .key(REMEMBER_ME_TOKEN_KEY)
                .rememberMeServices(rememberMeServices());
        // Filter chain config comment out for openid
        http
                .addFilterBefore(jwtAuthenticationFilter(), RememberMeAuthenticationFilter.class);
        http
                .addFilterBefore(adfsAuthenticationFilter(), JwtAuthenticationFilter.class);

    }

    private void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        String redirectUri = securityConfiguration.getPostLogoutRedirectUri(request, authentication);
        if (redirectUri != null)
            response.sendRedirect(redirectUri);
    }

    @Bean
      OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        return new DefaultAuthorizationCodeTokenResponseClient();
    }

    @Bean
      CSRFTokenService csrfTokenService() {
        return new CSRFTokenService(CSRF_KEY);
    }

    @Bean
      AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    OAuth2AuthenticationFailureHandlerImpl oAuth2AuthenticationFailureHandler() {
        return new OAuth2AuthenticationFailureHandlerImpl();
    }

    @Bean
    FilterRegistrationBean<CorsFilter> filterRegistrationBean() {
        List<String> publicDomainPatterns = Arrays.stream(publicCorsComaSeparatedDomainPatterns.split(",")).map(String::trim).collect(
                Collectors.toList());

        CorsConfiguration publicEndpointsConfiguration = new CorsConfiguration();
        publicEndpointsConfiguration.setAllowedOriginPatterns(publicDomainPatterns);
        publicEndpointsConfiguration.setAllowCredentials(true);
        publicEndpointsConfiguration.setAllowedMethods(Arrays.asList("HEAD", "GET", "OPTIONS"));
        publicEndpointsConfiguration.setAllowedHeaders(
                Arrays.asList("Authorization", "content-type", "language", "Set-Cookie", "X-CSRF-TOKEN"));
        publicEndpointsConfiguration.setExposedHeaders(
                Arrays.asList("Set-Cookie", "Content-Disposition", "Location", "CSRF-TOKEN"));
        publicEndpointsConfiguration.setMaxAge(corsMaxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/iiif/**", publicEndpointsConfiguration);
        source.registerCorsConfiguration("/api/projects/**", publicEndpointsConfiguration);
        source.registerCorsConfiguration("/api/oai-pmh/**", publicEndpointsConfiguration);

        CorsConfiguration publicBooksEndpointConfiguration = new CorsConfiguration();
        publicBooksEndpointConfiguration.setAllowedOriginPatterns(publicDomainPatterns);
        publicBooksEndpointConfiguration.setAllowCredentials(true);
        publicBooksEndpointConfiguration.setAllowedMethods(List.of("POST"));
        publicBooksEndpointConfiguration.setAllowedHeaders(
                Arrays.asList("Authorization", "content-type", "language", "Set-Cookie", "X-CSRF-TOKEN"));
        publicBooksEndpointConfiguration.setExposedHeaders(
                Arrays.asList("Set-Cookie", "Content-Disposition", "Location", "CSRF-TOKEN"));
        publicBooksEndpointConfiguration.setMaxAge(corsMaxAge);

        source.registerCorsConfiguration("/api/transcriptions/*/Books/*", publicBooksEndpointConfiguration);

        CorsConfiguration configuration = new CorsConfiguration();
        List<String> allowedDomains = Arrays.asList(CORSComaSeparatedDomains.split(","));
        configuration.setAllowedOrigins(allowedDomains);
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(
                Arrays.asList("Authorization", "content-type", "language", "Set-Cookie", "X-CSRF-TOKEN"));
        configuration.setExposedHeaders(
                Arrays.asList("Authorization", "Set-Cookie", "Content-Disposition", "Location", "CSRF-TOKEN"));
        configuration.setMaxAge(corsMaxAge);
        configuration.setAllowCredentials(true);

        source.registerCorsConfiguration("/api/**", configuration);

        FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(new CorsFilter(source));
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }

    @Bean
    OidcAuthorizationCodeAuthenticationProvider authProvider() {
        OidcUserService oidcUserService = new OidcUserService();
        oidcUserService.setOauth2UserService(new CustomOAuth2UserService());
        return new OidcAuthorizationCodeAuthenticationProvider(accessTokenResponseClient(), oidcUserService);
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .authenticationProvider(this.authProvider())
                .userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder);
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    //Remember Me staff
    @Bean
      RememberMeServices rememberMeServices() {
          return new RememberMeServiceImpl(REMEMBER_ME, userDetailsService(), rememberMeTokenProvider,
                  jwtTokenProvider, csrfTokenService(), REMEMBER_ME_TOKEN_KEY);
    }

    @Bean
      RememberMeAuthenticationFilter rememberMeAuthenticationFilter() throws Exception {
        return new RememberMeAuthenticationFilter(authenticationManager(), rememberMeServices());
    }

    @Bean
      AuthenticationSuccessHandler authenticationSuccessHandler() {
          return new AuthenticationSuccessHandlerImpl(rememberMeTokenProvider, jwtTokenProvider, REMEMBER_ME);
    }

    @Bean
      AuthenticationFailureHandler authenticationFailureHandler() {
        return new AuthenticationFailureHandlerImpl();
    }

    @Bean
      ADFSAuthenticationFilter adfsAuthenticationFilter() {
        return new ADFSAuthenticationFilter(decryptionService, adfsService, authenticationSuccessHandler());
    }

    @Bean
      JwtAuthenticationFilter jwtAuthenticationFilter() {
          return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService());
    }

    @Override
    @Bean
    public CustomUserDetailsService userDetailsService() {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter() throws Exception {
        UsernamePasswordAuthenticationFilter filter = new AuthenticationFilter();
        filter.setRememberMeServices(rememberMeServices());
        filter.setAuthenticationManager(authenticationManager());
        filter.setFilterProcessesUrl("/api/auth/sign-in");
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return filter;
    }
}