package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.user.LoginRequest;
import ftn.siit.nvt.dto.user.LoginResponse;
import ftn.siit.nvt.dto.user.RegisterRequest;
import ftn.siit.nvt.dto.user.UserDTO;
import ftn.siit.nvt.exception.AccountLockedException;
import ftn.siit.nvt.exception.AccountNotVerifiedException;
import ftn.siit.nvt.exception.InvalidCredentialsException;
import ftn.siit.nvt.model.User;
import ftn.siit.nvt.security.TokenUtils;
import ftn.siit.nvt.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenUtils tokenUtils;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthService authService, TokenUtils tokenUtils, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.tokenUtils = tokenUtils;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> createAuthenticationToken(
            @RequestBody LoginRequest authenticationRequest) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()
                    )
            );
        } catch (org.springframework.security.authentication.DisabledException ex) {
            throw new AccountNotVerifiedException("Account not verified. Check your email for verification link.");
        } catch (org.springframework.security.authentication.LockedException ex) {
            throw new AccountLockedException("Account is locked. Contact support.");
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid email or password.");
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new InvalidCredentialsException("Authentication failed.", ex);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        String jwt = tokenUtils.generateToken(user);
        int expiresIn = tokenUtils.getExpiresIn();

        LoginResponse loginResponse = authService.buildLoginResponse(
                authenticationRequest.getEmail(),
                user,
                jwt,
                expiresIn
        );

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> register(@ModelAttribute @Valid RegisterRequest request) throws IOException {

        UserDTO newUser = authService.registerCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(newUser);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) throws URISyntaxException {
        authService.verifyAccount(token);
        URI newUri = new URI("http://localhost/login?verified=true");
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(newUri);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}