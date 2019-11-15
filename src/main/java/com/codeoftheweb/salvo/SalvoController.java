package com.example.salvo;

import com.example.salvo.repository.GamePlayerRepository;
import com.example.salvo.repository.GameRepository;
import com.example.salvo.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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

    @RequestMapping("/games")
    public List<Object>  getGameAll(){

    return gameRepository.findAll()
            .stream()
            .map(game -> game.makeGameDTO())
            .collect(Collectors.toList());
}
    @RequestMapping("/game_view/{gpid}")
    public List<Object> getGame(@PathVariable   long    gpid){
        List<Object> dto = new ArrayList<>();
        dto.add(gamePlayerRepository.getOne(gpid).getGame().makeGameDTO());
        dto.add(gamePlayerRepository.getOne(gpid).getShipDTO());
        return dto;




    }




}
