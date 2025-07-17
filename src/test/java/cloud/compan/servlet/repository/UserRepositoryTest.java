package cloud.compan.servlet.repository;

import cloud.compan.servlet.config.AppConfigProvider;
import cloud.compan.servlet.config.AppModule;
import cloud.compan.servlet.config.GuiceDataSourceProvider;
import cloud.compan.servlet.model.User;
import cloud.compan.servlet.utils.HashUtil;
import cloud.compan.servlet.utils.JdbcExecutor;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.Key;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRepositoryTest {

    private static Injector injector;
    private UserRepository userRepository;

    // 自定义一个用于测试的 Guice 模块
    static class TestDatabaseModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(AppModule.class).toProvider(AppConfigProvider.class).in(Singleton.class);
            bind(DataSource.class).annotatedWith(Names.named("")).toInstance(createTestDataSource());
            bind(GuiceDataSourceProvider.class);
            bind(JdbcExecutor.class);
            bind(HashUtil.class);
            bind(UserRepository.class);
        }

        private DataSource createTestDataSource() {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL");
            config.setUsername("sa");
            config.setPassword("");
            return new HikariDataSource(config);
        }
    }

    @BeforeAll
    public void setUpClass() {
        injector = Guice.createInjector(new TestDatabaseModule());
        this.userRepository = injector.getInstance(UserRepository.class);
    }

    @BeforeEach
    public void setUp() throws Exception {
        // 在每个测试之前，运行 test1.sql 来创建表
        executeSqlScript("test1.sql");
    }

    @AfterEach
    public void tearDown() throws Exception {
        // 在每个测试之后，清空数据库
        try (Connection conn = injector.getInstance(Key.get(DataSource.class, Names.named(""))).getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
    }

    private void executeSqlScript(String scriptName) throws IOException, SQLException {
        DataSource ds = injector.getInstance(Key.get(DataSource.class, Names.named(""))); 
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             InputStream in = getClass().getClassLoader().getResourceAsStream(scriptName);
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            
            StringBuilder sql = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sql.append(buffer, 0, read);
            }
            stmt.execute(sql.toString());
        }
    }

    @Test
    void testSaveAndFindById() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getUserId(), "Saved user should have an ID");

        Optional<User> foundUserOpt = userRepository.findById(savedUser.getUserId());
        assertTrue(foundUserOpt.isPresent(), "User should be found by ID");
        assertEquals("testuser", foundUserOpt.get().getUsername());
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setUsername("updateuser");
        user.setEmail("update@example.com");
        user.setPassword("password");
        user = userRepository.save(user);

        user.setEmail("updated@example.com");
        userRepository.save(user);

        Optional<User> updatedUserOpt = userRepository.findById(user.getUserId());
        assertTrue(updatedUserOpt.isPresent());
        assertEquals("updated@example.com", updatedUserOpt.get().getEmail());
    }

    @Test
    void findByUsername_ShouldReturnUser() {
        User user = new User();
        user.setUsername("findme");
        user.setEmail("findme@example.com");
        user.setPassword("password");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("findme");
        assertTrue(foundUser.isPresent(), "User should be found by username");
        assertEquals("findme", foundUser.get().getUsername());
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        User user = new User();
        user.setUsername("deleteme");
        user.setEmail("deleteme@example.com");
        user.setPassword("password");
        user = userRepository.save(user);

        Long userId = user.getUserId();
        userRepository.deleteById(userId);

        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent(), "User should not be found after deletion");
    }
} 