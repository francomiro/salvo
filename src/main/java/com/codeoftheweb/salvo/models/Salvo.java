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
    private Long id;

    private long turn;

    @ElementCollection
    private List<String> locations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;


    public Salvo() {

    }

    public Salvo(GamePlayer gamePlayer, List<String> locations, long turn) {
        this.turn = turn;
        this.gamePlayer = gamePlayer;
        this.locations = locations;

    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTurn() {
        return turn;
    }

    public void setTurn(Long turn) {
        this.turn = turn;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
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
        dto.put("locations", this.getLocations());
        return dto;
    }
}