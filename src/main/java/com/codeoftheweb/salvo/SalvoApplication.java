package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository) {
		return (args) -> {

			Player player1 = new Player("franco@asda1");
			Player player2 = new Player("franco@asda2");
			Player player3 = new Player("franco@asda3");

			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);

			Date dateGame = new Date();
			Game game1 = new Game(dateGame);
			Game game2 = new Game(dateGame);
			Game game3 = new Game(dateGame);

			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);

			GamePlayer gamePlayer1 = new GamePlayer(game1,player1);
			GamePlayer gamePlayer2 = new GamePlayer(game1,player2);
			GamePlayer gamePlayer3 = new GamePlayer(game2,player3);

			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);

			List<String> locations1 = new ArrayList<>();
			locations1.add("H1");
			locations1.add("H2");
			locations1.add("H3");

			List<String> locations2 = new ArrayList<>();
			locations2.add("B3");
			locations2.add("B4");
			locations2.add("B5");

			List<String> locations3 = new ArrayList<>();
			locations3.add("C2");
			locations3.add("C3");
			locations3.add("C4");


			Ship ship1 = new Ship(gamePlayer1, "Destroyer", locations1);
			Ship ship2 = new Ship(gamePlayer2, "Destroyer", locations2);
			Ship ship3 = new Ship(gamePlayer3, "Destroyer", locations3);

			shipRepository.save(ship1);
			shipRepository.save(ship2);
			shipRepository.save(ship3);

			List<String> locations4 = new ArrayList<>();
			locations4.add("B3");
			locations4.add("H2");

			List<String> locations5 = new ArrayList<>();
			locations5.add("B2");
			locations5.add("A2");

			List<String> locations6 = new ArrayList<>();
			locations6.add("C3");
			locations6.add("C4");


			Salvo salvo1 = new Salvo(gamePlayer1, locations4, 1);
			Salvo salvo2 = new Salvo(gamePlayer1, locations6, 2);
			Salvo salvo3 = new Salvo(gamePlayer2, locations5, 1);
			Salvo salvo4 = new Salvo(gamePlayer2, locations6, 2);
			Salvo salvo5 = new Salvo(gamePlayer3, locations4, 1);

			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
			salvoRepository.save(salvo5);


		};
	}
}


