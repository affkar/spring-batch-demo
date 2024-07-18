package com.example.springbatchdemo.batch.playerload;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.stereotype.Service;

import com.example.springbatchdemo.entity.PlayerE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayersWriter extends AbstractItemStreamItemWriter<PlayerE> {

    private final JobParamsHolder jobParamsHolder;
    private ExecutionContext executionContext;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.executionContext = executionContext;
        setExecutionContextName("PlayerPage");
    }

    @Override
    public void write(Chunk<? extends PlayerE> chunk) throws Exception {
        final long pagesCompletedFromContext = executionContext.getLong(getExecutionContextKey("PAGESCOMPLETED"), 0);
        executionContext.putLong(getExecutionContextKey("PAGESCOMPLETED"), pagesCompletedFromContext + 1);
    }

}
