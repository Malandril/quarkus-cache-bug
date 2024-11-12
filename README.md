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

# Reproducing the bug

Install [k6](https://k6.io/) or use a the k6 docker image and run the script
`k6 run -u 150 -d 60s k6.js`

I could reproduce with at least 150 parallel request.

