package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    Set<Ship> ships;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Salvo> salvos;

    public GamePlayer() {

        this.joinDate = new Date();
    }

    public GamePlayer(Game game, Player player) {
        this.player = player;
        this.game = game;
        this.joinDate = new Date();
    }


    public long getId() {
        return id;
    }


    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Map<String, Object> makeGamePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("player", this.getPlayer().makePlayerDTO());
        //if (this.getScore() != null){
        //dto.put("score", this.getScore().getScore());}
        return dto;

    }

    public GamePlayer getOpponent(){
        return this.getGame().getGamePlayers().stream()
                .filter(gamePlayer -> gamePlayer.getId() != this.getId())
                .findFirst()
                .orElse(null);
    }

    public List<Object> getShipDTO() {

       return  this.getShips().
                stream().
                map(ship -> ship.makeShipDTO()).collect(Collectors.toList());

    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    public Set<Salvo> getSalvos() {
        return salvos;
    }

    public void setSalvos(Set<Salvo> salvos) {
        this.salvos = salvos;
    }

    public List<Object> getSalvoDTO() {

        return getSalvos().
                stream().
                map(salvo -> salvo.makeSalvoDTO()).
                collect(Collectors.toList());
    }

public Score getScore(){

        return this.player.getScore(this.getGame());
}

    public Map<String,Object> makeHitsDTO(Salvo salvo) {


        Map<String,Object> dto = new LinkedHashMap<>();
           // for(Salvo a : salvos) {

                dto.put("turn", salvo.getTurn());
                dto.put("hitLocations", getHitsLocation(salvo));
                dto.put("damages", this.getDamageDTO(salvo));
                dto.put("missed", hitMissed(salvo));
           // }

       return dto;

    }

    public Ship getShipByType(String type){
        return this.getShips()
                .stream().filter(ship -> ship.getType().equals(type)).findFirst().orElse(new Ship());
    }


    public Map<String,Object> getDamageDTO(Salvo a){
        Map<String,Object> dto = new LinkedHashMap<>();

        dto.put("carrierHits", a.countHits(getShipByType("carrier")));
        dto.put("battleshipHits", a.countHits(getShipByType("battleship")));
        dto.put("submarineHits", a.countHits(getShipByType("submarine")));
        dto.put("destroyerHits", a.countHits(getShipByType("destroyer")));
        dto.put("patrolboatHits", a.countHits(getShipByType("patrolboat")));

// EL ACUMULADO DE CADA UNO TOTAL

        List <Salvo> oppSalvo= new ArrayList<>(this.getOpponent().getSalvos());

        dto.put("carrier", oppSalvo
                .stream().map(salvo -> salvo.countHits(getShipByType("carrier"))).reduce(Long::sum).get());
        dto.put("battleship", oppSalvo
                .stream().map(salvo -> salvo.countHits(getShipByType("battleship"))).reduce(Long::sum).get());
        dto.put("submarine", oppSalvo
                .stream().map(salvo -> salvo.countHits(getShipByType("submarine"))).reduce(Long::sum).get());
        dto.put("destroyer", oppSalvo
                .stream().map(salvo -> salvo.countHits(getShipByType("destroyer"))).reduce(Long::sum).get());
        dto.put("patrolboat", oppSalvo
                .stream().map(salvo -> salvo.countHits(getShipByType("patrolboat"))).reduce(Long::sum).get());


        return dto;
    }

    public List<String> getHitsLocation( Salvo salvoOpp){
        return this.getShips()
                .stream()
                .flatMap(ship -> ship.getShipLocations()
                        .stream()
                        .flatMap(shiploc -> salvoOpp
                                .getSalvoLocations()
                                .stream()
                                .filter(salvoLoc-> shiploc.contains(salvoLoc))))
                .collect(Collectors.toList());

    }

    public long hitMissed(Salvo a){

        long missed = 5 - this.getHitsLocation(a).stream().count();

        return missed;
    }

//    public int hitsInAllTurns(Salvo s){
//        s.countHits(getShip)
//
//
//    }
//

//    public int countHits(Salvo salvo,Ship ship, GamePlayer gamePlayer){
//
//        int totalHits = gamePlayer.getHitsLocation().stream().count();
//        ArrayList<String> shipLoc = (ArrayList<String>) ship.getShipLocations();
//        ArrayList<String> hitLoc = (ArrayList<String>) gamePlayer.getHitsLocation(gamePlayer);
//        int count= 0;
//
//        if (totalHits != 0) {
//            for (int i = 0; i <= totalHits; i++) {
//                for (int j = 0; j <= ship.getShipLocations().stream().count(); j++) {
//                    if (hitLoc.get(j).equals(shipLoc.get(j))) {
//                        count++;
//                    }
//                }
//
//            }
//        }
//
//        this.getHitsLocation(salvo).stream()
//                .flatMap(hitLocs -> ship.getShipLocations()
//                                .stream()
//                                .filter(shipLocs -> shipLocs.contains(hitLocs))).count();
//
//        return count;
//
//    }







}