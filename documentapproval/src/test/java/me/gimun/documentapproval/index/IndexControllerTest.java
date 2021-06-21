package me.gimun.documentapproval.index;

import me.gimun.documentapproval.common.BaseControllerTest;
import me.gimun.documentapproval.common.TestDescription;
import org.junit.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IndexControllerTest extends BaseControllerTest {

    @Test
    @WithMockUser(username = "username", roles = "USER")
    @TestDescription("API 의 진입점을 통해 리소스를 제공")
    public void index() throws Exception {
        this.mockMvc.perform(get("/api/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.documents").exists())
                .andDo(document("index"
                        ,links(
                                linkWithRel("documents").description("문서 서비스 진입점 - 사용자 문서목록 조회")
                        )
                        ,relaxedResponseFields(
                                fieldWithPath("_links.documents").description("RestDoc에 대한 링크정보")
                        )
                ))
        ;
    }
}
