package com.codeoftheweb.salvo.models;


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

    private String type;

    @ElementCollection
    private List<String> shipLocations = new ArrayList<>();


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;


    public Ship(){

    }

    public Ship(GamePlayer gamePlayer , String type, List<String> shipLocations){
        this.gamePlayer = gamePlayer;
        this.type = type;
        this.shipLocations = shipLocations;
    }


    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public List<String> getShipLocations() {
        return shipLocations;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setShipLocations(List<String> shipLocations) {
        this.shipLocations = shipLocations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Map<String, Object> makeShipDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", getType());
        dto.put("locations" , getShipLocations());
        return dto;
    }
}
