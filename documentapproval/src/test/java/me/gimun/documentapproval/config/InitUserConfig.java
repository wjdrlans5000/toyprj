package me.gimun.documentapproval.config;

import me.gimun.documentapproval.accounts.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@TestConfiguration
public class InitUserConfig {

    @Bean
    public ApplicationRunner initUser() {
        return new InitUser();
    }

    class InitUser implements ApplicationRunner {

        @Autowired
        EntityManager em;

        @Override
        @Transactional
        public void run(ApplicationArguments args) throws Exception {
            em.persist(new Account("gimun@naver.com","1234"));
            em.flush();
            em.clear();
        }
    }
}
