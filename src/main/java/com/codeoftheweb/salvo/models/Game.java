package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date creationDate;

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    Set<Score> score;

    public Game(){

    }
    public Game(Date fecha){
        this.creationDate = fecha;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public Map<String , Object> makeGameDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("created" , this.getCreationDate());
        dto.put("gamePlayers", this.getGamePlayers()
                                .stream()
                                .map(gamePlayer -> gamePlayer.makeGamePlayerDTO())
                                .collect(Collectors.toList()));
        dto.put("scores", this.getScore().stream().map(score1 -> score1.makeScoreDTO()).collect(Collectors.toList()));
        return dto;

    }

    public Set<Score> getScore() {
        return score;
    }

    public void setScore(Set<Score> score) {
        this.score = score;
    }
}
