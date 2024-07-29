package com.example.springbatchdemo.batch.playerload;

import java.util.Iterator;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.springbatchdemo.batch.common.JobExecutionContextHolder;
import com.example.springbatchdemo.model.Player;
import com.example.springbatchdemo.restclient.PlayerClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@StepScope
public class PlayerReader extends AbstractItemStreamItemReader<Player> {

    private final PlayerClient playerClient;
    private final JobExecutionContextHolder jobExecutionContextHolder;
    private ExecutionContext executionContext;
    private Iterator<Player> players;

    @Override
    public void open(@NonNull final ExecutionContext executionContext) throws ItemStreamException {
        this.executionContext = executionContext;
        setExecutionContextName("PlayerLoad");
    }

    @Override
    public Player read()
            throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        final long totalPagesFromContext = jobExecutionContextHolder.getExecutionContext().getLong("TOTALPAGES");
        final long pagesCompletedFromContext = executionContext.getLong(getExecutionContextKey("PAGESCOMPLETED"), 0);
        final long currentPageFromContext = executionContext.getLong(getExecutionContextKey("CURRENTPAGE"),
                pagesCompletedFromContext);
        log.debug(
                "PlayerReader.read() totalPagesFromContext: {} pagesCompletedFromContext: {} currentPageFromContext: {}",
                totalPagesFromContext, pagesCompletedFromContext,
                currentPageFromContext);
        if (totalPagesFromContext == pagesCompletedFromContext) {
            return null;
        } else {
            if (currentPageFromContext == pagesCompletedFromContext // This is a new chunk, as the
                                                                    // pagesCompletedFromContext has been updated by the
                                                                    // PlayerWriter or the reader being invoked for the
                                                                    // first time(aka first call of the first chunk), so
                                                                    // player will not be there, so will have to be
                                                                    // loaded.
                    || players == null // This is an existing chunk which was being processed, as the
                                       // currentPageFromContext confirms currently(it is 1 higher than the
                                       // pagesCompleted), but processing of a step failed, so the players will be null
                                       // and hence have to
                                       // be loaded.

            ) {
                log.debug("Getting new Players - existing players was {}", players);
                executionContext.putLong(getExecutionContextKey("CURRENTPAGE"), pagesCompletedFromContext + 1);
                players = playerClient.getPlayers(pagesCompletedFromContext + 1).listIterator();
                log.debug("Got new Players {}", players);
            } else {
                log.debug(
                        "PlayerReader.read() totalPagesFromContext: {} pagesCompletedFromContext: {} currentPageFromContext: {}, Players: {}",
                        totalPagesFromContext, pagesCompletedFromContext,
                        currentPageFromContext, players);
            }

            if (players.hasNext()) {
                final Player player = players.next();
                log.debug("returning next player {}", player);
                return player;
            } else {
                log.debug("Players doesn't have next, so returning null");
                return null;
            }

        }
    }

}
