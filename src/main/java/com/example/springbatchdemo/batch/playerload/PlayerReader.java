package com.example.springbatchdemo.batch.playerload;

import java.util.Iterator;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.stereotype.Service;

import com.example.springbatchdemo.batch.common.JobExecutionContextHolder;
import com.example.springbatchdemo.model.Player;
import com.example.springbatchdemo.restclient.PlayerClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerReader extends AbstractItemStreamItemReader<Player> {

    private final PlayerClient playerClient;
    private ExecutionContext executionContext;
    private final JobExecutionContextHolder jobExecutionContextHolder;
    private Iterator<Player> players;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
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
                executionContext.putLong(getExecutionContextKey("CURRENTPAGE"), pagesCompletedFromContext + 1);
                players = playerClient.getPlayers(pagesCompletedFromContext + 1).listIterator();
            }

            if (players.hasNext()) {
                return players.next();
            } else {
                return null;
            }

        }
    }

}
