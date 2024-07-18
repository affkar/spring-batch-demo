package com.example.springbatchdemo.mapper;

import com.example.springbatchdemo.entity.PlayerE;
import com.example.springbatchdemo.model.Player;

public interface PlayerMapper {

    PlayerE map(Player player);

    Player map(PlayerE playerE);

}
