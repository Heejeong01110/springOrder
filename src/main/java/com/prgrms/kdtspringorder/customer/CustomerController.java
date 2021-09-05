package com.prgrms.kdtspringorder.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
//@CrossOrigin(origins = "*")
public class CustomerController {

    private final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

/*//    @RequestMapping(value = "/customers",method = RequestMethod.GET) //value 주소에서 GET 요청이 오면 이 함수가 실행된다. (mapping)
    @GetMapping("/customers") //윗 문장과 동일한 뜻
    public ModelAndView findCustomers(){
        var allCustomers = customerService.getAllCustomers();

        return new ModelAndView("views/customers",
                Map.of("serverTime", LocalDateTime.now(),
                    "customers", allCustomers));
    }*/


    @GetMapping("/api/v1/customers") //api를 정의할때는 꼭 versioning을 해줘야함
    @ResponseBody //메시지로 변환해준다!!!
    @CrossOrigin(origins = "*")
    public List<Customer> findCustomers(){
        return customerService.getAllCustomers();
    }

    @GetMapping("/api/v1/customers/{customerId}") //api를 정의할때는 꼭 versioning을 해줘야함
    @ResponseBody //메시지로 변환해준다!!!
    public ResponseEntity<Customer> findCustomer(@PathVariable("customerId") UUID customerId){ //ResponseEntity<T> : 헤더나 상태코드를 처리하기위해 제공해줌
        var customer = customerService.getCustomer(customerId);
        return customer.map(ResponseEntity::ok).orElse(ResponseEntity.status(404).build()); //ResponseEntity.status(404).body()
    }

    @PostMapping("/api/v1/customers/{customerId}") //api를 정의할때는 꼭 versioning을 해줘야함
    @ResponseBody //메시지로 변환해준다!!!
    public Customer saveCustomer(@PathVariable("customerId") UUID customerId, @RequestBody CustomerDto customer){
        logger.info("Got customer save request {}",customer);
        return null;
    }


//    @GetMapping("api/v1/customers/{customerId}")//@PathVariable로 매개변수로 변환. 만약 변환 실패시 오류 발생
//    public String findCustomer(@PathVariable("customerId") UUID customerId, Model model){
//        var maybeCustomer = customerService.getCustomer(customerId);
//        if(maybeCustomer.isPresent()){
//            model.addAttribute("customer", maybeCustomer.get());
//            return "views/customer-details";
//        }else{
//            return "views/404";
//        }
//    }

    @GetMapping("/customers") //api를 정의할때는 꼭 versioning을 해줘야함
    public String viewCustomerspage(Model model){
        var allCustomers = customerService.getAllCustomers();
        model.addAttribute("serverTime",LocalDateTime.now());
        model.addAttribute("customers",allCustomers);
        return "views/customers";
    }


    @GetMapping("/customers/new")
    public String viewNewCustomerPage(){
        return "views/new-customers";
    }

    @PostMapping("/customers/new")
    public String addNewCustomer(CreateCustomerRequest createCustomerRequest){
        customerService.createCustomer(createCustomerRequest.email(), createCustomerRequest.name());
        return "redirect:/customers";
    }


}
