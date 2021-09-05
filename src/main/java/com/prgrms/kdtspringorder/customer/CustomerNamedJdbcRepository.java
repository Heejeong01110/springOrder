package com.prgrms.kdtspringorder.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.*;

@Repository
@Primary
public class CustomerNamedJdbcRepository implements CustomerRepository {

    private final Logger logger = LoggerFactory.getLogger(CustomerNamedJdbcRepository.class); //this.getClass()
    private final NamedParameterJdbcTemplate jdbcTemplate;

//    private final PlatformTransactionManager dataSourceTransactionManager;
//    private final TransactionTemplate transactionTemplate;

    private static final RowMapper<Customer> customerRowMapper = (resultSet, i) -> {
        var customerName = resultSet.getString("name");
        var customerEmail = resultSet.getString("email");
        var customerId = toUUID(resultSet.getBytes("customer_id"));
        var lastLoginAt = resultSet.getTimestamp("last_login_at") != null ?
                resultSet.getTimestamp("last_login_at").toLocalDateTime() : null;
        var createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
        return new Customer(customerId, customerName, customerEmail, lastLoginAt, createdAt);
    };

    private Map<String, Object> toParamMap(Customer customer){
        return new HashMap<String, Object>() {{
            put("customer_id", customer.getCustomerId().toString().getBytes());
            put("name", customer.getName());
            put("email", customer.getEmail());
            put("created_at", customer.getCreatedAt()); //안쓰는 인자가 들어있어도 상관 없음 --> 함수로 정의해두고 재사용
            put("last_login_at", customer.getLastLoginAt() != null ? Timestamp.valueOf(customer.getLastLoginAt()) : null);
        }};
    }

    public CustomerNamedJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate/*, TransactionTemplate transactionTemplate*/) {
        this.jdbcTemplate = jdbcTemplate;
//        this.dataSourceTransactionManager = dataSourceTransactionManager;
//        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Customer insert(Customer customer) {
        var update = jdbcTemplate.update("insert into customers(customer_id, name, email, created_at) values (UNHEX(REPLACE(:customer_id,'-','')), :name, :email, :created_at)",
                toParamMap(customer));
        if (update != 1) {
            throw new RuntimeException("Nothing was insert");
        }
        return customer;
    }



    @Override
    public Customer update(Customer customer) {
        var update = jdbcTemplate.update("update customers set name = :name, email = :email, last_login_at = :last_login_at where customer_id = UNHEX(REPLACE(:customer_id,'-',''))",
                toParamMap(customer));

        if (update != 1) {
            throw new RuntimeException("Nothing was updated");
        }
        return customer;
    }

    @Override
    public int count() {
        return jdbcTemplate.queryForObject("select count(*) from customers",Collections.emptyMap(), Integer.class);
    }

    @Override
    public List<Customer> findAll() {
        return jdbcTemplate.query("select * from customers", customerRowMapper);
    }

    @Override
    public Optional<Customer> findById(UUID customerId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("select * from customers where customer_id = UNHEX(REPLACE(:customer_id,'-',''))", //UUID_TO_BIN(:customer_id)
                    Collections.singletonMap("customer_id",customerId.toString().getBytes()),
                    customerRowMapper));
        } catch (EmptyResultDataAccessException e) {
            logger.error("Got empty result", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Customer> findByName(String name) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("select * from customers where name = :name",
                    Collections.singletonMap("name",name),
                    customerRowMapper));
        } catch (EmptyResultDataAccessException e) {
            logger.error("Got empty result", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("select * from customers where email = :email",
                    Collections.singletonMap("email",email),
                    customerRowMapper));
        } catch (EmptyResultDataAccessException e) {
            logger.error("Got empty result", e);
            return Optional.empty();
        }
    }

//    public void testTransaction(Customer customer){
//        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//            @Override
//            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) { //결과가 없는 sql 수행
//                jdbcTemplate.update("update customers set name = :name where customer_id = UNHEX(REPLACE(:customer_id,'-',''))", toParamMap(customer));
//                jdbcTemplate.update("update customers set email = :email where customer_id = UNHEX(REPLACE(:customer_id,'-',''))", toParamMap(customer));
//            }
//        });
//        /*var transaction = dataSourceTransactionManager.getTransaction(new DefaultTransactionDefinition());
//        try{
//            jdbcTemplate.update("update customers set name = :name where customer_id = UNHEX(REPLACE(:customer_id,'-',''))", toParamMap(customer));
//            jdbcTemplate.update("update customers set email = :email where customer_id = UNHEX(REPLACE(:customer_id,'-',''))", toParamMap(customer));
//            dataSourceTransactionManager.commit(transaction);
//        } catch (DataAccessException e) {
//            logger.error("Got error", e);
//            dataSourceTransactionManager.rollback(transaction);
//        }*/
//    }

    @Override
    public void deleteAll() {
//        jdbcTemplate.update("delete from customers",Collections.emptyMap());
//        jdbcTemplate.getJdbcTemplate().update("delete from customers"); //.getJdbcTemplate을 하면 기존 함수처럼 쓸 수 있다.
        jdbcTemplate.update("delete from customers",Collections.emptyMap());
    }

    static UUID toUUID(byte[] bytes) {
        var byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());//한번 쪼개서 각각 64비트 씩 가져오는 방법으로 버전 차이를 우회한다.
    }
}
