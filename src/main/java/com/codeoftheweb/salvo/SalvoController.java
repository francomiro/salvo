package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.models.Ship;
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


 //modificar como pida el punto 5
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam String email, @RequestParam String password) {

        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUserName(email) !=  null) {
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player( email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @RequestMapping("/games")
    public Map<String, Object> getGameAll(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();

        if(Objects.isNull(authentication)){
            dto.put("player","Guest");
        } else if(Objects.nonNull(playerAuth(authentication))){
            dto.put("player", playerAuth(authentication).makePlayerDTO());
        }
        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList()));
        return dto;
    }
    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> createGame(Authentication authentication){

        if (isGuest(authentication)){
            return new ResponseEntity<>(makeMap("error", "No puedes crear juegos si no estas logeado"), HttpStatus.FORBIDDEN);
        }
        if (playerAuth(authentication) == null){
            return new ResponseEntity<>(makeMap("error", "algo paso..."), HttpStatus.UNAUTHORIZED);
        }

        if (playerAuth(authentication) != null) {
            Date dateGame = new Date();
            Game newGame = gameRepository.save(new Game(dateGame));
            GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(newGame,playerAuth(authentication)));


             // otra opcion de return, devuelvo el dto
           // Map<String,Object> dto = newGamePlayer.makeGamePlayerDTO()
          //  dto.put("gpid", newGamePlayer.getId());

            return new ResponseEntity<>(makeMap("gpid",newGamePlayer.getId()),HttpStatus.CREATED);

        }

            return new ResponseEntity<>(makeMap("error", "no autorizado"),HttpStatus.UNAUTHORIZED);
    }




    @RequestMapping("/game_view/{gpid}")
    public ResponseEntity<Map<String,Object>> getGame(@PathVariable long gpid, Authentication authentication){

    if (isGuest(authentication)){
        return new ResponseEntity<>(makeMap("Error", "Usuario no logueado"),HttpStatus.UNAUTHORIZED);
    }

       // Player playerLogued = playerRepository.findByUserName(authentication.getName()).orElse(null);
        GamePlayer gamePlayer = gamePlayerRepository.findById(gpid).get();
        Game game = gamePlayer.getGame();

        if (gamePlayer.getPlayer().getId() != playerAuth(authentication).getId()){
            return new ResponseEntity<>(makeMap("Error", "Acceso restringido"), HttpStatus.UNAUTHORIZED);
        }
            Map<String, Object> dto = gamePlayerRepository.getOne(gpid).getGame().makeGameDTO();
            Map<String, Object> hits = new LinkedHashMap<>();

            dto.put("ships", gamePlayerRepository.getOne(gpid).getShipDTO());
            dto.put("salvoes", game.getGamePlayers().
                    stream().
                    flatMap(gamePlayer1 -> gamePlayer1.getSalvos().
                            stream().
                            map(salvo -> salvo.makeSalvoDTO())));
            hits.put("self", new ArrayList<>());
            hits.put("opponent", new ArrayList<>());
            dto.put("hits",hits);
            return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);

    }
    @RequestMapping(path = "/game/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> joinGame(@PathVariable long gameId, Authentication authentication) {

        if (isGuest(authentication)){
            return new ResponseEntity<>(makeMap("Error", "Usuario no logueado"),HttpStatus.UNAUTHORIZED);
        }
        if (gameRepository.findById(gameId).get() == null) {

            return new ResponseEntity<>(makeMap("Error", "No existe ese juego"), HttpStatus.FORBIDDEN);
        }
        if (gameRepository.findById(gameId).get().getGamePlayers().stream().count() == 2){
            return new ResponseEntity<>(makeMap("Error", "El juego esta lleno"), HttpStatus.FORBIDDEN);
        }

        Game game = gameRepository.findById(gameId).get();
        GamePlayer gamePlayer = gamePlayerRepository.save(new GamePlayer(game, playerAuth(authentication)));

        return new ResponseEntity<>(makeMap("gpid",gamePlayer.getId()),HttpStatus.CREATED);
    }
    @RequestMapping(path = "/games/players/{gpid}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> placeShips(@PathVariable long gpid, @RequestBody List<Ship> ships,Authentication authentication){
        GamePlayer gamePlayer = gamePlayerRepository.findById(gpid).get();
        if (isGuest(authentication)){

            return new ResponseEntity<>(makeMap("Error", "Usuario no logueado"),HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer.getId() != gpid){

            return new ResponseEntity<>(makeMap("Error", "El usuario no corresponde"),HttpStatus.UNAUTHORIZED);
        }
        if (playerAuth(authentication).getId() != gamePlayer.getPlayer().getId()){

            return new ResponseEntity<>(makeMap("Error", "El usuario no  corresponde"),HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer.getShips().stream().count() >=5){
            return new ResponseEntity<>(makeMap("Error", "Ya colocaste tus barcos"),HttpStatus.FORBIDDEN);
        }

     //  List<String> locations = ship.getLocations();
       // String shipType = ship.getShipType();
       // ship = shipRepository.save(Ship(gamePlayer, shipType ,locations));

        ships.stream().forEach(ship -> ship.setGamePlayer(gamePlayer));
        shipRepository.saveAll(ships);

        return new ResponseEntity<>(makeMap("ships","Ships added"),HttpStatus.CREATED);

    }








    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
    private Player playerAuth(Authentication authentication){
        return playerRepository.findByUserName(authentication.getName());
    }
    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}
