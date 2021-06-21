package me.gimun.documentapproval.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.gimun.documentapproval.config.InitUserConfig;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/*
 * @WebMvcTest
 * MockMvc 빈을 자동 설정 해준다. 따라서 그냥 가져와서 쓰면 됨.
 * 웹 관련 빈만 등록해 준다. (슬라이스)
 * 리파지토리 빈으로 등록해주지 않음.
 * */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc // 모킹을 사용하지않고 실제 리파지토리를 사용하여 테스트 동작
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class, InitUserConfig.class}) //다른 스프링 bean 설정파일을 읽어와서 사용하는 방법 중 하나
@Ignore
public class BaseControllerTest {
    /*
     * MockMvc는 요청을만들고 응답을 검증할수있는 스프링MVC 테스트에 있어서 핵심적인 클래스 중 하나.
     * 웹 서버를 띄우지 않고도 스프링 MVC (DispatcherServlet)가 요청을 처리하는 과정을 확인할 수 있기 때문에 컨트롤러 테스트용으로 자주 쓰임.
     * 디스패처서블릿을 만들어야하기때문에 단위테스트보다는 느림.
     * */

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;


}
