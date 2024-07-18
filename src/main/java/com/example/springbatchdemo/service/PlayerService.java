package com.example.springbatchdemo.service;

import com.example.springbatchdemo.model.Player;

public interface PlayerService {

    Player getPlayerById(int playerId);

    Player savePlayer(Player player);

}
