package com.example.springbatchdemo.batch.playerload;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.springbatchdemo.entity.PlayerE;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@StepScope
public class PlayersWriter extends AbstractItemStreamItemWriter<PlayerE> {

    private ExecutionContext executionContext;

    @Override
    public void open(@NonNull final ExecutionContext executionContext) throws ItemStreamException {
        this.executionContext = executionContext;
        setExecutionContextName("PlayerLoad");
    }

    @Override
    public void write(@NonNull final Chunk<? extends PlayerE> chunk) throws Exception {

        final long pagesCompletedFromContext = executionContext.getLong(getExecutionContextKey("PAGESCOMPLETED"), 0);
        executionContext.putLong(getExecutionContextKey("PAGESCOMPLETED"), pagesCompletedFromContext + 1);
    }

}
