package com.example.springwebflax;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.net.SocketException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class SimpleMonoAndFlexTest {

    @Test
    public void SimpleMono(){
        Mono<String> lang = Mono.just("Java").log();
        lang.subscribe(obj->System.out.println(obj));
    }

    @Test
    public void SimpleFlex(){
        Flux<String> lang = Flux.just("Java","PYTHON","GOLANG")
        .concatWithValues("C").log();
        lang.subscribe(obj->System.out.println(obj));
    }

    @Test
    public void testDoWithSession2() throws Exception {
        Function<String, Mono<Integer>> fun1 = str -> Mono.fromCallable(() -> {
            System.out.println("start some long timed work");
            //for demonstration we'll print some clock ticks
            for (int i = 1; i <= 10; i++) {
                try {
                    Thread.sleep(1000);
                    System.out.println(i + "s...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("work has completed");
            return str.length();
        });

        //

        //let two ticks show up
        StepVerifier.create(doWithConnection(fun1,2100))
                .verifyError(SocketException.class);
    }


    public <T> Mono<T> doWithConnection(Function<String, Mono<T>> callback, long timeout) {
        return Mono.using(
                //the resource supplier:
                () -> {
                    System.out.println("connection acquired");
                    return "hello";
                },
                //create a Mono out of the resource. On any termination, the resource is cleaned up
                connection -> Mono.just(connection)
                        //the blocking callable needs own thread:
                        .publishOn(Schedulers.single())
                        //execute the callable and get result...
                        .then(callback.apply(""))
                        //...but cancel if it takes too long
                        .timeout(Duration.ofMillis(timeout))
                        //for demonstration we'll log when timeout triggers:
                        .doOnCancel(()-> {
                                    System.out.println("socket timed out");
                                }
                        ),
                //the resource cleanup:
                connection -> System.out.println("cleaned up " + connection));
    }


}
