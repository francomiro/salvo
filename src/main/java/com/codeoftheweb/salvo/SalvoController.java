package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.repository.GamePlayerRepository;
import com.codeoftheweb.salvo.repository.GameRepository;
import com.codeoftheweb.salvo.repository.SalvoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private SalvoRepository salvoRepository;


    @RequestMapping("/games")
    public List<Object>  getGameAll(){

    return gameRepository.findAll()
            .stream()
            .map(game -> game.makeGameDTO())
            .collect(Collectors.toList());
}
    @RequestMapping("/game_view/{gpid}")
    public Map<String,Object> getGame(@PathVariable   long    gpid){
        GamePlayer gamePlayer = gamePlayerRepository.findById(gpid).get();
        Game game = gamePlayer.getGame();

        Map<String,Object> dto = gamePlayerRepository.getOne(gpid).getGame().makeGameDTO();
        dto.put("ships",gamePlayerRepository.getOne(gpid).getShipDTO());
        dto.put("salvoes", game.getGamePlayers().
                stream().
                flatMap(gamePlayer1 -> gamePlayer1.getSalvos().
                        stream().
                        map(salvo -> salvo.makeSalvoDTO())));
        return dto;




    }




}
