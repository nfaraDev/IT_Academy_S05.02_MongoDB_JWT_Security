package cat.itacademy.barcelonactiva.fara.natalia.s05._MongoDB.S0502_MongoFaraNatalia.models.service;

import cat.itacademy.barcelonactiva.fara.natalia.s05._MongoDB.S0502_MongoFaraNatalia.models.DTO.RankingDTO;
import cat.itacademy.barcelonactiva.fara.natalia.s05._MongoDB.S0502_MongoFaraNatalia.models.domainEntity.Game;
import cat.itacademy.barcelonactiva.fara.natalia.s05._MongoDB.S0502_MongoFaraNatalia.models.domainEntity.Player;
import cat.itacademy.barcelonactiva.fara.natalia.s05._MongoDB.S0502_MongoFaraNatalia.models.repository.IGameRepo;
import cat.itacademy.barcelonactiva.fara.natalia.s05._MongoDB.S0502_MongoFaraNatalia.models.repository.IPlayerRepo;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class GameService implements IGameService {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private IGameRepo gameRepo;
    @Autowired
    private IPlayerRepo playerRepo;

    @Override
    public Object playGame(String id) {
        Player player = playerService.getPlayerById(id);
        Game game = new Game(player.getId(), throwDices());
        gameRepo.save(game);
        return game;
    }

    @Override
    public String deleteGamesByIdPlayer(String id) {
        List<Game> games = gameRepo.findGamesByPlayerId(id);
        System.out.println("Number of games found: " + games.size());
        if (games.isEmpty()) {
            return "Player with id " + id + " has no games";
        } else {
            for (Game game : games) {
                if (game.getId() != null) {
                    gameRepo.delete(game);
                } else {
                    // Aquí puedes manejar el caso cuando el id del juego es nulo
                    System.out.println("Game with null id found");
                }
            }
            return "Games deleted";
        }
    }
    @Override
    public List<Game> getAllGamesByIdPlayer(String id) {
        return gameRepo.findGamesByPlayerId(id);
    }

    @Override
    public List<RankingDTO> getRankingGames() {
        List<RankingDTO> rankingDTOS = calcRanking();
        return rankingDTOS;
    }
    @Override
    public String getWinnerRankingPlayer() {
        List<RankingDTO> rankingDTOS = calcRanking();
        if (rankingDTOS.isEmpty()) {
            return "There aren't games";
        }
        return rankingDTOS.stream()
                .filter(dto -> dto.getSuccessRate() == rankingDTOS.get(0).getSuccessRate())
                .toList()
                .toString();
    }

    @Override
    public String getLoserRankingPlayer() {
        List<RankingDTO> rankingDTOS = calcRanking();
        if (rankingDTOS.isEmpty()) {
            return "There aren't games";
        }
        return rankingDTOS.stream()
                .filter(dto -> dto.getSuccessRate() == rankingDTOS.get(rankingDTOS.size() - 1).getSuccessRate())
                .toList()
                .toString();
    }

    private List<RankingDTO> calcRanking() {
        List<RankingDTO> rankingDTOS = new ArrayList<>();
        List<Player> players = playerRepo.findAll();
        List<Game> games = gameRepo.findAll();
        for (Player player : players) {
            String id = player.getId();
            long countWins = games.stream()
                    .filter(game -> game.getPlayerId().equals(id) && game.getPoints() == 7)
                    .count();
            long totalGames = games.stream()
                    .filter(game -> game.getPlayerId().equals(id))
                    .count();
            double successRate = (countWins > 0) ? (double) countWins / totalGames : 0.0;

            if (totalGames > 0) {
                RankingDTO dto = new RankingDTO(id, player.getName(),(int) countWins,(int) totalGames, successRate);
                rankingDTOS.add(dto);
            }
        }
        // Usando lambda para ordenar la lista en orden descendente según SuccessRate
        rankingDTOS.sort(Comparator.comparing(RankingDTO::getSuccessRate).reversed());
        return rankingDTOS;
    }

    private Integer throwDices() {
        Integer dado1 = new Random().nextInt(6) + 1;
        Integer dado2 = new Random().nextInt(6) + 1;
        return dado1 + dado2;
    }
}
