package org.kafka.etl.verticle;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.vertx.core.AbstractVerticle;
import org.kafka.etl.ioc.EtlContext;
import org.kafka.etl.kafka.ITopicStreamer;

public class EtlVerticle extends AbstractVerticle {
  private EtlContext injectContext;

  @Inject
  private ITopicStreamer streamer;

  @Override
  public void start() throws Exception {
    injectContext = new EtlContext(vertx);
    Injector injector = Guice.createInjector(injectContext);
    injector.injectMembers(this);

    streamer.startStreamAsync();
  }
}
