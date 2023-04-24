package racingcar.repository.web;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.zaxxer.hikari.HikariDataSource;

import racingcar.domain.Car;
import racingcar.domain.Cars;
import racingcar.dto.CarDto;
import racingcar.dto.HistoryDto;
import racingcar.dto.ResultDto;
import racingcar.repository.RacingCarRepository;

@Repository
@Transactional
public class WebRacingCarRepository implements RacingCarRepository {
	private final JdbcTemplate jdbcTemplate;
	private final RowMapper<HistoryDto> gameRowMapper = (resultSet, rowNum) -> new HistoryDto(
		resultSet.getInt("gameId"),
		resultSet.getString("winner")
	);
	private final RowMapper<CarDto> carRowMapper = (resultSet, rowNum) -> new CarDto(
		resultSet.getString("name"),
		resultSet.getInt("position")
	);

	public WebRacingCarRepository() {
		final DataSource dataSource = DataSourceBuilder.create()
			.url("jdbc:h2:mem:testdb")
			.username("sa")
			.password("")
			.type(HikariDataSource.class)
			.build();
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public void save(Cars cars, int count) {
		String sqlGame = "INSERT INTO games(count, winner, timestamp) VALUES (?,?,?)";
		String sqlCars = "INSERT INTO cars(name, position, gameId) VALUES(?,?,?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sqlGame, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, count);
			ps.setString(2, cars.winners());
			ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			return ps;
		}, keyHolder);
		for (Car car : cars.getCars()) {
			jdbcTemplate.update(sqlCars, car.getName(), car.getPosition(), keyHolder.getKey().intValue());
		}
	}

	@Override
	public List<ResultDto> find() {
		List<ResultDto> resultDtos = new ArrayList<>();

		String sqlGames = "SELECT winner, gameId FROM GAMES";
		List<HistoryDto> historyDtos = jdbcTemplate.query(sqlGames, gameRowMapper);
		for (HistoryDto historyDto : historyDtos) {
			ResultDto resultDto = new ResultDto();
			String winner = historyDto.getWinner();
			resultDto.setWinners(winner);

			String sqlCars = "SELECT name, position FROM cars WHERE gameId = ?";
			int gameId = historyDto.getGameId();
			List<CarDto> carDtos = jdbcTemplate.query(sqlCars, carRowMapper, gameId);

			resultDto.setRacingCars(carDtos);
			resultDtos.add(resultDto);
		}
		return resultDtos;
	}
}
