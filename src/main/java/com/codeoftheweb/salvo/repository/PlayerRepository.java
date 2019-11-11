package com.codeoftheweb.salvo.repository;
import com.codeoftheweb.salvo.models.Player;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.JpaRepository;


@RepositoryRestResource
public interface PlayerRepository extends JpaRepository<Player, Long> {



}
