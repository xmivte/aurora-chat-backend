package com.example.kns.repositories;

import com.example.kns.config.TestPostgresConfig;
import com.example.kns.entities.MockUser;
import com.example.kns.repository.HelloRepository;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest // only used for loading mappers, no controllers, services ...
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // no auto datasource application from springboot
@Import(TestPostgresConfig.class)
@TestPropertySource("classpath:application-repo.yml")
class HelloRepositoryTest {

    @Autowired
    HelloRepository repository;

    @Autowired
    DataSource dataSource;

    @Test
    void saveAndLoadMockUser_WithValidUser_ReturnsPersistedUser() {
        MockUser user = new MockUser(null, "a", "a@example.com");
        repository.save(user);

        MockUser loadedUser = repository.findById(user.getId());
        assertThat(loadedUser.getUsername()).isEqualTo("a");
    }

    // displays the jdbc url used by the embedded database
    @Test
    void showUrl_PrintsJdbcUrl() throws Exception {
        String url = dataSource.getConnection().getMetaData().getURL();
        System.out.println("Using jdbc url: " + url);
    }

    @Test
    void checkEmbeddedDb_UsesEmbeddedPostgres() throws Exception {
        String url = dataSource.getConnection().getMetaData().getURL();
        assertThat(url).doesNotContain("5455"); // not the real DB
        assertThat(url).matches(".*:\\d{4,5}.*"); // random port
        assertThat(url).contains("localhost");
    }
}
