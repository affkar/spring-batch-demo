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
@Table(name = "CHARACTERISTIC")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacterE {

    @Id
    @GeneratedValue(generator = "character-id-generator")
    @SequenceGenerator(name = "character-id-generator", sequenceName = "CHARACTERISTIC_ID_SEQUENCE", allocationSize = 1)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "TYPE")
    private String type;

}
