package com.hoangha.medical_chatbot.module.auth.service;

import com.hoangha.medical_chatbot.infrastructure.redis.RedisService;
import com.hoangha.medical_chatbot.module.auth.mapper.AuthMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.hoangha.medical_chatbot.module.auth.dto.request.*;
import com.hoangha.medical_chatbot.module.auth.dto.response.*;
import com.hoangha.medical_chatbot.module.user.entity.Role;
import com.hoangha.medical_chatbot.module.user.entity.User;
import com.hoangha.medical_chatbot.exception.AppException;
import com.hoangha.medical_chatbot.exception.ErrorCode;
import com.hoangha.medical_chatbot.module.user.repository.UserRepository;
// Nhớ import class RedisService của bạn
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    private final AuthMapper authMapper;

    @Value("${jwt.signerKey}")
    protected String SECRET_KEY;

    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    // Định nghĩa tiền tố để Redis gọn gàng
    private static final String REDIS_BLACKLIST_PREFIX = "jwt_blacklist:";

    public IntrospectResponse introspect(IntrospectRequest request) {
        boolean isValid = true;
        try {
            verifyToken(request.getToken(), false);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER) // Đăng ký mặc định là quyền USER
                .isActive(true)
                .build();

        user = userRepository.save(user);
        String token = generateToken(user);

        return authMapper.toAuthResponse(user,token, true);
    }

    public AuthenticationResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Block chặn user đã bị de-active (Xóa mềm)
        if (!user.isActive()) {
            throw new AppException(ErrorCode.USER_DISABLED);
        }

        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!isAuthenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = generateToken(user);

        return authMapper.toAuthResponse(user, token, true);
    }

    public void logout(LogoutRequest request) {
        try {
            var signedToken = verifyToken(request.getToken(), true);
            String jti = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

            long ttl = (expiryTime.getTime() - System.currentTimeMillis()) / 1000;

            if (ttl > 0) {
                redisService.setValue(REDIS_BLACKLIST_PREFIX + jti, "invalidated", ttl, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.info("Invalid Token during logout: {}", e.getMessage());
        }
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        var signedToken = verifyToken(request.getToken(), true);
        String jti = signedToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

        long ttl = (expiryTime.getTime() - System.currentTimeMillis()) / 1000;

        if (ttl > 0) {
            redisService.setValue(REDIS_BLACKLIST_PREFIX + jti, "invalidated", ttl, TimeUnit.SECONDS);
        }

        String username = signedToken.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!user.isActive()) {
            throw new AppException(ErrorCode.USER_DISABLED);
        }

        String newToken = generateToken(user);
        return authMapper.toAuthResponse(user, newToken, true);
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = isRefresh
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean isValid = signedJWT.verify(verifier);

        if (!(isValid && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Luôn check kèm Prefix khi kiểm tra Blacklist
        if (redisService.getValue(REDIS_BLACKLIST_PREFIX + signedJWT.getJWTClaimsSet().getJWTID()) != null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    public String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("userId", user.getId())
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        return jwsObject.serialize();
    }

    private String buildScope(User user) {
        return "ROLE_" + user.getRole().name();
    }
}