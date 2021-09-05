package com.prgrms.kdtspringorder;

import com.prgrms.kdtspringorder.customer.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JdbcCustomerRepository {

    private static final Logger logger = LoggerFactory.getLogger(JdbcCustomerRepository.class);
    private static final String SELECT_BY_NAME_SQL = "select * from customers where name = ?";
    private static final String SELECT_ALL_SQL = "select * from customers";
    private static final String INSERT_SQL = "insert into customers(customer_id, name, email) values (UUID_TO_BIN(?), ?, ?)";
    private static final String DELETE_ALL_SQL = "delete from customers";
    private static final String UPDATE_BY_ID_SQL = "update customers set name = ? where customer_id = UUID_TO_BIN(?)";

    public void transactionTest(Customer customer){ //spring jdbc template, spring transaction manager 사용하면 이렇게 복잡한 try-catch문을 쓰지 않아도 된다.(spring aop)
        String updateNameSql = "update customers set name = ? where customer_id = UUID_TO_BIN(?)";
        String updateEmailSql = "update customers set email = ? where customer_id = UUID_TO_BIN(?)";
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:mysql://localhost:/order_mgmt", "root", "root1234!");
            connection.setAutoCommit(false);//트랜잭션 시작
            try (
                    var updateNameStatement = connection.prepareStatement(updateNameSql);
                    var updateEmailStatement = connection.prepareStatement(updateEmailSql);
            ) {
                updateNameStatement.setString(1,customer.getName());
                updateNameStatement.setBytes(2,customer.getCustomerId().toString().getBytes());
                updateNameStatement.executeUpdate();

                updateEmailStatement.setString(1,customer.getEmail());
                updateEmailStatement.setBytes(2,customer.getCustomerId().toString().getBytes());
                updateEmailStatement.executeUpdate();
                connection.setAutoCommit(true);//트랜잭션 종료
            }
        } catch (SQLException exception) {
            if(connection!=null){
                try {
                    connection.rollback();
                    connection.close();
                } catch (SQLException throwable) {
                    logger.error("Got error while closing connection",throwable);
                    throw new RuntimeException(exception);
                }
            }
            logger.error("Got error while closing connection",exception);
            throw new RuntimeException(exception);
        }
    }
    public static void main(String[] args) {

        var jdbcCustomerRepository = new JdbcCustomerRepository();
        jdbcCustomerRepository.transactionTest(
                new Customer(UUID.fromString("242693c1-6f34-44e5-8c5a-30687fcd3741"), "update-user", "neww-user@gmail.com", LocalDateTime.now()));


        /*var jdbcCustomerRepository = new JdbcCustomerRepository();
        UUID customerId = UUID.randomUUID();
        jdbcCustomerRepository.insertCustomer(customerId, "neww-user", "neww-user@gmail.com");*/

        /*
        //List<String> names = new JdbcCustomerRepository().findNames("tester01' OR 'a'='a"); //sql injection
        //해결책으로 preparedSatement를 사용
        var jdbcCustomerRepository = new JdbcCustomerRepository();

        int count = jdbcCustomerRepository.deleteAllCustomers();
        logger.info("deleted count -> {}",count);

        UUID customerId = UUID.randomUUID();
        logger.info("create customerId -> {} and version -> {}",customerId,customerId.version());
        jdbcCustomerRepository.insertCustomer(customerId, "new-user", "new-user@gmail.com");

        jdbcCustomerRepository.findAllIds().forEach(v -> logger.info("Found name : {} and version : {}",v, v.version()));
        //서로 다른 id값이 리턴됨! >> 만든 값이 제대로 들어갔지만, 조회할 때는 다른 값이 리턴됨
        //id를 getBytes 하는 부분이 잘못됨. 만든 uuid의 버전 = 4, 받은 uuid의버전 = 3
*/
    }
    public int deleteAllCustomers(){
        try (
                var connection = DriverManager.getConnection("jdbc:mysql://localhost:/order_mgmt", "root", "root1234!");
                var statement = connection.prepareStatement(DELETE_ALL_SQL);

        ) {
            return statement.executeUpdate();

        }catch (SQLException throwable) {
            logger.error("Got error while closing connection",throwable);
        }
        return 0;
    }

    public int updateCustomerName(UUID customerId, String name){
        try (
                var connection = DriverManager.getConnection("jdbc:mysql://localhost:/order_mgmt", "root", "root1234!");
                var statement = connection.prepareStatement(UPDATE_BY_ID_SQL);

        ) {
            statement.setString(1,name);
            statement.setBytes(2,customerId.toString().getBytes());
            return statement.executeUpdate();

        }catch (SQLException throwable) {
            logger.error("Got error while closing connection",throwable);
        }
        return 0;
    }


    public int insertCustomer(UUID customerId, String name, String email){
        try (
                var connection = DriverManager.getConnection("jdbc:mysql://localhost:/order_mgmt", "root", "root1234!");
                var statement = connection.prepareStatement(INSERT_SQL);

        ) {
            statement.setBytes(1,customerId.toString().getBytes());
            statement.setString(2,name);
            statement.setString(3,email);
            return statement.executeUpdate();

        }catch (SQLException throwable) {
            logger.error("Got error while closing connection",throwable);
        }
        return 0;
    }

    public List<String> findName(String name){
//        var SELECT_SQL = "select * from customers where name = '%s' ".formatted(name); //createSatement()
//        var SELECT_SQL = "select * from customers where name = ?"; //preparedStatement()
        List<String> names = new ArrayList<>();

        try (
                //AutoCloseable을 extend하고 있는데, 이걸 구현한 객체는 자동으로 close됨
                //코드상에 id, pw가 들어가게 하면 안됨
                var connection = DriverManager.getConnection("jdbc:mysql://localhost:/order_mgmt", "root", "root1234!");
//                var statement = connection.createStatement();
//                var resultSet = statement.executeQuery("select * from customers");
//                var resultSet = statement.executeQuery(SELECT_SQL);
                var statement = connection.prepareStatement(SELECT_BY_NAME_SQL);

        ) {
            statement.setString(1,name); //파라미터 인덱스의 순서를 적어줘야 됨(? 자리)
            logger.info("statement -> {}",statement);
            try(var resultSet = statement.executeQuery();){
                while(resultSet.next()){
                    var customerName = resultSet.getString("name");
                    var customerId = UUID.nameUUIDFromBytes(resultSet.getBytes("customer_id"));
                    //createdAt처럼 null이 들어갈 수 있는 객체들을 주의해서 작성하기
                    var createdAt = resultSet.getTimestamp("created_at").toLocalDateTime(); //DATE 타입으로 바꿔주는게 좋음
                    names.add(customerName);
                }
            }catch (SQLException throwable){
                logger.error("Got error while closing connection",throwable);
            }

        }catch (SQLException throwable) {
            logger.error("Got error while closing connection",throwable);
        }
        //final을 안써도 됨
        return names;
    }

    public List<String> findAllNames(){
        List<String> names = new ArrayList<>();

        try (
                var connection = DriverManager.getConnection("jdbc:mysql://localhost:/order_mgmt", "root", "root1234!");
                var statement = connection.prepareStatement(SELECT_ALL_SQL);
                var resultSet = statement.executeQuery()

        ) {
            while(resultSet.next()){
                var customerName = resultSet.getString("name");
                var customerId = UUID.nameUUIDFromBytes(resultSet.getBytes("customer_id"));
                var createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();

//                logger.info("customer id -> {}, name -> {}, createdAt -> {}",customerId, customerName, createdAt);
                names.add(customerName);
            }
        }catch (SQLException throwable) {
            logger.error("Got error while closing connection",throwable);
        }
        return names;
    }


    public List<UUID> findAllIds(){
        List<UUID> ids = new ArrayList<>();

        try (
                var connection = DriverManager.getConnection("jdbc:mysql://localhost:/order_mgmt", "root", "root1234!");
                var statement = connection.prepareStatement(SELECT_ALL_SQL);
                var resultSet = statement.executeQuery()

        ) {
            while(resultSet.next()){
                var customerName = resultSet.getString("name");
//                var customerId = UUID.nameUUIDFromBytes(resultSet.getBytes("customer_id"));
                var customerId = toUUID(resultSet.getBytes("customer_id"));
                var createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();

//                logger.info("customer id -> {}, name -> {}, createdAt -> {}",customerId, customerName, createdAt);
                ids.add(customerId);
            }
        }catch (SQLException throwable) {
            logger.error("Got error while closing connection",throwable);
        }
        return ids;
    }

    //이런 함수는 나중에 Utils에 모아두면 됨
    static UUID toUUID(byte[] bytes){
        var byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());//한번 쪼개서 각각 64비트 씩 가져오는 방법으로 버전 차이를 우회한다.
    }


}
