    package com.innowise.paymentservice.config.security;

    import lombok.RequiredArgsConstructor;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.context.annotation.Profile;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.oauth2.jwt.JwtDecoder;
    import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
    import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
    import org.springframework.security.web.SecurityFilterChain;

    @Configuration
    @EnableWebSecurity
    @RequiredArgsConstructor
    @Profile("!test")
    public class SecurityConfiguration {

        private final JwtDecoder jwtDecoder;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
            httpSecurity
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated()
                    )
                    .oauth2ResourceServer(oauth2 ->
                            oauth2.jwt(
                                    jwt -> jwt.decoder(jwtDecoder)
                                            .jwtAuthenticationConverter(jwtAuthenticationConverter())
                            )
                    );
            return httpSecurity.build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
            JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

            jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("role");
            jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

            JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
            jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

            return jwtAuthenticationConverter;
        }

    }
