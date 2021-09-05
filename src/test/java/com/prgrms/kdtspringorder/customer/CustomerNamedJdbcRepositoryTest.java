package com.prgrms.kdtspringorder.customer;

import com.wix.mysql.EmbeddedMysql;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.ScriptResolver.classPathScript;
import static com.wix.mysql.config.Charset.UTF8;
import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static com.wix.mysql.distribution.Version.v5_7_10;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringJUnitConfig
//setUp함수같은건 먼저 실행해야 하기 때문에 순서를 지정해준다.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //클래스 단위 생명주기를 가짐
class CustomerNamedJdbcRepositoryTest {
    private final Logger logger = LoggerFactory.getLogger(CustomerNamedJdbcRepositoryTest.class); //this.getClass()
    @Configuration
    @ComponentScan(basePackages = {"com.prgrms.kdtspringorder.customer"})
    static class Config {
        @Bean
        public DataSource dataSource(){
            var dataSource = DataSourceBuilder.create()
                    .url("jdbc:mysql://localhost:2215/test-order_mgmt") //test용
                    .username("test")
                    .password("test1234!")
                    .type(HikariDataSource.class)
                    .build();
            return dataSource;
        }
        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource){
            return new JdbcTemplate(dataSource);
        }

        @Bean
        public NamedParameterJdbcTemplate namedParameterJdbcTemplate(JdbcTemplate jdbcTemplate){
            return new NamedParameterJdbcTemplate(jdbcTemplate);
        }

        @Bean
        public PlatformTransactionManager platformTransactionManager(DataSource dataSource){
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public TransactionTemplate transactionTemplate(PlatformTransactionManager platformTransactionManager){
            return new TransactionTemplate(platformTransactionManager);
        }
    }

    @Autowired
    CustomerNamedJdbcRepository customerNamedJdbcRepository;

    @Autowired
    DataSource dataSource;

    Customer newCustomer;

    EmbeddedMysql embeddedMysql;
    @BeforeAll
    void setUp() {
        //LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS) //밀리초 길이 6자리 지정
        //newCustomer = new Customer(UUID.randomUUID(), "test-user2","test-user2@gmail.com", LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        newCustomer = new Customer(UUID.randomUUID(), "test-user2@gmail.com", "test-user2", LocalDateTime.now().withNano(0)); //embeded test
        var mysqldConfig = aMysqldConfig(v5_7_10) //port, characterset을 지정해 줄 수 있음
                .withCharset(UTF8)
                .withPort(2215) //임의의 포트 지정
                .withUser("test", "test1234!")
                .withTimeZone("Asia/Seoul")
                .build();

        embeddedMysql = anEmbeddedMysql(mysqldConfig)
                .addSchema("test-order_mgmt", classPathScript("schema.sql"))
                .start();
//        customerJdbcRepository.deleteAll();
    }

    @AfterAll
    void cleanUp(){
        embeddedMysql.stop();
    }


    @Test
    @Order(1)
    @DisplayName("Hikari Connectionpool test")
    public void testHikariConnectionPool() {
        //given
         assertThat(dataSource.getClass().getName(), is("com.zaxxer.hikari.HikariDataSource"));
        //when

        //then
    }


    @Test
    @Order(2)
    @DisplayName("고객을 추가할 수 있다.")
    public void testInsert() {
        try{
            customerNamedJdbcRepository.insert(newCustomer);
        } catch (BadSqlGrammarException e) {
            logger.error("Got BadSqlGrammarException error code -> {}",e.getSQLException().getErrorCode());


        }
        logger.info("customId -> {} email -> {} createdAt ->{}", newCustomer.getCustomerId(), newCustomer.getEmail(), newCustomer.getCreatedAt());

        var retrievedCustomer = customerNamedJdbcRepository.findById(newCustomer.getCustomerId());
        logger.info("customId -> {} email -> {} createdAt ->{}", retrievedCustomer.get().getCustomerId(), retrievedCustomer.get().getEmail(), retrievedCustomer.get().getCreatedAt());
        assertThat(retrievedCustomer.isEmpty(), is(false));
        assertThat(retrievedCustomer.get(),samePropertyValuesAs(newCustomer));//프로퍼티 value들이 같은지 비교
    }

    @Test
    @Order(3)
    @DisplayName("전체 고객을 조회할 수 있다.")
    public void testFindAll() {
         var customers = customerNamedJdbcRepository.findAll();
         assertThat(customers.isEmpty(), is(false));
        //Thread.sleep(5000);
    }

    @Test
    @Order(4)
    @DisplayName("이름으로 고객을 조회할 수 있다.")
    public void testFindByName() {
        var customer = customerNamedJdbcRepository.findByName(newCustomer.getName());
        assertThat(customer.isEmpty(), is(false));

        var unknown = customerNamedJdbcRepository.findByName("unknown-user");
        assertThat(unknown.isEmpty(), is(true));
    }

    @Test
    @Order(5)
    @DisplayName("이메일로로 고객 조회할 수 있다.")
    public void testFindByEmail() {
        var customer = customerNamedJdbcRepository.findByEmail(newCustomer.getEmail());
        assertThat(customer.isEmpty(), is(false));

        var unknown = customerNamedJdbcRepository.findByName("unknown-user@gmail.com");
        assertThat(unknown.isEmpty(), is(true));
    }

    @Test
    @Order(6)
    @DisplayName("고객을 수정할 수 있다.")
    public void testUpdate() {
        newCustomer.changeName("updated-user");
        customerNamedJdbcRepository.update(newCustomer);

        List<Customer> all = customerNamedJdbcRepository.findAll();
        assertThat(all,hasSize(1));
        assertThat(all,everyItem(samePropertyValuesAs(newCustomer)));

        Optional<Customer> retrievedCustomer = customerNamedJdbcRepository.findById(newCustomer.getCustomerId());
        assertThat(retrievedCustomer.isEmpty(), is(false));
        assertThat(retrievedCustomer.get(),samePropertyValuesAs(newCustomer));
    }

//    @Test
//    @Order(7)
//    @DisplayName("트랜잭션 테스트")
//    public void testTransaction() {
//        var prevOne = customerNamedJdbcRepository.findById(newCustomer.getCustomerId());
//        assertThat(prevOne.isEmpty(),is(false));
//
//        var newOne = new Customer(UUID.randomUUID(), "a@email.com", "a",LocalDateTime.now().withNano(0));
//        var insertedNewOne = customerNamedJdbcRepository.insert(newOne);
//
//        try{
//            customerNamedJdbcRepository.testTransaction(
//                    new Customer(
//                            newOne.getCustomerId(),
//                            prevOne.get().getEmail(),
//                            "b",
//                            newOne.getCreatedAt()));
//        }catch (DataAccessException e){
//            logger.error("Got error when testing transaction,e");
//        }
//
//        var maybeNewOne = customerNamedJdbcRepository.findById(insertedNewOne.getCustomerId());
//        assertThat(maybeNewOne.isEmpty(), is(false));
//        assertThat(maybeNewOne.get(), samePropertyValuesAs(newOne));
//    }




}