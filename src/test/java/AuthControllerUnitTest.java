import converter.api.requests.AuthRequestBody;
import converter.controller.AuthController;
import converter.services.interfaces.AuthService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class AuthControllerUnitTest {

    @Mock
    private AuthService service;
    private MockMvc mockMvc;

    @Mock
    AuthRequestBody request;

    @InjectMocks
    AuthController controller;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        controller = new AuthController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        request = AuthRequestBody.builder().login("petr").password("password").build();
    }

    @Test
    public void loginTest() throws Exception {
        controller.login(request);
        verify(service).login(request.getLogin(), request.getPassword());

        String correctUser = "{\"login\": \"petr\", \"password\" : \"password\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .content(correctUser).contentType(MediaType.APPLICATION_JSON)
                .sessionAttr("login", request.getLogin()))
                .andExpect(status().isOk());
    }
}
