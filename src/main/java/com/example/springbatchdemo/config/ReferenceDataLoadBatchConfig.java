package com.example.springbatchdemo.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.springbatchdemo.entity.CharacterE;
import com.example.springbatchdemo.model.Character;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class ReferenceDataLoadBatchConfig {
    @Bean
    public Job referenceDataLoadBatchJob(JobRepository jobRepository, Step characteristicLoad) {
        return new JobBuilder("ReferenceDataLoadBatchJob", jobRepository)
                // .listener(jobCom)
                // .validator(jobParametersValidator())
                .start(characteristicLoad)
                .build();
    }

    @Bean
    public Step characteristicLoad(JobRepository jobRepository,
            @Qualifier("springCloudTaskTransactionManager") PlatformTransactionManager platformTransactionManager,
            ItemReader<Character> characterReader, ItemProcessor<Character, CharacterE> characterTransformer,
            ItemWriter<CharacterE> characterJpaItemWriter) {
        return new StepBuilder("characteristicLoad", jobRepository)
                .<Character, CharacterE>chunk(10, platformTransactionManager)
                .reader(characterReader)
                .processor(characterTransformer)
                .writer(characterJpaItemWriter)
                .build();
    }

    @Bean
    public ItemWriter<CharacterE> characterJpaItemWriter(final EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<CharacterE>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public ItemReader<Character> characterReader() {
        return new FlatFileItemReaderBuilder<Character>()
                .name("characterItemReader")
                .resource(new ClassPathResource("data-load/sample-characteristics.csv"))
                .delimited()
                .names("type")
                .targetType(Character.class)
                .build();
    }

    @Bean
    public ItemProcessor<Character, CharacterE> characterTransformer() {
        return (character) -> CharacterE.builder().type(character.getType()).build();
    }
}
