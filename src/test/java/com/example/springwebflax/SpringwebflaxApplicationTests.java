package com.example.springwebflax;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringwebflaxApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testPassValue(){
        System.out.println("test");
        Double item = 5.0;
        double increment = increment(item);
        System.out.println(item);
    }

    private double increment(double item){
        double five = 5.0;
        item += five;
        return item;
    }

}
