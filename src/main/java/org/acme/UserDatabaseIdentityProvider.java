package org.acme;

import io.quarkus.arc.Arc;
import io.quarkus.logging.Log;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.db.User;
import org.acme.db.UserRepository;

import java.util.Optional;

import static io.quarkus.security.identity.IdentityProvider.SYSTEM_FIRST;

@Priority(SYSTEM_FIRST)
@ApplicationScoped
public class UserDatabaseIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {
    private final UserRepository userRepository;

    public UserDatabaseIdentityProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
                                              AuthenticationRequestContext context) {
        return Uni.createFrom().item(QuarkusSecurityIdentity
                .builder()
                .setPrincipal(new QuarkusPrincipal(request.getUsername()))
                .build());

        // Disabled to avoid "NOISE", as a problem still happens without hibernate
        // return context.runBlocking(() -> {
        //     // Activate request context for hibernate
        //     if (!Arc.container().requestContext().isActive()) {
        //         var requestContext = Arc.container().requestContext();
        //         requestContext.activate();
        //         try {
        //             return authenticate(request);
        //         } finally {
        //             requestContext.terminate();
        //         }
        //     }
        //     return authenticate(request);
        // });
    }

    protected SecurityIdentity authenticate(UsernamePasswordAuthenticationRequest request) {
        Optional<User> foundUser = userRepository.findByIdOptional(request.getUsername());
        if (foundUser.isEmpty()) {
            Log.debug("User not found in db");
            return null;
        }
        Log.infof("User %s trying to login ", foundUser.get().getName());
        User user = foundUser.get();
        if (!passwordMatches(new String(request.getPassword().getPassword()), user.getPassword())) {
            throw new AuthenticationFailedException();
        }
        return QuarkusSecurityIdentity
                .builder()
                .setPrincipal(new QuarkusPrincipal(user.getName()))
                .addCredential(request.getPassword())
                .build();
    }

    protected boolean passwordMatches(String plainText, String hashedPassword) {
        try {
            return plainText.equals(hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
