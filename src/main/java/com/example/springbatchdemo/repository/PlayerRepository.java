package com.example.springbatchdemo.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.springbatchdemo.entity.PlayerE;

@Repository
public interface PlayerRepository extends CrudRepository<PlayerE, Integer> {

}
