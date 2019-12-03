package com.codeoftheweb.salvo.models;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Salvo {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private long turn;

    @ElementCollection
    private List<String> salvoLocations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;


    public Salvo() {

    }

    public Salvo(GamePlayer gamePlayer, List<String> salvoLocations, long turn) {
        this.turn = turn;
        this.gamePlayer = gamePlayer;
        this.salvoLocations = salvoLocations;

    }

    public long countHits(Ship ship){

        return this.getSalvoLocations()
                .stream()
                .filter(salvoLocs -> ship.getShipLocations().contains(salvoLocs))
                .count();

    }

    public long getId() {
        return id;
    }


    public long getTurn() {
        return turn;
    }

    public void setTurn(long turn) {
        this.turn = turn;
    }

    public List<String> getSalvoLocations() {
        return salvoLocations;
    }

    public void setSalvoLocations(List<String> salvoLocations) {
        this.salvoLocations = salvoLocations;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }


    public Map<String, Object> makeSalvoDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn", this.getTurn());
        dto.put("player", this.gamePlayer.getPlayer().getId());
        dto.put("locations", this.getSalvoLocations());
        return dto;
    }
}