package com.prgrms.kdtspringorder.customer;

import com.wix.mysql.EmbeddedMysql;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
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
class CustomerServiceTest {
    private final Logger logger = LoggerFactory.getLogger(CustomerServiceTest.class); //this.getClass()
    @Configuration
//    @ComponentScan(basePackages = {"com.prgrms.kdtspringorder.customer"}) //빈으로넣으면 이건 안해도 됨
    @EnableTransactionManagement //aop 관련 애노테이션
    static class Config {

        @Bean
        public DataSource dataSource(){
            var dataSource = DataSourceBuilder.create()
                    .url("jdbc:mysql://localhost:2215/test-order_mgmt") //test용
                    .username("test")
                    .password("test1234!")
                    .type(HikariDataSource.class)
                    .build();
            dataSource.setMaximumPoolSize(1000);
            dataSource.setMinimumIdle(100);
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

        @Bean
        public CustomerRepository customerRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate){
            return new CustomerNamedJdbcRepository(namedParameterJdbcTemplate);
        }

        @Bean
        public CustomerService customerService(CustomerRepository customerRepository){
            return new CustomerServiceImpl(customerRepository);
        }
    }

    static EmbeddedMysql embeddedMysql;

    @BeforeAll
    static void setUp() {
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
    static void cleanUp(){
        embeddedMysql.stop();
    }

    //각 테스트코드마다 데이터가 중복되는 문제 방지
    @AfterEach
    void dataCleanUp(){
        customerRepository.deleteAll();
    }

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerRepository customerRepository;




    @Test
    @DisplayName("멀티 추가 테스트")
    public void multiInsertTest() {
        //given
        var customers = List.of(
                new Customer(UUID.randomUUID(), "a", "a@gmail.com", LocalDateTime.now()),
                new Customer(UUID.randomUUID(), "b", "b@gmail.com", LocalDateTime.now())
        );
        //when
        customerService.createCustomers(customers);
        List allCustomersRetrived = customerRepository.findAll();

        logger.info(customers.toString());
        logger.info(allCustomersRetrived.toString());

        //then
        assertThat(allCustomersRetrived.size(), is(2));
//        assertThat(allCustomersRetrived, containsInAnyOrder(samePropertyValuesAs(customers.get(0)), samePropertyValuesAs(customers.get(1))));
        assertThat((List<Object>) allCustomersRetrived, containsInAnyOrder(
                hasProperty("customerId",is(customers.get(0).getCustomerId())),
                hasProperty("customerId",is(customers.get(1).getCustomerId()))
        ));
    }


    @Test
    @DisplayName("멀티 추가 실패시 전체 트랜잭션이 롤백되야 한다.")
    public void multiInsertRollbackTest() {
        //given
        var customers = List.of(
                new Customer(UUID.randomUUID(), "c", "c@gmail.com", LocalDateTime.now()),
                new Customer(UUID.randomUUID(), "d", "c@gmail.com", LocalDateTime.now())
        );
        //when
        try{
            customerService.createCustomers(customers);
        } catch (DataAccessException e) {
        }
        List allCustomersRetrived = customerRepository.findAll();
        //then
        assertThat(allCustomersRetrived.size(), is(0));
        assertThat(allCustomersRetrived.isEmpty(), is(true));
        assertThat((List<Object>) allCustomersRetrived, not(containsInAnyOrder(
                hasProperty("customerId",is(customers.get(0).getCustomerId())),
                hasProperty("customerId",is(customers.get(1).getCustomerId()))
        )));
    }



}