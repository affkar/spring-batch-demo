package com.example.springbatchdemo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.Parameter.param;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.MediaType;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.springbatchdemo.model.Player;
import com.example.springbatchdemo.model.PlayerPages;
import com.example.springbatchdemo.repository.PlayerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@ActiveProfiles("test")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class })

@TestPropertySource(properties = { "spring.batch.job.name=PlayerLoadBatchJob"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBatchTest
@SpringBootTest
@Slf4j
@ExtendWith(MockServerExtension.class)
public class SpringbatchdemoApplicationTests {

	@TestConfiguration
	static class WebClientTestConfig {
		@Bean
		public WebClient webClient() {
			return WebClient.builder().baseUrl("http://localhost:8081").build();
		}
	}

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;
	@Autowired
	private Job playerDataLoadBatchJob;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private PlayerRepository playerRepository;

	@BeforeAll
	static void beforeAll(final MockServerClient client) {
		client.bind(8081);
	}

	@BeforeEach
	void setUp(final MockServerClient client) {

		jobLauncherTestUtils.setJob(playerDataLoadBatchJob);
		assertThat(playerRepository.count()).isEqualTo(0L);
	}

	@AfterEach
	void tearDown(final MockServerClient client) {
		client.reset();
		jobRepositoryTestUtils.removeJobExecutions();
		playerRepository.deleteAll();
	}

	@Test
	public void contextLoads() throws Exception {
		log.info("SpringbatchdemoApplicationTests.contextLoads()");
	}

	@Test
	public void playerDataLoadBatchJob_HappyPath(final MockServerClient client) throws Exception {
		final int expectedCount = 10;
		client
				.when(
						request()
								.withMethod("GET")
								.withPath("/api/player/pages/count/1"))
				.respond(
						response()
								.withBody(objectMapper
										.writeValueAsString(PlayerPages.builder().count(expectedCount).build()))
								.withContentType(MediaType.APPLICATION_JSON));
		IntStream.rangeClosed(1, expectedCount).forEach(i -> setupPlayerLoadResponse(client, i));

		final JobParameters jobParameters = new JobParameters(
				Map.<String, JobParameter<?>>of("queryDate",
						new JobParameter<>("2024-07-13", String.class)));

		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

		assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
		assertThat(jobExecution.getStepExecutions())
				.extracting(StepExecution::getStepName, StepExecution::getReadCount, StepExecution::getWriteCount,
						StepExecution::getCommitCount)
				.containsExactly(Tuple.tuple("playerPageLoad", 1L, 1L, 2L),
						Tuple.tuple("playerLoad", 5000L, 5000L, 11L));
		assertThat(playerRepository.count()).isEqualTo(5000L);
	}

	@Test
	public void playerDataLoadBatchJob_FailureAt_PlayerLoadStep(final MockServerClient client) throws Exception {
		final int expectedCount = 10;
		client
				.when(
						request()
								.withMethod("GET")
								.withPath("/api/player/pages/count/1"))
				.respond(
						response()
								.withBody(objectMapper
										.writeValueAsString(PlayerPages.builder().count(expectedCount).build()))
								.withContentType(MediaType.APPLICATION_JSON));
		IntStream.rangeClosed(1, 4).forEach(i -> setupPlayerLoadResponse(client, i));
		// note response for page 5 is not setup and hence when the batch step calls it,
		// there will be a failure.
		IntStream.rangeClosed(6, 10).forEach(i -> setupPlayerLoadResponse(client, i));

		final JobParameters jobParameters = new JobParameters(
				Map.<String, JobParameter<?>>of("queryDate",
						new JobParameter<>("2024-07-13", String.class)));

		JobExecution jobExecution1 = jobLauncherTestUtils.launchJob(jobParameters);

		assertThat(jobExecution1.getExitStatus().getExitCode()).isEqualTo(ExitStatus.FAILED.getExitCode());
		assertThat(jobExecution1.getStepExecutions())
				.extracting(StepExecution::getStepName, StepExecution::getReadCount, StepExecution::getWriteCount,
						StepExecution::getCommitCount)
				.containsExactly(Tuple.tuple("playerPageLoad", 1L, 1L, 2L),
						Tuple.tuple("playerLoad", 2000L, 2000L, 4L));
		assertThat(playerRepository.count()).isEqualTo(2000L);

		// now setup response for page 5.
		IntStream.range(5, 6).forEach(i -> setupPlayerLoadResponse(client, i));

		JobExecution jobExecution2 = jobLauncherTestUtils.launchJob(jobParameters);

		assertThat(jobExecution2.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
		assertThat(jobExecution2.getStepExecutions())
				.extracting(StepExecution::getStepName, StepExecution::getReadCount,
						StepExecution::getWriteCount,
						StepExecution::getCommitCount)
				.containsExactly(
						Tuple.tuple("playerLoad", 3000L, 3000L, 7L));
		assertThat(playerRepository.count()).isEqualTo(5000L);
	}

	private void setupPlayerLoadResponse(final MockServerClient client, int i) {
		try {
			client
					.when(
							request()
									.withMethod("GET")
									.withPath("/api/player/pages")
									.withQueryStringParameters(
											param("pageNo", String.valueOf(i))))
					.respond(
							response()
									.withBody(objectMapper.writeValueAsString(getPlayers(i)))
									.withContentType(MediaType.APPLICATION_JSON));
		} catch (JsonProcessingException e) {
			log.error("Unable to setup mock for GET /api/player/pages?pageNo=" + i, e);
		}
	}

	public List<Player> getPlayers(Integer pageNo) {
		// if (pageNo == 100) {
		// throw new RuntimeException("Can't handle load at page no. 100");
		// }
		return IntStream.rangeClosed((500 * (pageNo - 1)) + 1, 500 * pageNo)
				.boxed()
				.map(i -> Player.builder().id(i).name("player" + i).build())
				.collect(Collectors.toList());
	}

}
