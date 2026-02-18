package io.tpan.springcloudgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ReactiveClientRegistrationRepository repo) {
        http
                .authorizeExchange(ex -> ex.anyExchange().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationRequestResolver(new CustomRequestResolver(repo))
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable); // Disable for local testing

        return http.build();
    }
}
class CustomRequestResolver implements ServerOAuth2AuthorizationRequestResolver {
    private final DefaultServerOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomRequestResolver(ReactiveClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultServerOAuth2AuthorizationRequestResolver(repo);
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
        return defaultResolver.resolve(exchange).map(this::addAudience);
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange, String registrationId) {
        return defaultResolver.resolve(exchange, registrationId).map(this::addAudience);
    }

    private OAuth2AuthorizationRequest addAudience(OAuth2AuthorizationRequest request) {
        return OAuth2AuthorizationRequest.from(request)
                .additionalParameters(params -> params.put("audience", "https://tpan-api.duckdns.org"))
                .build();
    }
}