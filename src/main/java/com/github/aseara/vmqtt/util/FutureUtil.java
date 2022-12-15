package com.github.aseara.vmqtt.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FutureUtil {

    /**
     * 对列表中的每个元素执行相同的操作，合并全部执行结果交由 resultHandler处理
     * @param list               列表
     * @param op                 执行操作
     * @param resultHandler      结果处理
     * @param <T>                列表元素类型
     */
    @SuppressWarnings("rawtypes")
    public  static <T> void listAll(
            List<T> list, Function<T, Future> op,
            Handler<AsyncResult<CompositeFuture>> resultHandler) {
        if (list == null || list.size() == 0) {
            resultHandler.handle(Future.succeededFuture());
            return;
        }
        List<Future> close = list.stream().map(op).collect(Collectors.toList());
        CompositeFuture.all(close).onComplete(resultHandler);
    }

}
