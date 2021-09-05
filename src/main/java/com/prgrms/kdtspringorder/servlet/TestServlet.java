package com.prgrms.kdtspringorder.servlet;

import com.prgrms.kdtspringorder.JdbcCustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

//@WebServlet(value = "/*", loadOnStartup = 1) //에너테이션으로 지정하는 방법. was를 실행하는 순간 미리 로드 시키겠다. (요청을 하는 순간 init이 되는게 아니라). default는 -1.
public class TestServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(TestServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("Init Servlet");
    }

    @Override //컨테이너가 servlet을 요청해오면 container가 Servlet을 만들고 알아서 서비스를 호출하고 doGet이 호출됨. 웹 컨테이너에 테스트서블릿이 있다는걸 알려줘야함
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { //메서드단위로 Get, Post등 있음
        var requestURI = req.getRequestURI();
        logger.info("Get Request from {}",requestURI);

        var writer = resp.getWriter();
        writer.println("Hello Servlet!");
    }

}
