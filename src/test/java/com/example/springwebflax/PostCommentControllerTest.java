package com.example.springwebflax;

import com.example.springwebflax.config.CircuitBreakerManager;
import com.example.springwebflax.service.CommentService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostCommentControllerTest {

    public static final String POST_URI = "https://jsonplaceholder.typicode.com/posts/1";
    public static final String COMMENT_URI = "https://jsonplaceholder.typicode.com/posts/";

   // @InjectMocks
    //PostCommentController postCommentController;

    @InjectMocks
    CommentService commentService;
    @Mock
    CircuitBreakerManager circuitBreakerManager;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private WebClient webClient;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void tearDown() {
     //   postCommentController = null;
        commentService = null;
    }

    @Test
    void showPostCommentsSuccess() throws IOException {
        // Setup:
        createNecessarySpy();
        // When
        Object response = commentService.getComments(POST_URI).block();
        Map postMap = (Map) response;
        String id = String.valueOf(postMap.get("id"));

        //Verify mocks:
        assertNotNull(response);
        assertEquals(id, "1");

    }

    CircuitBreaker circuitBreaker;
    @Test
    void circuitOpenAfterMinimumCall() throws IOException {
        // Setup:
        commentService = spy(new CommentService(webClient,circuitBreakerRegistry));
        circuitBreaker = circuitBreakerRegistry.find("webclient").get();
        // When
        for (int num = 1; num <= 5; num++) {
            commentService
                    .getComments(POST_URI+"/dummy")
                    .onErrorResume(throwable -> Mono.just(throwable.getMessage())).block();
            System.out.println(circuitBreaker.getState().name());
            if(num<circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls()) {
                assertEquals(circuitBreaker.getState().name(), "CLOSED");
            } else {
                assertEquals(circuitBreaker.getState().name(), "OPEN");
            }
        }
    }

    private void createNecessarySpy() {

        circuitBreakerManager = spy(new CircuitBreakerManager(circuitBreakerRegistry));
        WebClient webClientMock = mock(WebClient.class);
        commentService = spy(new CommentService(webClientMock,circuitBreakerRegistry));

        WebClient.RequestHeadersUriSpec requestBodyUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        when(webClientMock.get()).thenReturn(requestBodyUriSpecMock);
        WebClient.RequestHeadersUriSpec uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(uriSpecMock);

        ClientResponse clientResponseMock = mock(ClientResponse.class);
        Mono<ClientResponse> clientResponseMockMono = Mono.just(clientResponseMock);

        when(clientResponseMock.bodyToMono(Object.class)).thenReturn(Mono.just(getMockPostResponse()));
        when(clientResponseMock.statusCode()).thenReturn(HttpStatus.OK);
        when(clientResponseMock.rawStatusCode()).thenReturn(HttpStatus.OK.value());
        when(uriSpecMock.exchange()).thenReturn(clientResponseMockMono);

    }

    private Object getMockPostResponse(){
        Map mockPostObj = new HashMap();
        mockPostObj.put("id","1");
        mockPostObj.put("userId","1");
        mockPostObj.put("title","demo title");
        return mockPostObj;
    }

}
