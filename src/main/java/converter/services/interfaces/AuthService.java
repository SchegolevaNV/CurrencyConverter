package converter.services.interfaces;

import converter.api.responses.ResponsePlatformApi;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpSession;

public interface AuthService {

    ResponseEntity<ResponsePlatformApi> login (String login, String password);

    HttpSession getSession();
    boolean isUserAuthorize();
    int getAuthorizedUserId();
}
