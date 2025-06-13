package ru.stayyhydratedd.wbot.ShiftSheet.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.models.Role;
import ru.stayyhydratedd.wbot.ShiftSheet.models.User;
import ru.stayyhydratedd.wbot.ShiftSheet.security.MyUserDetails;
import ru.stayyhydratedd.wbot.ShiftSheet.services.RoleService;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JWTUtil {

    private final RoleService roleService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    private SecretKey key;

    @PostConstruct
    private void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {

        MyUserDetails userDetails = new MyUserDetails(user);

        return Jwts.builder()
                .subject("User details")
                .claim("username", userDetails.getUsername())
                .claim("hash", userDetails.getPassword())
                .claim("roles", userDetails.getAuthorities())
                .issuer(issuer)
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .requireSubject("User details")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public User extractUser(String token){
        Claims claims = extractClaims(token);
        String username = claims.get("username", String.class);
        String hash = claims.get("hash", String.class);
        Set<?> rolesRaw = claims.get("roles", Set.class);
        Set<Role> roles = rolesRaw.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(roleName -> roleService.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        return new User(username, hash, roles);
    }
}
