package com.example.springwebflax.controller;

import com.example.springwebflax.service.CommentService;
import com.example.springwebflax.service.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.*;

@RestController
public class CommentController {

    public static final Logger LOGGER = LoggerFactory.getLogger(CommentController.class);

    private CommentService commentService;
    public CommentController(CommentService commentService){
        this.commentService = commentService;
    }

    @GetMapping(value = "/comments",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Map<String,Object>>> comments(){

        return commentService.getComments(Util.POST_URI)
                .flatMap(res-> Mono.just(res))
                .flatMap(post->
                        getComments().flatMap(comments->{
                    Map<String,Object> item = new HashMap<>();
                    item.put("post",post);
                    item.put("comments",comments);
                    LOGGER.debug("SUCCESS","Get response");
                    return Mono.just(new ResponseEntity<>(item, HttpStatus.OK));
                })).onErrorResume(err->{
                    Map<String,Object> item = new HashMap<>();
                    item.put("comments",err.getMessage());
                    LOGGER.debug("ERROR",err.getMessage());
                    return Mono.just(new ResponseEntity<>(item, HttpStatus.OK));
                }).doFinally(rs->{
                    LOGGER.debug("INFO","Complete");
                });

    }

    private Mono<List<Object>>  getComments(){
        Collection<Mono<Object>> item = new ArrayList<>();
        for(int i=1;i<=20;i++){
            item.add(commentService.getComments(Util.COMMET_URI+i)
                                .flatMap(comment-> Mono.just(comment)));
        }
        return Flux.merge(item).collectList();
    }
}
