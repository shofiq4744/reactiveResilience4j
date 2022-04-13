package com.example.springwebflax.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

	@Bean
	public WebClient getWebClient() {
		HttpClient httpClient = HttpClient.create()
				.tcpConfiguration(client ->
										  client.option(ChannelOption.SO_KEEPALIVE,true)
												  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
												  .doOnConnected(conn -> conn
														  .addHandlerLast(new ReadTimeoutHandler(1))
														  .addHandlerLast(new WriteTimeoutHandler(1))));


		ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient.wiretap(true));
		return WebClient.builder()
				.clientConnector(connector)
				.build();
	}

}
