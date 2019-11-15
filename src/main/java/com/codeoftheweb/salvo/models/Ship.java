package com.example.salvo.models;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private String shipType;

    @ElementCollection
    private List<String> locations = new ArrayList<>();


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;


    public Ship(){

    }

    public Ship(GamePlayer gamePlayer , String shipType, List<String> locations){
        this.gamePlayer = gamePlayer;
        this.shipType = shipType;
        this.locations = locations;


    }


    public Long getId() {
        return id;
    }

    public String getShipType() {
        return shipType;
    }

    public List<String> getLocations() {
        return locations;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Map<String, Object> makeShipDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", getShipType());
        dto.put("locations" , getLocations());
        return dto;
    }
}
