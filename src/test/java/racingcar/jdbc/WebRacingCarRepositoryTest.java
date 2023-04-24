package racingcar.jdbc;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import racingcar.domain.Cars;
import racingcar.domain.RacingGame;
import racingcar.repository.web.WebRacingCarRepository;

class WebRacingCarRepositoryTest {

	private final WebRacingCarRepository repository = new WebRacingCarRepository();

	@BeforeEach
	void setUp() {
		JdbcTemplate jdbcTemplate = repository.getJdbcTemplate();

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS games\n"
			+ "(\n"
			+ "`gameId`        INT          NOT NULL AUTO_INCREMENT,\n"
			+ "`count`         INT          NOT NULL,\n"
			+ "`winner`       VARCHAR(100)  NOT NULL,\n"
			+ "`timeStamp`    TIMESTAMP     NOT NULL,\n"
			+ "PRIMARY KEY (`gameId`)\n"
			+ ");\n"
			+ "CREATE TABLE IF NOT EXISTS cars\n"
			+ "(\n"
			+ "`name`       VARCHAR(20) NOT NULL,\n"
			+ "`position`     INT       NOT NULL,\n"
			+ "`gameId`       INT       NOT NULL,\n"
			+ "PRIMARY KEY (`name`, `gameId`),\n"
			+ "FOREIGN KEY ( `gameId` ) REFERENCES `games` ( `gameId` )\n"
			+ ");"
		);
	}

	@Test
	void insertAndFindCar() {
		RacingGame racingGame = new RacingGame("a,b,c,d");
		Cars cars = racingGame.moveCars();
		int count = 5;

		repository.save(cars, count);
		assertThat(repository.find().size()).isEqualTo(1);
	}
}
