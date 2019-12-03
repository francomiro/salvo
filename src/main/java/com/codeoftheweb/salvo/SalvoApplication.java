package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@SpringBootApplication
public class SalvoApplication {



	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
		return (args) -> {

			Player player1 = new Player("franco@asda1", passwordEncoder().encode("asd123"));
			Player player2 = new Player("franco@asda2", passwordEncoder().encode("123"));
			Player player3 = new Player("franco@asda3", passwordEncoder().encode("123456"));

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

			Date finishDate = new Date();
			Score score1 = new Score(player1, game1, 1.0, finishDate);
			Score score2 = new Score(player2, game1, 1.0, finishDate);
			Score score3 = new Score(player3, game1, 0.5, finishDate);

			scoreRepository.save(score1);
			scoreRepository.save(score2);
			scoreRepository.save(score3);
		};
	}
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;


	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName-> {
			Player player = playerRepository.findByUserName(inputName);
			if (player != null) {
				return new User(player.getUserName(), player.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + inputName);
			}
		});
	}

}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http.authorizeRequests()
				.antMatchers("/web/**").permitAll()
				.antMatchers("/web/games.html").permitAll()
				.antMatchers("web/game.html?gp=*","/api/game_view/*").hasAuthority("USER")
				.antMatchers("/api/**").permitAll()
				.anyRequest().permitAll();

		http.formLogin()
				.usernameParameter("name")
				.passwordParameter("pwd")
				.loginPage("/api/login");

				http.logout().logoutUrl("/api/logout");

		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}



	}



