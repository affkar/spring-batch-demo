package com.example.springbatchdemo.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.springbatchdemo.entity.PlayerE;
import com.example.springbatchdemo.mapper.PlayerMapper;
import com.example.springbatchdemo.model.Player;
import com.example.springbatchdemo.model.PlayerPages;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class PlayerLoadBatchConfig {

        @Bean
        public Job playerDataLoadBatchJob(JobRepository jobRepository, Step playerPageLoad,
                        Step playerLoad,
                        JobExecutionListener jobParamsHolder,
                        JobExecutionListener jobExecutionContextHolder) {
                return new JobBuilder("PlayerLoadBatchJob", jobRepository)
                                .listener(jobParamsHolder)
                                .listener(jobExecutionContextHolder)
                                // .validator(jobParametersValidator())
                                .start(playerPageLoad)
                                .next(playerLoad)
                                .build();
        }

        @Bean
        public Step playerPageLoad(JobRepository jobRepository,
                        PlatformTransactionManager platformTransactionManager,
                        ItemReader<PlayerPages> playerPageReader) {
                return new StepBuilder("playerPageLoad", jobRepository)
                                .<PlayerPages, PlayerPages>chunk(1, platformTransactionManager)
                                .reader(playerPageReader)
                                .writer((playerPages) -> {
                                })
                                .build();
        }

        @Bean
        public Step playerLoad(JobRepository jobRepository,
                        PlatformTransactionManager platformTransactionManager,
                        ItemReader<Player> playerReader, PlayerMapper playerMapper,
                        ItemWriter<PlayerE> playersCompositeWriter) {
                return new StepBuilder("playerLoad", jobRepository)
                                .<Player, PlayerE>chunk(500, platformTransactionManager)
                                .reader(playerReader)
                                .processor((p) -> playerMapper.map(p))
                                .writer(playersCompositeWriter)
                                .build();
        }

        @Bean
        public ItemWriter<PlayerE> playersCompositeWriter(final EntityManagerFactory entityManagerFactory,
                        ItemWriter<PlayerE> playersWriter) {
                return new CompositeItemWriterBuilder<PlayerE>().delegates(
                                new JpaItemWriterBuilder<PlayerE>()
                                                .entityManagerFactory(entityManagerFactory)
                                                .build(),
                                playersWriter).build();
        }

}
