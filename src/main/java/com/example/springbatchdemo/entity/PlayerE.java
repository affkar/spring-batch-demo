package com.example.springbatchdemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PLAYER")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerE {

    @Id
    @GeneratedValue(generator = "player-id-generator")
    @SequenceGenerator(name = "player-id-generator", sequenceName = "Player_Id_Sequence", allocationSize = 1)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "NAME")
    private String name;

}
