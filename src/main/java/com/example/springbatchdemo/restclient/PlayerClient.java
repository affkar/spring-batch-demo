package com.example.springbatchdemo.restclient;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.springbatchdemo.model.Player;
import com.example.springbatchdemo.model.PlayerPages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerClient {

    public static final int COUNT = 1000000;
    public final WebClient webClient;

    public PlayerPages getPlayerPages(java.time.Instant queryDate) {
        return webClient.get().uri("/api/player/pages/count/1").retrieve()
                .bodyToMono(PlayerPages.class)
                .block();
    }

    public List<Player> getPlayers(long pageNo) {
        String uri = "/api/player/pages?pageNo=" + pageNo;
        log.info("Contacting URI {}", uri);
        return webClient.get().uri(uri).retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Player>>() {
                })
                .block();
    }
}