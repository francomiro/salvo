package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String userName;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<Score> score;

    private String password;

    public Player(){

    }

    public Player(String mail, String pass)
    {
        this.password = pass;
        this.userName = mail;
    }


    public long getId() {
        return id;
    }



    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public Map<String, Object> makePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("email" , this.getUserName());
        return dto;
    }

    public Set<Score> getScore() {
        return score;
    }

    public void setScore(Set<Score> score) {
        this.score = score;
    }

    public Score getScore(Game game){

        return this.getScore()
                .stream()
                .filter(score -> score.getGame().getId() == game.getId())
                .findFirst().orElse(null);
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
