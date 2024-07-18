package com.example.springbatchdemo.batch.playerload;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.stereotype.Service;

import com.example.springbatchdemo.batch.common.JobExecutionContextHolder;
import com.example.springbatchdemo.batch.common.JobParamsHolder;
import com.example.springbatchdemo.model.PlayerPages;
import com.example.springbatchdemo.restclient.PlayerClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerPageReader extends AbstractItemStreamItemReader<PlayerPages> {

    private final PlayerClient playerClient;
    private final JobParamsHolder jobParamsHolder;
    private final JobExecutionContextHolder jobExecutionContextHolder;

    @Override
    public PlayerPages read()
            throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        final long totalPagesFromContext = jobExecutionContextHolder.getExecutionContext().getLong("TOTALPAGES", -1);
        if (totalPagesFromContext == -1) {
            final ZonedDateTime localDate = jobParamsHolder.getJobParams().getLocalDate("queryDate",
                    LocalDate.now()).atStartOfDay(ZoneId.systemDefault());
            log.info("localDate is {}", localDate);

            final long queryDateSecondsSinceEpoch = localDate
                    .getLong(ChronoField.INSTANT_SECONDS);

            final PlayerPages playerPages = playerClient.getPlayerPages(
                    Instant.ofEpochSecond(queryDateSecondsSinceEpoch));
            jobExecutionContextHolder.getExecutionContext().putLong("TOTALPAGES", playerPages.getCount());
            return playerPages;
        } else {
            return null;
        }
    }

}
