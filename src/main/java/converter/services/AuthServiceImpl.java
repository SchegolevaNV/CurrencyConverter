package converter.services;

import converter.api.responses.ResponsePlatformApi;
import converter.model.User;
import converter.repositories.UserRepository;
import converter.services.interfaces.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;

    @Value("${session.attribute}")
    private String attribute;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public void userCreation () {

        String login = "petr";
        if (userRepository.findByLogin(login) == null) {
            userRepository.save(User.builder()
                    .login(login)
                    .password(bcryptEncoder.encode("password"))
                    .build());

            log.info("User: '{}' is created", login);
        }
        else log.info("User: '{}' is already exist", login);
    }

    @Override
    public ResponseEntity<ResponsePlatformApi> login(String login, String password) {

        User user = userRepository.findByLogin(login);

        if (user != null && bcryptEncoder.matches(password, user.getPassword())) {
            HttpSession session = getSession();
            session.setAttribute(attribute, login);
            session.setMaxInactiveInterval(1800);

            log.info("Attribute for session was set to: '{}'", attribute);
        }

        return new ResponseEntity<>(ResponsePlatformApi.builder().message("OK").build(), HttpStatus.OK);
    }

    @Override
    public HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true);
    }

    @Override
    public boolean isUserAuthorize() {
        return getSession().getAttribute(attribute) != null;
    }

    public int getAuthorizedUserId() {
        Object login = getSession().getAttribute(attribute);
        return userRepository.findByLogin((String) login).getId();
    }
}
