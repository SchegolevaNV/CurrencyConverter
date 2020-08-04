package converter.controller;

import converter.api.requests.AuthRequestBody;
import converter.api.responses.ResponsePlatformApi;
import converter.services.interfaces.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth/")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping ("login")
    public ResponseEntity<ResponsePlatformApi> login(@RequestBody AuthRequestBody authRequestBody) {
        return authService.login(authRequestBody.getLogin(), authRequestBody.getPassword());
    }
}
