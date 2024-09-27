# quarkus-programmatic-cache-bug

When using a programmatic cache in a reactive `SecurityIdentityProvider` a heavy constant load causes issues in the 
reactive context, and the context is then in some invalide states and the following exception can be thrown

When running in a blocking context this does not occur.

```
jakarta.enterprise.context.ContextNotActiveException: RequestScoped context was not active when trying to obtain a bean instance for a client proxy of CLASS bean [class=io.quarkus.vertx.http.runtime.CurrentVertxRequest, id=0_6n6EmChCiiDdd8HelptG_A0AE]
        - you can activate the request context for a specific method using the @ActivateRequestContext interceptor binding
        at io.quarkus.arc.impl.ClientProxies.notActive(ClientProxies.java:70)
        at io.quarkus.arc.impl.ClientProxies.getSingleContextDelegate(ClientProxies.java:30)
        at io.quarkus.vertx.http.runtime.CurrentVertxRequest_ClientProxy.arc$delegate(Unknown Source)
        at io.quarkus.vertx.http.runtime.CurrentVertxRequest_ClientProxy.setCurrent(Unknown Source)
        at io.quarkus.resteasy.reactive.server.runtime.QuarkusCurrentRequest.set(QuarkusCurrentRequest.java:33)
        at org.jboss.resteasy.reactive.server.core.CurrentRequestManager.set(CurrentRequestManager.java:12)
        at org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext.handleRequestScopeActivation(ResteasyReactiveRequestContext.java:642)
        at io.quarkus.resteasy.reactive.server.runtime.QuarkusResteasyReactiveRequestContext.handleRequestScopeActivation(QuarkusResteasyReactiveRequestContext.java:39)
        at org.jboss.resteasy.reactive.common.core.AbstractResteasyReactiveContext.requireCDIRequestScope(AbstractResteasyReactiveContext.java:264)
        at org.jboss.resteasy.reactive.server.handlers.ResponseWriterHandler.handle(ResponseWriterHandler.java:27)
        at io.quarkus.resteasy.reactive.server.runtime.QuarkusResteasyReactiveRequestContext.invokeHandler(QuarkusResteasyReactiveRequestContext.java:147)
        at org.jboss.resteasy.reactive.common.core.AbstractResteasyReactiveContext.run(AbstractResteasyReactiveContext.java:147)
        at io.quarkus.virtual.threads.ContextPreservingExecutorService$ContextPreservingRunnable.run(ContextPreservingExecutorService.java:45)
        at java.base/java.util.concurrent.ThreadPerTaskExecutor$TaskRunner.run(ThreadPerTaskExecutor.java:314)
        at java.base/java.lang.VirtualThread.run(VirtualThread.java:309)
```

```
java.lang.IllegalStateException: Session/EntityManager is closed
        at org.hibernate.internal.AbstractSharedSessionContract.checkOpen(AbstractSharedSessionContract.java:517)
        at org.hibernate.engine.spi.SharedSessionContractImplementor.checkOpen(SharedSessionContractImplementor.java:186)
        at org.hibernate.internal.AbstractSharedSessionContract.checkOpenOrWaitingForAutoClose(AbstractSharedSessionContract.java:535)
        at org.hibernate.internal.SessionImpl.checkOpenOrWaitingForAutoClose(SessionImpl.java:622)
        at org.hibernate.internal.SessionImpl.instantiate(SessionImpl.java:1481)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.instantiateEntity(EntityInitializerImpl.java:1146)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.resolveEntityInstance(EntityInitializerImpl.java:1139)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.resolveEntityInstance2(EntityInitializerImpl.java:1097)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.resolveEntityInstance1(EntityInitializerImpl.java:1013)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.resolveInstance(EntityInitializerImpl.java:928)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.resolveKey(EntityInitializerImpl.java:528)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.resolveKey(EntityInitializerImpl.java:424)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.resolveKey(EntityInitializerImpl.java:94)
        at org.hibernate.sql.results.internal.StandardRowReader.coordinateInitializers(StandardRowReader.java:235)
        at org.hibernate.sql.results.internal.StandardRowReader.readRow(StandardRowReader.java:141)
        at org.hibernate.sql.results.spi.ListResultsConsumer.readUnique(ListResultsConsumer.java:283)
        at org.hibernate.sql.results.spi.ListResultsConsumer.consume(ListResultsConsumer.java:195)
        at org.hibernate.sql.results.spi.ListResultsConsumer.consume(ListResultsConsumer.java:35)
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.doExecuteQuery(JdbcSelectExecutorStandardImpl.java:224)
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.executeQuery(JdbcSelectExecutorStandardImpl.java:102)
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.executeQuery(JdbcSelectExecutor.java:91)
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.list(JdbcSelectExecutor.java:165)
        at org.hibernate.loader.ast.internal.SingleIdLoadPlan.load(SingleIdLoadPlan.java:145)
        at org.hibernate.loader.ast.internal.SingleIdEntityLoaderStandardImpl.load(SingleIdEntityLoaderStandardImpl.java:89)
        at org.hibernate.loader.ast.internal.AbstractEntityBatchLoader.load(AbstractEntityBatchLoader.java:94)
        at org.hibernate.loader.ast.internal.AbstractEntityBatchLoader.load(AbstractEntityBatchLoader.java:55)
        at org.hibernate.loader.ast.internal.EntityBatchLoaderInPredicate.load(EntityBatchLoaderInPredicate.java:116)
        at org.hibernate.persister.entity.AbstractEntityPersister.doLoad(AbstractEntityPersister.java:3777)
        at org.hibernate.persister.entity.AbstractEntityPersister.load(AbstractEntityPersister.java:3766)
        at org.hibernate.event.internal.DefaultLoadEventListener.loadFromDatasource(DefaultLoadEventListener.java:604)
        at org.hibernate.event.internal.DefaultLoadEventListener.loadFromCacheOrDatasource(DefaultLoadEventListener.java:590)
        at org.hibernate.event.internal.DefaultLoadEventListener.load(DefaultLoadEventListener.java:560)
        at org.hibernate.event.internal.DefaultLoadEventListener.doLoad(DefaultLoadEventListener.java:544)
        at org.hibernate.event.internal.DefaultLoadEventListener.load(DefaultLoadEventListener.java:206)
        at org.hibernate.event.internal.DefaultLoadEventListener.proxyOrLoad(DefaultLoadEventListener.java:245)
        at org.hibernate.event.internal.DefaultLoadEventListener.doOnLoad(DefaultLoadEventListener.java:110)
        at org.hibernate.event.internal.DefaultLoadEventListener.onLoad(DefaultLoadEventListener.java:69)
        at org.hibernate.event.service.internal.EventListenerGroupImpl.fireEventOnEachListener(EventListenerGroupImpl.java:138)
        at org.hibernate.internal.SessionImpl.fireLoadNoChecks(SessionImpl.java:1229)
        at org.hibernate.internal.SessionImpl.fireLoad(SessionImpl.java:1217)
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.load(IdentifierLoadAccessImpl.java:210)
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.doLoad(IdentifierLoadAccessImpl.java:161)
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.lambda$load$1(IdentifierLoadAccessImpl.java:150)
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.perform(IdentifierLoadAccessImpl.java:113)
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.load(IdentifierLoadAccessImpl.java:150)
        at org.hibernate.internal.SessionImpl.find(SessionImpl.java:2459)
        at org.hibernate.internal.SessionImpl.find(SessionImpl.java:2425)
        at io.quarkus.hibernate.orm.runtime.session.TransactionScopedSession.find(TransactionScopedSession.java:176)
        at org.hibernate.engine.spi.SessionLazyDelegator.find(SessionLazyDelegator.java:825)
        at org.hibernate.Session_OpdLahisOZ9nWRPXMsEFQmQU03A_Synthetic_ClientProxy.find(Unknown Source)
        at io.quarkus.hibernate.orm.panache.common.runtime.AbstractJpaOperations.findById(AbstractJpaOperations.java:183)
        at io.quarkus.hibernate.orm.panache.common.runtime.AbstractJpaOperations.findByIdOptional(AbstractJpaOperations.java:191)
        at org.acme.db.UserRepository.findByIdOptional(UserRepository.java)
        at org.acme.db.UserRepository.findByIdOptional(UserRepository.java)
        at org.acme.db.UserRepository_ClientProxy.findByIdOptional(Unknown Source)
        at org.acme.UserDatabaseIdentityProvider.authenticate(UserDatabaseIdentityProvider.java:55)
        at org.acme.UserDatabaseIdentityProvider.lambda$authenticate$0(UserDatabaseIdentityProvider.java:50)
        at io.quarkus.vertx.http.runtime.security.VertxBlockingSecurityExecutor$1$1.call(VertxBlockingSecurityExecutor.java:43)
        at io.vertx.core.impl.ContextImpl.lambda$executeBlocking$0(ContextImpl.java:178)
        at io.vertx.core.impl.ContextInternal.dispatch(ContextInternal.java:270)
        at io.vertx.core.impl.ContextImpl.lambda$internalExecuteBlocking$2(ContextImpl.java:210)
        at org.jboss.threads.ContextHandler$1.runWith(ContextHandler.java:18)
        at org.jboss.threads.EnhancedQueueExecutor$Task.doRunWith(EnhancedQueueExecutor.java:2516)
        at org.jboss.threads.EnhancedQueueExecutor$Task.run(EnhancedQueueExecutor.java:2495)
        at org.jboss.threads.EnhancedQueueExecutor$ThreadBody.run(EnhancedQueueExecutor.java:1521)
        at org.jboss.threads.DelegatingRunnable.run(DelegatingRunnable.java:11)
        at org.jboss.threads.ThreadLocalResettingRunnable.run(ThreadLocalResettingRunnable.java:11)
        at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
        at java.base/java.lang.Thread.run(Thread.java:1583)
```

# Reproducing the bug

Install [k6](https://k6.io/) or use a the k6 docker image and run the script
`k6 run -u 150 -d 60s k6.js`

I could reproduce with at least 150 parallel request.

