package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private ScoreRepository scoreRepository;


    //modificar como pida el punto 5
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam String email, @RequestParam String password) {

        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUserName(email) != null) {
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @RequestMapping("/games")
    public Map<String, Object> getGameAll(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();

        if (Objects.isNull(authentication)) {
            dto.put("player", "Guest");
        } else if (Objects.nonNull(playerAuth(authentication))) {
            dto.put("player", playerAuth(authentication).makePlayerDTO());
        }
        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList()));
        return dto;
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "No puedes crear juegos si no estas logeado"), HttpStatus.FORBIDDEN);
        }
        if (playerAuth(authentication) == null) {
            return new ResponseEntity<>(makeMap("error", "algo paso..."), HttpStatus.UNAUTHORIZED);
        }

        if (playerAuth(authentication) != null) {
            Date dateGame = new Date();
            Game newGame = gameRepository.save(new Game(dateGame));
            GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(newGame, playerAuth(authentication)));


            // otra opcion de return, devuelvo el dto
            // Map<String,Object> dto = newGamePlayer.makeGamePlayerDTO()
            //  dto.put("gpid", newGamePlayer.getId());

            return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);

        }

        return new ResponseEntity<>(makeMap("error", "no autorizado"), HttpStatus.UNAUTHORIZED);
    }


    @RequestMapping("/game_view/{gpid}")
    public ResponseEntity<Map<String, Object>> getGame(@PathVariable Long gpid, Authentication authentication) {

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("Error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }

        // Player playerLogued = playerRepository.findByUserName(authentication.getName()).orElse(null);
        GamePlayer gamePlayer = gamePlayerRepository.findById(gpid).get();
        Game game = gamePlayer.getGame();

        if (gamePlayer.getPlayer().getId() != playerAuth(authentication).getId()) {
            return new ResponseEntity<>(makeMap("Error", "Acceso restringido"), HttpStatus.UNAUTHORIZED);
        }
        Map<String, Object> dto = gamePlayerRepository.getOne(gpid).getGame().makeGameDTO();
        Map<String, Object> hits = new LinkedHashMap<>();

        dto.put("gameState", this.getState(gamePlayer, gamePlayer.getOpponent()));
        dto.put("ships", gamePlayerRepository.getOne(gpid).getShipDTO());
        dto.put("salvoes", game.getGamePlayers().
                stream().
                flatMap(gamePlayer1 -> gamePlayer1.getSalvos().
                        stream().
                        map(salvo -> salvo.makeSalvoDTO())));
        //hits.put("self", gamePlayer.makeHitsDTO());
        //hits.put("opponent", new ArrayList<>());
        dto.put("hits", this.hitDTO(gamePlayer));
        return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);

    }

    private Map<String,Object> hitDTO(GamePlayer gamePlayer) {

//        List<Salvo> salvosMios = gamePlayer.getSalvos().stream().sorted(Comparator.comparing(Salvo::getTurn)).collect(Collectors.toList());
//        List<Salvo> salvosOpp = gamePlayer.getOpponent().getSalvos().stream().sorted(Comparator.comparing(Salvo::getTurn)).collect(Collectors.toList());

        Map<String,Object> dto = new LinkedHashMap<>();
        if(Objects.nonNull(gamePlayer.getOpponent())) {
            dto.put("self", gamePlayer.getOpponent().getSalvos().stream().sorted(Comparator.comparing(Salvo::getTurn)).collect(Collectors.toList())
                    .stream().map(salvo1 -> gamePlayer.makeHitsDTO(salvo1)).collect(Collectors.toList()));
            dto.put("opponent", gamePlayer.getSalvos().stream().sorted(Comparator.comparing(Salvo::getTurn)).collect(Collectors.toList())
                    .stream().map(salvo1 -> gamePlayer.getOpponent().makeHitsDTO(salvo1)).collect(Collectors.toList()) );

//            dto.put("self", gamePlayer.makeHitsDTO(gamePlayer.getOpponent()));
//            dto.put("opponent", gamePlayer.getOpponent().makeHitsDTO(gamePlayer));
        }
        else {
            dto.put("self", new ArrayList<>());
            dto.put("opponent", new ArrayList<>());
        }
        return dto;
    }


    @RequestMapping(path = "/game/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long gameId, Authentication authentication) {

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }
        if (gameRepository.findById(gameId).get() == null) {

            return new ResponseEntity<>(makeMap("error", "No existe ese juego"), HttpStatus.FORBIDDEN);
        }
        if (gameRepository.findById(gameId).get().getGamePlayers().stream().count() == 2) {
            return new ResponseEntity<>(makeMap("error", "El juego esta lleno"), HttpStatus.FORBIDDEN);
        }

        Game game = gameRepository.findById(gameId).get();
        GamePlayer gamePlayer = gamePlayerRepository.save(new GamePlayer(game, playerAuth(authentication)));

        return new ResponseEntity<>(makeMap("gpid", gamePlayer.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gpid}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> placeShips(@PathVariable Long gpid, @RequestBody List<Ship> ships, Authentication authentication) {
        GamePlayer gamePlayer = gamePlayerRepository.findById(gpid).get();
        if (isGuest(authentication)) {

            return new ResponseEntity<>(makeMap("error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer.getId() != gpid) {

            return new ResponseEntity<>(makeMap("error", "El usuario no existe"), HttpStatus.UNAUTHORIZED);
        }
        if (playerAuth(authentication).getId() != gamePlayer.getPlayer().getId()) {

            return new ResponseEntity<>(makeMap("error", "El usuario no  corresponde"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer.getShips().stream().count() >= 5) {
            return new ResponseEntity<>(makeMap("error", "Ya colocaste tus barcos"), HttpStatus.FORBIDDEN);
        }

        //  List<String> locations = ship.getLocations();
        // String shipType = ship.getShipType();
        // ship = shipRepository.save(Ship(gamePlayer, shipType ,locations));

        ships.stream().forEach(ship -> ship.setGamePlayer(gamePlayer));
        shipRepository.saveAll(ships);

        return new ResponseEntity<>(makeMap("OK", "Ships added"), HttpStatus.CREATED);

    }

    @RequestMapping(path = "/games/players/{gpid}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> placeSalvos(@PathVariable Long gpid, @RequestBody Salvo salvo, Authentication authentication) {

        GamePlayer gamePlayer = gamePlayerRepository.findById(gpid).get();

        if (isGuest(authentication)) {

            return new ResponseEntity<>(makeMap("error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer.getId() != gpid) {

            return new ResponseEntity<>(makeMap("error", "El usuario no existe"), HttpStatus.UNAUTHORIZED);
        }
        if (playerAuth(authentication).getId() != gamePlayer.getPlayer().getId()) {

            return new ResponseEntity<>(makeMap("error", "El usuario no corresponde"), HttpStatus.UNAUTHORIZED);
        }


        if (gamePlayer.getSalvos().stream().filter(salvo1 -> salvo1.getTurn() == salvo.getTurn()).count() > 0) {

            return new ResponseEntity<>(makeMap("error", "Ya colocaste tus salvos en este turno"), HttpStatus.FORBIDDEN);
        }

        salvo.setTurn(gamePlayer.getSalvos().size() + 1);
        salvo.setGamePlayer(gamePlayer);

        //salvoRepository.save(salvo.setGamePlayer(gamePlayer));
        salvoRepository.save(salvo);

        return new ResponseEntity<>(makeMap("OK", "Salvos added"), HttpStatus.CREATED);
    }


    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    private Player playerAuth(Authentication authentication) {
        return playerRepository.findByUserName(authentication.getName());
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public String getState(GamePlayer gamePlayerSelf, GamePlayer gamePlayerOpponent) {
        if (gamePlayerSelf.getShips().isEmpty()) {
            return "PLACESHIPS";
        }
        if (gamePlayerSelf.getGame().getGamePlayers().size() == 1) {

            return "WAITINGFOROPP";
        }
        if (gamePlayerOpponent.getShips().size() == 0){
            return "WAIT";
        }
        if (gamePlayerSelf.getSalvos().size() > 0
                && gamePlayerOpponent.getSalvos().size() > 0
                && gamePlayerSelf.getSalvos().size() == gamePlayerOpponent.getSalvos().size()
                && gamePlayerSelf.win() == true
                && gamePlayerOpponent.win() == true){


            Date date = new Date();

            Score score1 = new Score(gamePlayerOpponent.getPlayer(),gamePlayerOpponent.getGame(), 0.5, date);
            Score score2 = new Score(gamePlayerSelf.getPlayer(),gamePlayerSelf.getGame(), 0.5, date);

            if(!this.existsScore(gamePlayerSelf.getGame())) {
                scoreRepository.save(score1);
                scoreRepository.save(score2);
            }

            return "TIE";

        }
        if (gamePlayerSelf.getSalvos().size() > 0
                && gamePlayerOpponent.getSalvos().size() > 0
                && gamePlayerSelf.getSalvos().size() == gamePlayerOpponent.getSalvos().size()
                && gamePlayerSelf.win() == true
                && gamePlayerOpponent.win() == false){

            Date date = new Date();
            Score score = new Score(gamePlayerSelf.getPlayer(),gamePlayerSelf.getGame(), 1.0, date);

            if (!this.existsScore(gamePlayerSelf.getGame())){
                scoreRepository.save(score);
            }

            return "WON";
        }
        if (gamePlayerSelf.getSalvos().size() > 0
                && gamePlayerOpponent.getSalvos().size() > 0
                && gamePlayerSelf.getSalvos().size() == gamePlayerOpponent.getSalvos().size()
                && gamePlayerSelf.win() == false
                && gamePlayerOpponent.win() == true){

            Date date = new Date();
            Score score = new Score(gamePlayerSelf.getPlayer(),gamePlayerSelf.getGame(), 0.0, date);
            if (!this.existsScore(gamePlayerSelf.getGame())) {
                scoreRepository.save(score);
            }
            return "LOST";
        }
        if (gamePlayerSelf.getId() < gamePlayerOpponent.getId()) {
            if (gamePlayerSelf.getSalvos().size() > gamePlayerOpponent.getSalvos().size()) {

                return "WAIT";
            } else if (gamePlayerSelf.getSalvos().size() == gamePlayerOpponent.getSalvos().size()) {
                if (gamePlayerSelf.getId() < gamePlayerOpponent.getId()) {

                    return "PLAY";
                } else {
                    return "WAIT";
                }
            }
        }
        if (gamePlayerSelf.getId() > gamePlayerOpponent.getId()) {
            if (gamePlayerSelf.getSalvos().size() < gamePlayerOpponent.getSalvos().size()) {

                return "PLAY";
            } else if (gamePlayerSelf.getSalvos().size() == gamePlayerOpponent.getSalvos().size()) {
                if (gamePlayerSelf.getId() > gamePlayerOpponent.getId()) {

                    return "WAIT";
                } else {
                    return "PLAY";
                }
            }
        }



//            if (gamePlayerSelf.getSalvos()
//                    .size()
//                    == gamePlayerOpponent.getSalvos()
//                    .size()
//                    //compara turnos mios y del opp
//                    && gamePlayerSelf.getShips()
//                    .stream()
//                    .flatMap(_ship -> _ship.getShipLocations().stream())
//                    .count() == gamePlayerOpponent.totalHits())
//            // compara el numero total de mis locaciones de mis ships y
//            {
//                if (gamePlayerOpponent
//                        .getShips()
//                        .stream()
//                        .mapToLong(ship ->ship.getShipLocations().size())
//                        .sum() == gamePlayerSelf.totalHits()) {
//
//                    return "TIE";
//                } else {
//                    return "WON";
//                }
//
//            }
//            if (gamePlayerSelf.getSalvos()
//                    .size()
//                    == gamePlayerOpponent.getSalvos()
//                    .size()
//
//                    && gamePlayerOpponent.getShips()
//                    .stream()
//                    .flatMap(_ship -> _ship.getShipLocations().stream())
//                    .count() == gamePlayerSelf.totalHits()) {
//
//                return "LOST";
//            }


        return "LOST";
        }

        public boolean existsScore(Game game){
        if (game.getScore().isEmpty()){
            return false;
        }
        return true;
        }

    }
