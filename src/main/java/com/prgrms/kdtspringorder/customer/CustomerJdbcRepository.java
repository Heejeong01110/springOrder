package com.prgrms.kdtspringorder.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CustomerJdbcRepository implements CustomerRepository {

    private final Logger logger = LoggerFactory.getLogger(CustomerJdbcRepository.class); //this.getClass()
    private final JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;
    private static final RowMapper<Customer>  customerRowMapper = (resultSet, i) -> {
        var customerName = resultSet.getString("name");
        var customerEmail = resultSet.getString("email");
        var customerId = toUUID(resultSet.getBytes("customer_id"));
        var lastLoginAt = resultSet.getTimestamp("last_login_at") != null ?
                resultSet.getTimestamp("last_login_at").toLocalDateTime() : null;
        var createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
        return new Customer(customerId, customerName, customerEmail, lastLoginAt, createdAt);
    };

    public CustomerJdbcRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public Customer insert(Customer customer) {
        //int update = jdbcTemplate.update("insert into customers(customer_id, name, email, created_at) values (UUID_TO_BIN(?), ?, ?, ?)",
        int update = jdbcTemplate.update("insert into customers(customer_id, name, email, created_at) values (UNHEX(REPLACE(?,'-','')), ?, ?, ?)",
                customer.getCustomerId().toString().getBytes(),
                customer.getName(),
                customer.getEmail(),
                Timestamp.valueOf(customer.getCreatedAt()));
        if(update != 1){
            throw new RuntimeException("Nothing was insert");
        }
        return customer;
/*
        try (
                var connection = dataSource.getConnection();
                var statement = connection.prepareStatement("insert into customers(customer_id, name, email, created_at) values (UUID_TO_BIN(?), ?, ?, ?)");
        ) {
            statement.setBytes(1,customer.getCustomerId().toString().getBytes());
            statement.setString(2,customer.getName());
            statement.setString(3,customer.getEmail());
            statement.setTimestamp(4, Timestamp.valueOf(customer.getCreatedAt()));

            int executeUpdate = statement.executeUpdate();
            if(executeUpdate != 1){
                throw new RuntimeException("Nothing was insert");
            }
            return customer;
        }catch (SQLException throwable) {
            logger.error("Got error while closing connection", throwable);
            throw new RuntimeException(throwable);
        }*/
    }

    @Override
    public Customer update(Customer customer) {
        int update = jdbcTemplate.update("update customers set name = ?, email = ?, last_login_at = ? where customer_id = UNHEX(REPLACE(?,'-',''))",
                customer.getName(),
                customer.getEmail(),
                customer.getLastLoginAt() != null ? Timestamp.valueOf(customer.getLastLoginAt()) : null,
                customer.getCustomerId().toString().getBytes());

        if(update != 1){
            throw new RuntimeException("Nothing was updated");
        }
        return customer;
        /*
        try (
                var connection = dataSource.getConnection();
                var statement = connection.prepareStatement("update customers set name = ?, email = ?, last_login_at = ? where customer_id = UUID_TO_BIN(?)");

        ) {
            statement.setString(1,customer.getName());
            statement.setString(2, customer.getEmail());
            statement.setTimestamp(3, customer.getLastLoginAt()!= null ? Timestamp.valueOf(customer.getLastLoginAt()):null);
            statement.setBytes(4,customer.getCustomerId().toString().getBytes());
            var executeUpdate = statement.executeUpdate();
            if(executeUpdate != 1){
                throw new RuntimeException("Nothing was updated");
            }
            return customer;

        }catch (SQLException throwable) {
            logger.error("Got error while closing connection",throwable);
            throw new RuntimeException(throwable);
        }*/
    }

    @Override
    public int count() {
        return jdbcTemplate.queryForObject("select count(*) from customers", Integer.class);
        //결과값이 1개일 때 queryForObject 사용
    }

    @Override
    public List<Customer> findAll() {
         return jdbcTemplate.query("select * from customers", customerRowMapper);
        /*List<Customer> allCustomers = new ArrayList<>();
        try (
                var connection = dataSource.getConnection();
                var statement = connection.prepareStatement("select * from customers");
                var resultSet = statement.executeQuery()

        ) {
            while (resultSet.next()) {
                mapToCustomer(allCustomers, resultSet);
            }
        } catch (SQLException throwable) {
            logger.error("Got error while closing connection", throwable);
            throw new RuntimeException(throwable);
        }
        return allCustomers;*/

    }

    @Override
    public Optional<Customer> findById(UUID customerId) {
        try{
            return Optional.ofNullable(jdbcTemplate.queryForObject("select * from customers where customer_id = UNHEX(REPLACE(?,'-',''))",
                    customerRowMapper,
                    customerId.toString().getBytes()));
        } catch (EmptyResultDataAccessException e){
            logger.error("Got empty result",e);
            return Optional.empty();
        }

        /*List<Customer> allCustomers = new ArrayList<>();

        try (
                var connection = dataSource.getConnection();
                var statement = connection.prepareStatement("select * from customers where customer_id = UUID_TO_BIN(?)");
        ) {
            statement.setBytes(1,customerId.toString().getBytes()); //파라미터 인덱스의 순서를 적어줘야 됨(? 자리)
            try(var resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    mapToCustomer(allCustomers, resultSet);
                }
            }
        }catch (SQLException throwable) {
            logger.error("Got error while closing connection",throwable);
            throw new RuntimeException(throwable);
        }
        return allCustomers.stream().findFirst();*/
    }

    @Override
    public Optional<Customer> findByName(String name) {
        try{
            return Optional.ofNullable(jdbcTemplate.queryForObject("select * from customers where name = ?",
                    customerRowMapper,
                    name));
        } catch (EmptyResultDataAccessException e){
            logger.error("Got empty result",e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        try{
            return Optional.ofNullable(jdbcTemplate.queryForObject("select * from customers where email = ?",
                    customerRowMapper,
                    email));
        } catch (EmptyResultDataAccessException e){
            logger.error("Got empty result",e);
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("delete from customers");
/*
        try (
                var connection = dataSource.getConnection();
                var statement = connection.prepareStatement("delete from customers");
        ) {
            statement.executeUpdate();
        }catch (SQLException throwable) {
            logger.error("Got error while closing connection",throwable);
            throw new RuntimeException(throwable);
        }*/
    }
/*
    private void mapToCustomer(List<Customer> allCustomers, ResultSet resultSet) throws SQLException {
        var customerName = resultSet.getString("name");
        var customerEmail = resultSet.getString("email");
        var customerId = toUUID(resultSet.getBytes("customer_id"));
        var lastLoginAt = resultSet.getTimestamp("last_login_at") != null ?
                resultSet.getTimestamp("last_login_at").toLocalDateTime() : null;
        var createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
        allCustomers.add(new Customer(customerId,customerName,customerEmail,lastLoginAt,createdAt));
    }*/

    static UUID toUUID(byte[] bytes){
        var byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());//한번 쪼개서 각각 64비트 씩 가져오는 방법으로 버전 차이를 우회한다.
    }
}
